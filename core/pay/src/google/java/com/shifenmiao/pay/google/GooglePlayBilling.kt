package com.shifenmiao.pay.google

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.PrePayResponse
import com.shifenmiao.model.pay.alipay.PayPrice
import com.shifenmiao.model.pay.google.GooglePlayOrder
import com.shifenmiao.model.pay.google.GooglePlayPayResult
import com.shifenmiao.pay.PaymentMethod
import com.shifenmiao.pay.PaymentResultCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Google Play Billing 支付方式(消耗型积分商品, 仅 google 渠道启用).
 *
 * 职责: BillingClient 连接管理(断线后下次操作自动重连)、商品详情查询、拉起支付、结果回调.
 * 注意: 客户端不做 consume, 须等服务端 /google/pay/verify 验单成功后调 [consumePurchase],
 * 否则验单失败时用户已付款但商品被消耗, 无法补偿.
 */
class GooglePlayBilling : PaymentMethod<PrePayResponse>, PurchasesUpdatedListener {

    private var paymentResultCallback: PaymentResultCallback? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(AppContext.getContext())
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    /** productId -> ProductDetails 缓存, 拉起支付与展示价格共用 */
    private val productDetailsCache = ConcurrentHashMap<String, ProductDetails>()

    /** 最近一次结算操作的 BillingResponseCode, 供 UI 区分"设备不支持结算"与普通失败 */
    @Volatile
    var lastErrorCode: Int = BillingClient.BillingResponseCode.OK
        private set

    override val id: Int
        get() = 2
    override val displayName: String
        get() = "Google Play"

    override fun getIcon(): Int {
        return R.drawable.google_play
    }

    override fun setPaymentResultCallback(callback: PaymentResultCallback) {
        this.paymentResultCallback = callback
    }

    private fun consumeCallback(): PaymentResultCallback? {
        val callback = paymentResultCallback
        paymentResultCallback = null
        return callback
    }

    /** 确保 BillingClient 已连接; 断线时重新 startConnection(自动重连) */
    private suspend fun ensureConnected(): Boolean = suspendCancellableCoroutine { cont ->
        if (billingClient.isReady) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    lastErrorCode = billingResult.responseCode
                }
                if (cont.isActive) {
                    cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }
            }

            override fun onBillingServiceDisconnected() {
                // 等待中的连接失败返回; 已建立的连接断开后, 下次操作会自动重连
                if (cont.isActive) {
                    lastErrorCode = BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
                    cont.resume(false)
                }
            }
        })
    }

    /** 查询一次性商品详情并缓存, 失败返回空列表(失败原因见 [lastErrorCode]);
     *  返回 SDK-free 的 [PlayProductInfo], 使 main 源集无需依赖 Billing SDK 即可消费 */
    suspend fun queryPlayProducts(productIds: List<String>): List<PlayProductInfo> {
        if (productIds.isEmpty() || !ensureConnected()) return emptyList()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            lastErrorCode = result.billingResult.responseCode
            Log.w(TAG, "queryProductDetails failed: ${result.billingResult.debugMessage}")
            return emptyList()
        }
        lastErrorCode = BillingClient.BillingResponseCode.OK
        val list = result.productDetailsList.orEmpty()
        list.forEach { productDetailsCache[it.productId] = it }
        return list.map {
            PlayProductInfo(
                productId = it.productId,
                name = it.name,
                formattedPrice = it.oneTimePurchaseOfferDetails?.formattedPrice.orEmpty(),
            )
        }
    }

    /** 查询当前已持有但未消耗的购买, 用于支付中断(验单失败)后的补偿 */
    suspend fun queryOwnedPurchases(): List<GooglePlayPayResult> {
        if (!ensureConnected()) return emptyList()
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return emptyList()
        }
        return result.purchasesList.orEmpty()
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .map {
                GooglePlayPayResult(
                    productId = it.products.firstOrNull().orEmpty(),
                    purchaseToken = it.purchaseToken,
                )
            }
    }

    /** 最近失败的用户可读提示; 设备不支持/服务不可达/网络异常有专属文案 */
    fun lastErrorMessage(): String = billingErrorMessage(lastErrorCode, "")

    /** BillingResponseCode → 用户可读提示; 未特判的码回退 debugMessage 或通用错误 */
    private fun billingErrorMessage(responseCode: Int, debugMessage: String): String {
        val resId = when (responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
                R.string.google_pay_billing_unavailable

            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT ->
                R.string.google_pay_service_unavailable

            BillingClient.BillingResponseCode.NETWORK_ERROR ->
                R.string.google_pay_network_error

            else -> null
        }
        return when {
            resId != null -> AppContext.getString(resId)
            debugMessage.isNotBlank() -> debugMessage
            else -> AppContext.getString(R.string.pay_error)
        }
    }

    fun launchPlayPurchase(activity: Activity, productId: String) {
        val productDetails = productDetailsCache[productId]
        if (productDetails == null) {
            onPayFailure(AppContext.getString(R.string.google_pay_product_unavailable))
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            onPayFailure(billingErrorMessage(result.responseCode, result.debugMessage))
        }
    }

    /** 服务端验单成功后调用, 消耗商品使其可复购; 失败仅记日志, 由下次 [queryOwnedPurchases] 补偿 */
    suspend fun consumePurchase(purchaseToken: String): Boolean {
        if (!ensureConnected()) return false
        val result = billingClient.consumePurchase(
            ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        )
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "consumePurchase failed: ${result.billingResult.debugMessage}")
            return false
        }
        return true
    }

    override fun pay(context: Context, payPrice: PayPrice, prePayResult: PrePayResponse) {
        val order = prePayResult as? GooglePlayOrder
        val activity = context as? Activity
        if (order == null || activity == null) {
            onPayFailure(AppContext.getString(R.string.pay_error))
            return
        }
        launchPlayPurchase(activity, order.productId)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (purchase != null) {
                    onPaySuccess(
                        GooglePlayPayResult(
                            productId = purchase.products.firstOrNull().orEmpty(),
                            purchaseToken = purchase.purchaseToken,
                        )
                    )
                } else {
                    onPayFailure(AppContext.getString(R.string.pay_error))
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> onPayIncomplete()

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                onPayFailure(AppContext.getString(R.string.google_pay_already_owned))

            else -> onPayFailure(
                billingErrorMessage(billingResult.responseCode, billingResult.debugMessage)
            )
        }
    }

    override fun onPaySuccess(payResult: PayResult) {
        consumeCallback()?.onPaymentSuccess(payResult)
    }

    override fun onPayFailure(error: String) {
        consumeCallback()?.onPaymentFailure(error)
    }

    override fun onPayIncomplete() {
        consumeCallback()?.onPaymentIncomplete()
    }

    private companion object {
        const val TAG = "GooglePlayBilling"
    }
}
