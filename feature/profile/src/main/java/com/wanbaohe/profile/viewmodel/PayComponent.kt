package com.wanbaohe.profile.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.hilt.ResourceProvider
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PayState
import com.shifenmiao.model.pay.PayUIState
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.AlipayResult
import com.shifenmiao.model.pay.alipay.PayEncodeParamResult
import com.shifenmiao.model.pay.alipay.PayParams
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.model.pay.google.GooglePayVerifyRequest
import com.shifenmiao.model.pay.google.GooglePlayOrder
import com.shifenmiao.model.pay.google.GooglePlayPayResult
import com.shifenmiao.model.pay.google.PlayProduct
import com.shifenmiao.model.pay.wechat.WechatPayResult
import com.shifenmiao.model.pay.wechat.WechatPrepayRequest
import com.shifenmiao.model.pay.wechat.WechatPrepayResponse
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.model.wechat.event.WechatEvent
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.utils.NetworkUtils
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.pay.PaymentResultCallback
import com.shifenmiao.pay.alipay.Alipay
import com.shifenmiao.pay.google.GooglePlayBilling
import com.shifenmiao.pay.wechat.WechatPay
import com.shifenmiao.model.wechat.common.WechatProtocol
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class PayComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    private val apiService: ApiService,
    private val resourceProvider: ResourceProvider,
    private val channelConfig: ChannelConfig,
    private val analyticsManager: AnalyticsManager,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _paymentOptions: List<PaymentMethod<PrePayResponse>> = buildList {
        if (channelConfig.enableAlipay) {
            add(Alipay())
        }
        if (channelConfig.enableWechat) {
            add(WechatPay())
        }
        if (channelConfig.enablePlayBilling) {
            add(GooglePlayBilling())
        }
    }

    /** Google Play Billing 实例(google 渠道), 供商品查询/消耗直接调用 */
    private val googlePlayBilling: GooglePlayBilling? =
        _paymentOptions.filterIsInstance<GooglePlayBilling>().firstOrNull()

    /** 是否 Google Play Billing 渠道(决定充值 UI 展示 Play 商品而非人民币档位) */
    val playBillingEnabled: Boolean = channelConfig.enablePlayBilling

    /** Play 商品目录(后端 productId/points + Play 本地化价格) */
    private val _playProducts = MutableStateFlow<List<PlayProduct>>(emptyList())
    val playProducts: StateFlow<List<PlayProduct>> = _playProducts

    /** Play 商品加载失败原因(非空表示加载已结束且失败, UI 据此展示失败占位而非一直转圈) */
    private val _playProductsError = MutableStateFlow<String?>(null)
    val playProductsError: StateFlow<String?> = _playProductsError

    private var currentTradeNo: String = ""

    // 默认选中第一个可用支付方式; 支付全关时入口均已隐藏, 此处仅兜底
    private val _selectedPayment = mutableStateOf<PaymentMethod<PrePayResponse>>(
        _paymentOptions.firstOrNull() ?: Alipay()
    )
    val selectedPayment: PaymentMethod<PrePayResponse> by _selectedPayment

    private val _errors = MutableStateFlow<List<String>>(listOf())
    val errorState: StateFlow<List<String>> = _errors

    private val _loadingState = mutableStateOf(false)
    val loadingState: Boolean by _loadingState

    private val _payUIState = mutableStateOf(PayUIState())
    val payUIState: PayUIState by _payUIState

    init {
        if (channelConfig.enableWechat) {
            componentScope.launch {
                com.shifenmiao.model.event.AppEventBus.wechatEvents.collect { onWechatPayEvent(it) }
            }
        }
        if (channelConfig.enablePlayBilling) {
            componentScope.launch(defaultDispatcher) {
                loadPlayProducts()
                recoverOwnedPurchases()
            }
        }
        setPayUIState(PayState.DEFAULT)
    }

    /** 拉取后端商品目录(productId+points), 再查询 Play 本地化价格合并为 UI 模型 */
    private suspend fun loadPlayProducts() {
        val billing = googlePlayBilling ?: return
        val response = NetworkUtils.safeApiCall { apiService.googlePlayProducts() }
        val catalog = response?.body().orEmpty()
        if (response?.isSuccessful != true || catalog.isEmpty()) {
            onPlayProductsLoadFailed(resourceProvider.getString(R.string.google_play_products_load_failed))
            return
        }
        val detailsList = billing.queryPlayProducts(catalog.map { it.productId })
        if (detailsList.isEmpty()) {
            onPlayProductsLoadFailed(billing.lastErrorMessage())
            return
        }
        val detailsById = detailsList.associateBy { it.productId }
        // 顺序以后端目录为准, Play 查不到详情的商品直接剔除
        _playProducts.value = catalog.mapNotNull { product ->
            val details = detailsById[product.productId] ?: return@mapNotNull null
            PlayProduct(
                productId = product.productId,
                points = product.points,
                title = details.name,
                formattedPrice = details.formattedPrice,
            )
        }
    }

    private fun onPlayProductsLoadFailed(message: String) {
        _playProductsError.value = message
        addError(message)
    }

    /**
     * 支付中断补偿: 用户已付款但验单失败(如断网)时购买会滞留未消耗,
     * 这里静默重新验单 + 消耗(服务端幂等), 避免付了钱拿不到积分也无法复购
     */
    private suspend fun recoverOwnedPurchases() {
        val billing = googlePlayBilling ?: return
        billing.queryOwnedPurchases().forEach { owned ->
            val response = NetworkUtils.safeApiCall {
                apiService.googlePlayVerify(
                    GooglePayVerifyRequest(
                        productId = owned.productId,
                        purchaseToken = owned.purchaseToken,
                    )
                )
            }
            if (response?.isSuccessful == true) {
                billing.consumePurchase(owned.purchaseToken)
            }
        }
    }

    fun onWechatPayEvent(event: WechatEvent) {
        componentScope.launch {
            val resp = event.message
            if (resp.type == WechatProtocol.COMMAND_PAY_BY_WX) {
                if (_selectedPayment.value is WechatPay) {
                    _selectedPayment.value.onPaySuccess(
                        WechatPayResult(
                            code = resp.code,
                            transaction = resp.transaction,
                            openId = resp.openId,
                            outTradeNo = currentTradeNo
                        )
                    )
                }
            }
        }
    }

    fun getPaymentList(): List<PaymentMethod<PrePayResponse>> {
        return _paymentOptions
    }

    /** 支付成功埋点: 服务端验单通过后上报 GA4 预定义 purchase 事件(国内渠道为 no-op) */
    private fun trackPurchase(method: String, params: Map<String, Any>) {
        analyticsManager.logEvent("purchase", params + ("payment_method" to method))
    }

    fun prePayInfo(
        context: Context,
        paymentMethod: PaymentMethod<PrePayResponse>,
        payPrice: PayPrice,
        loginComponent: LoginComponent
    ) {
        if (_paymentOptions.isEmpty()) return
        componentScope.launch {
            if (_loadingState.value) {
                return@launch
            }
            showLoading()
            if (paymentMethod is Alipay) {
                prePayAlipay(context, paymentMethod, payPrice, loginComponent)
            } else if (paymentMethod is WechatPay) {
                // 微信支付金额最小值是1元
                if (payPrice.price < 1f) {
                    ActionUtils.showError(R.string.wechat_pay_min_amount)
                    setPayUIState(PayState.FAILURE)
                    return@launch
                }
                if (!Wechat.isInstalled()) {
                    ActionUtils.showError(R.string.wechat_not_installed)
                    setPayUIState(PayState.FAILURE)
                    return@launch
                }
                preWechatPay(context, paymentMethod, payPrice, loginComponent)
            }
        }
    }

    /** Google Play 渠道支付: 拉起 Play 支付 → 服务端验单 → 验单成功后消耗商品(可复购) */
    fun payPlayProduct(
        context: Context,
        product: PlayProduct,
        loginComponent: LoginComponent
    ) {
        val billing = googlePlayBilling ?: return
        if (_loadingState.value) return
        showLoading()
        billing.setPaymentResultCallback(object : PaymentResultCallback {
            override fun onPaymentSuccess(payResult: PayResult) {
                val result = payResult as GooglePlayPayResult
                loginComponent.googlePlayReturnVerify(
                    productId = result.productId,
                    purchaseToken = result.purchaseToken,
                    onSuccess = {
                        // 验单成功后才能消耗; 失败仅影响复购, 由 recoverOwnedPurchases 补偿
                        componentScope.launch(defaultDispatcher) {
                            billing.consumePurchase(result.purchaseToken)
                        }
                        trackPurchase(
                            method = "google_play",
                            params = mapOf(
                                "item_id" to product.productId,
                                "points" to product.points
                            )
                        )
                        ActionUtils.showToast(R.string.pay_success)
                        setPayUIState(PayState.SUCCESS)
                    },
                    onFail = {
                        ActionUtils.showError(
                            resourceProvider.getString(R.string.pay_fail, it)
                        )
                        setPayUIState(PayState.FAILURE)
                    })
            }

            override fun onPaymentFailure(error: String) {
                ActionUtils.showError(error)
                setPayUIState(PayState.FAILURE)
            }

            override fun onPaymentIncomplete() {
                setPayUIState(PayState.INCOMPLETE)
            }
        })
        // payPrice 在 Google Play 路径不使用(价格由 Play 商品详情提供), 传占位值
        billing.pay(context, PayPrice.WuMaoPrice, GooglePlayOrder(product.productId))
    }

    private fun preWechatPay(
        context: Context,
        paymentMethod: WechatPay,
        payPrice: PayPrice,
        loginComponent: LoginComponent
    ) {
        componentScope.launch(defaultDispatcher) {
            currentTradeNo = "${payPrice.userId}_${Date().time}"
            val wechatPrepayRequest = WechatPrepayRequest(
                description = resourceProvider.getString(payPrice.name),
                outTradeNo = currentTradeNo,
                amount = WechatPrepayRequest.Amount(
                    total = payPrice.price.toLong(),
                    currency = "CNY"
                ),
            )
            val response = NetworkUtils.safeApiCall {
                apiService.wechatPayOrder(wechatPrepayRequest)
            }
            response?.let {
                if (response.isSuccessful && response.body() != null) {
                    handleWechatPayResult(
                        context,
                        paymentMethod,
                        response.body(),
                        payPrice,
                        loginComponent
                    )
                } else {
                    addError("生成支付订单失败")
                    setPayUIState(PayState.FAILURE)
                }
            }
        }
    }

    private fun handleWechatPayResult(
        context: Context,
        paymentMethod: WechatPay,
        prepayResponse: WechatPrepayResponse?,
        payPrice: PayPrice,
        loginComponent: LoginComponent
    ) {
        prepayResponse?.let {
            paymentMethod.setPaymentResultCallback(object : PaymentResultCallback {
                override fun onPaymentSuccess(payResult: PayResult) {
                    loginComponent.wechatReturnVerify(
                        payResult = payResult as WechatPayResult,
                        onSuccess = {
                            trackPurchase(
                                method = "wechat",
                                params = mapOf(
                                    "value" to payPrice.price.toDouble(),
                                    "currency" to "CNY"
                                )
                            )
                            ActionUtils.showToast(R.string.pay_success)
                            setPayUIState(PayState.SUCCESS)
                        },
                        onFail = {
                            ActionUtils.showError(
                                resourceProvider.getString(
                                    R.string.pay_fail, it
                                )
                            )
                            setPayUIState(PayState.FAILURE)
                        })
                }

                override fun onPaymentFailure(error: String) {
                    setPayUIState(PayState.FAILURE)
                }

                override fun onPaymentIncomplete() {
                    setPayUIState(PayState.INCOMPLETE)
                }
            })
            paymentMethod.pay(context, payPrice, prepayResponse)
        }
    }

    private fun prePayAlipay(
        context: Context,
        paymentMethod: Alipay,
        payPrice: PayPrice,
        loginComponent: LoginComponent
    ) {
        componentScope.launch(defaultDispatcher) {
            val payParams = PayParams(
                subject = resourceProvider.getString(payPrice.name),
                outTradeNo = "${payPrice.userId}_${Date().time}",
                totalAmount = payPrice.price.toString(),
                productCode = "QUICK_MSECURITY_PAY",
            )
            val response = apiService.getAliPayOrder(payParams)
            if (response.isSuccessful && response.body() != null) {
                handleAlipayResult(
                    context,
                    paymentMethod,
                    response.body(),
                    payPrice,
                    loginComponent
                )
            } else {
                addError("获取支付信息失败")
                setPayUIState(PayState.FAILURE)
            }
        }
    }

    private fun handleAlipayResult(
        context: Context,
        paymentMethod: PaymentMethod<PrePayResponse>,
        payEncodeParamResult: PayEncodeParamResult?,
        payPrice: PayPrice,
        loginComponent: LoginComponent
    ) {
        componentScope.launch {
            payEncodeParamResult?.let {
                paymentMethod.setPaymentResultCallback(object : PaymentResultCallback {
                    override fun onPaymentSuccess(payResult: PayResult) {
                        loginComponent.alipayReturnVerify(
                            alipayResult = payResult as AlipayResult,
                            onSuccess = {
                                trackPurchase(
                                    method = "alipay",
                                    params = mapOf(
                                        "value" to payPrice.price.toDouble(),
                                        "currency" to "CNY"
                                    )
                                )
                                setPayUIState(PayState.SUCCESS)
                                ActionUtils.showToast(R.string.pay_success)
                            },
                            onFail = {
                                ActionUtils.showError(
                                    resourceProvider.getString(
                                        R.string.pay_fail, it
                                    )
                                )
                                setPayUIState(PayState.FAILURE)
                            })
                    }

                    override fun onPaymentFailure(error: String) {
                        setPayUIState(PayState.FAILURE)
                    }

                    override fun onPaymentIncomplete() {
                        setPayUIState(PayState.INCOMPLETE)
                    }
                })
                paymentMethod.pay(context, payPrice, payEncodeParamResult)
            }
        }
    }

    private fun addError(errorMessage: String) {
        // 使用新的列表替换，只包含当前错误，避免旧错误重复显示
        _errors.value = listOf(errorMessage)
    }

    fun clearErrors() {
        _errors.value = emptyList()
    }

    private fun setLoading(loading: Boolean) {
        componentScope.launch {
            _loadingState.value = loading
        }
    }

    private fun showLoading() {
        setLoading(true)
        setPayUIState(PayState.LOADING)
    }

    fun setPaymentMethod(paymentMethod: PaymentMethod<PrePayResponse>) {
        componentScope.launch {
            _selectedPayment.value = paymentMethod
        }
    }

    fun setPayUIState(payState: PayState) {
        componentScope.launch {
            _payUIState.value = _payUIState.value.copy(
                payState = payState
            )
            setLoading(false)
        }
    }

    fun setPayDefaultState() {
        componentScope.launch {
            _payUIState.value = _payUIState.value.copy(
                payState = PayState.DEFAULT
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): PayComponent
    }
}

