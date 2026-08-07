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
import com.shifenmiao.pay.wechat.WechatPay
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.tencent.mm.opensdk.constants.ConstantsAPI
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
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _paymentOptions: List<PaymentMethod<PrePayResponse>> = buildList {
        if (channelConfig.enableAlipay) {
            add(Alipay())
        }
        if (channelConfig.enableWechat) {
            add(WechatPay())
        }
    }

    private var currentTradeNo: String = ""

    // 默认选中第一个可用支付方式; 支付全关时(google 渠道)入口均已隐藏, 此处仅兜底
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
        setPayUIState(PayState.DEFAULT)
    }

    fun onWechatPayEvent(event: WechatEvent) {
        componentScope.launch {
            val resp = event.message
            if (resp.type == ConstantsAPI.COMMAND_PAY_BY_WX) {
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

