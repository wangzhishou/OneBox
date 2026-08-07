package com.shifenmiao.base.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.Icons
import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import com.shifenmiao.base.entrypoint.AppEntryPoint
import com.shifenmiao.base.hilt.ResourceProvider
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.auth.RequestAuthorizationCodeEvent
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.login.LoginChannelConfig
import com.shifenmiao.model.login.LoginEvent
import com.shifenmiao.model.user.event.BindPhoneEvent
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import androidx.core.net.toUri
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError


object ActionUtils {

    private val resourceProvider: ResourceProvider

    private val authorizationCodeStateHolder: AuthorizationCodeStateHolder

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context = AppContext.getContext(),
            entryPoint = AppEntryPoint::class.java
        )
        resourceProvider = entryPoint.getResourceProvider()
        authorizationCodeStateHolder = entryPoint.getAuthorizationCodeStateHolder()
    }

    fun showToast(
        message: String,
        duration: ToastDuration = ToastDuration.Short
    ) {
        AppToastHost.showToast(message = message, duration = duration)
    }

    fun showToast(resId: Int) {
        showToast(resourceProvider.getString(resId))
    }

    fun showToast(
        resId: Int,
        duration: ToastDuration = ToastDuration.Short
    ) {
        showToast(resourceProvider.getString(resId), duration)
    }

    fun showError(message: String) {
        AppToastHost.showToast(message = message,
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
            duration = ToastDuration.Long)
    }

    fun showError(resId: Int) {
        showError(resourceProvider.getString(resId))
    }

    fun openWebBrowser(context: Context, url: String) {
        showToast(context.getString(R.string.open_url_toast))
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /****************
     *
     * 发起添加群流程。群号：好特别(13110846) 的 key 为： 1JOfn5KCue56UhXT1fRe6NgCLJB5sHFO
     * 调用 joinQQGroup(1JOfn5KCue56UhXT1fRe6NgCLJB5sHFO) 即可发起手Q客户端申请加群 万宝盒(13110846)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(context: Context, key: String): Boolean {

        Toast.makeText(context, R.string.open_qq_group, Toast.LENGTH_SHORT).show()
        val intent = Intent()
        intent.setData("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key".toUri())
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

    fun showLogin(
        source: String = "unknown",
        onFailure: (String) -> Unit = {
            showToast(R.string.login_failed)
        },
        onSuccess: () -> Unit = {},
    ) {
        if (TokenStorage.isLogin()) {
            onSuccess.invoke()
            return
        } else {
            AppEventBus.emit(
                LoginEvent(
                    source = source,
                    onSuccess = {
                        onSuccess.invoke()
                    },
                    onFailure = {
                        onFailure.invoke(it.message())
                    }
                ))
        }
    }

    private fun showBindPhone(
        source: String = "unknown",
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {
            showError(R.string.login_failed)
        }
    ) {
        AppEventBus.emit(
            BindPhoneEvent(
                source = source,
                onSuccess = {
                    onSuccess.invoke()
                },
                onError = {
                    onFailure.invoke(it)
                }
            ))
    }

    private fun showLoginNeedBindPhone(
        source: String = "unknown",
        onFailure: (String) -> Unit = {
            showError(R.string.login_failed)
        },
        onSuccess: () -> Unit = {},
    ) {
        showLogin(
            source,
            onSuccess = {
                if (TokenStorage.isLogin()) {
                    if (isNeedBindPhone()) {
                        showBindPhone(
                            source,
                            onSuccess = {
                                onSuccess.invoke()
                            },
                            onFailure = {
                                onFailure.invoke(it)
                            }
                        )
                    } else {
                        onSuccess.invoke()
                    }
                } else {
                    onSuccess.invoke()
                }
            },
            onFailure = {
                onFailure.invoke(it)
            }
        )
    }

    fun isNeedBindPhone(): Boolean {
        // 海外渠道不提供绑定手机功能, 不受远端配置影响
        if (!LoginChannelConfig.getConfigByFlavor().bindPhoneSupported) {
            return false
        }
        val remoteConfig = RemoteConfigStorage.getRemoteConfig()
        if (TokenStorage.isWeChatUser()) {
            if (remoteConfig.needWechatBindPhone == true) {
                return !TokenStorage.isBindPhone()
            }
            return false
        }
        return remoteConfig.loginBindPhone == true && !TokenStorage.isBindPhone()
    }

    fun checkPointsAndDo(
        point: Int = 0,
        onFailure: (() -> Unit)? = null,
        onSuccess: () -> Unit = {},
    ) {
        if (!TokenStorage.canConsumePoints(point)) {
            if (onFailure != null) {
                onFailure.invoke()
            } else {
                showError(R.string.no_points)
            }
            return
        }
        onSuccess.invoke()
    }

    fun ensureLoginAndCheckPoints(
        source: String = "unknown",
        point: Int = 0,
        onLoginFailure: (String) -> Unit = {
            showToast(R.string.login_failed)
        },
        onPointsFailure: (() -> Unit)? = null,
        onSuccess: () -> Unit = {},
    ) {
        showLogin(
            source = source,
            onFailure = onLoginFailure
        ) {
            checkPointsAndDo(
                point = point,
                onFailure = onPointsFailure,
                onSuccess = onSuccess
            )
        }
    }

    /**
     * AI 聊天的时候,如果是自定义的模型, 不需要积分检验, 直接过
     * 检查用户登录状态，如果未登录则弹出登录界面，
     * 如果已登录则检查是否需要绑定手机号，如果需要则弹出绑定手机号界面
     * 顺便再检查一下积分是否足够
     *
     * Google 渠道特殊规则:
     * - 引擎自带 token 可直连 → 免登录免积分直接聊;
     * - 引擎既无 token 也无代理路由 → 登录也无意义(网关无对应代理),
     *   不弹登录, 仅提示去完成引擎设置;
     * - 引擎有代理路由(走 Go 网关, 服务端强制 JWT)→ 维持原有登录/积分检查。
     */
    fun userAIChatInputCheck(
        conversation: Conversation,
        source: String = "Unknown",
        onFailure: (String) -> Unit = {
            showError(it)
        },
        onSuccess: () -> Unit = {},
    ) {
        if (conversation.engine.canChatDirectly()) {
            onSuccess.invoke()
            return
        }
        if (FlavorType.fromName() == FlavorType.GOOGLE &&
            !conversation.engine.hasProxyRouteConfigured()
        ) {
            onFailure.invoke(resourceProvider.getString(R.string.ai_chat_engine_unavailable_toast))
            return
        }
        if (!TokenStorage.isLogin()) {
            showLoginNeedBindPhone(
                source = source,
                onSuccess = {
                    onSuccess.invoke()
                },
                onFailure = {
                    onFailure.invoke(it)
                }
            )
        } else {
            if (isNeedBindPhone()) {
                showBindPhone(
                    source = source,
                    onSuccess = {
                        onSuccess.invoke()
                    },
                    onFailure = {
                        onFailure.invoke(it)
                    }
                )
            } else {
                onSuccess.invoke()
            }
        }
    }

    /**
     * 弹出全局授权码锁屏,要求用户输入 6 位授权码。
     *
     * 行为:
     * - 若已经授权 ([AuthorizationCodeStateHolder.isAuthorized]) → 立即调 [onSuccess]
     * - 否则通过 [AppEventBus] 发出 [RequestAuthorizationCodeEvent],由全局
     *   [AuthorizationCodeStateHolder] 接管锁屏 UI;成功 / 取消 / 验证失败后回调
     *   [onSuccess] / [onFailure]
     *
     * 默认 [onFailure] 为空:取消 / 错误都已通过锁屏自身文案反馈给用户,
     * 调用方只需关心成功的回调路径。
     *
     * @param source 调用来源,用于日志 / 调试
     * @param onSuccess 解锁或首次设置成功时回调
     * @param onFailure 取消或验证失败时回调,参数为错误原因 (`null` 表示用户主动取消)
     */
    fun showAuthCode(
        source: String = "unknown",
        onSuccess: () -> Unit = {},
        onFailure: (String?) -> Unit = {},
    ) {
        if (authorizationCodeStateHolder.isAuthorized) {
            onSuccess.invoke()
            return
        }
        authorizationCodeStateHolder.ensureSubscribed()
        AppEventBus.emit(
            RequestAuthorizationCodeEvent(
                source = source,
                onSuccess = onSuccess,
                onFailure = onFailure,
            )
        )
    }
}
