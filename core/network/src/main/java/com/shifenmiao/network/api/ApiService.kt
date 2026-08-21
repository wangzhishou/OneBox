package com.shifenmiao.network.api

import com.shifenmiao.core.constants.Constants
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.CategoryList
import com.shifenmiao.model.DataItemLIst
import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.ai.AgentItem
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.ai.ChatPromptItem
import com.shifenmiao.model.app.UpdateLogs
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.model.blog.FeedbackRequest
import com.shifenmiao.model.blog.Tag
import com.shifenmiao.network.model.comment.Comment
import com.shifenmiao.network.model.comment.CommentEnvelope
import com.shifenmiao.network.model.comment.CommentListResponse
import com.shifenmiao.network.model.comment.CreateCommentRequest
import com.shifenmiao.network.model.comment.UpdateCommentRequest
import com.shifenmiao.model.common.AnnouncementItem
import com.shifenmiao.model.common.DataList
import com.shifenmiao.model.common.DataObject
import com.shifenmiao.model.common.Status
import com.shifenmiao.model.moderation.SensitiveWordCheckRequest
import com.shifenmiao.model.moderation.SensitiveWordCheckResponse
import com.shifenmiao.model.pay.alipay.AlipayResult
import com.shifenmiao.model.pay.alipay.PayEncodeParamResult
import com.shifenmiao.model.pay.alipay.PayParams
import com.shifenmiao.model.pay.google.GooglePayVerifyRequest
import com.shifenmiao.model.pay.google.GooglePlayProduct
import com.shifenmiao.model.pay.wechat.WechatPayQueryRequest
import com.shifenmiao.model.pay.wechat.WechatPrepayRequest
import com.shifenmiao.model.pay.wechat.WechatPrepayResponse
import com.shifenmiao.model.points.ConsumePoints
import com.shifenmiao.model.points.RewardPoints
import com.shifenmiao.model.remote.RemoteConfigListResponse
import com.shifenmiao.model.user.Login
import com.shifenmiao.model.user.LoginRequest
import com.shifenmiao.model.user.RegisterRequest
import com.shifenmiao.model.user.SMSRequest
import com.shifenmiao.model.user.UserInviteRequest
import com.shifenmiao.model.user.GoogleLoginRequest
import com.shifenmiao.model.user.VerifyCodeRequest
import com.shifenmiao.model.user.WechatLoginRequest
import com.shifenmiao.model.user.ForgotPasswordRequest
import com.shifenmiao.model.user.ResetPasswordRequest
import com.shifenmiao.network.BuildConfig
import com.shifenmiao.storage.TokenStorage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * val retrofit = Retrofit.Builder()
 *     .baseUrl("https://your_base_url/")
 *     .addConverterFactory(GsonConverterFactory.create())
 *     .build()
 *
 * val apiService = retrofit.create(ApiService::class.java)
 */
interface ApiService {
    /**
     * 增量同步条目。
     * @param updatedAfter ISO8601 时间戳，只拉取 updated_at > updatedAfter 的数据。
     * @param listType 列表类型，对应 list_types.type_id。
     * @param category 分类 documentId（Strapi v5 稳定标识），null/空表示不过滤。
     *   服务端兼容数字 id，但数字 id 重发后会漂移，客户端只应传 documentId。
     */
    @GET("api/items/sync")
    suspend fun fetchItemsSync(
        @Query("updatedAfter") updatedAfter: String?,
        @Query("listType") listType: Int,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = Constants.PAGE_SIZE,
        @Query("vipLevel") vipLevel: Int = TokenStorage.getUserVipLevel(),
        @Header("X-Force-Refresh") forceRefresh: Boolean? = null,
    ): Response<DataItemLIst>

    @GET("api/blogs")
    suspend fun fetchBlogs(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("blogType") blogType: Int = 1,
    ): Response<DataList<BlogItem>>

    @GET("api/blogs/{id}")
    suspend fun fetchBlog(
        @Path("id") id: Int,
        @Query("blogType") blogType: Int? = null
    ): Response<DataObject<BlogItem>>

    @GET("api/blogs/tags")
    suspend fun fetchBlogTags(
    ): Response<DataList<Tag>>

    @POST("api/blogs/feedback")
    suspend fun createFeedback(
        @Body feedbackRequest: FeedbackRequest
    ): Response<BlogItem>


    /**
     * curl -X POST http://your-strapi-url/api/upload \
     *   -F "files=@/path/to/image.jpg" \
     *   -F "ref=api::restaurant.restaurant" \
     *   -F "refId=1" \
     *   -F "field=cover"
     */
    @Multipart
    @POST("api/upload")
    fun uploadFile(
        @Part file: MultipartBody.Part? = null,
        @Part("ref") ref: RequestBody? = null,
        @Part("refId") refId: RequestBody? = null,
        @Part("field") field: RequestBody? = null
    ): Call<List<StrapiImage>>


    /**
     * agent 详情。:id 服务端兼容数字 id 与 documentId，客户端统一传 documentId（空时降级数字 id）。
     */
    @GET("api/agents/{id}")
    suspend fun fetchAgent(
        @Path("id") id: String
    ): Response<AgentItem>

    @PUT("api/agents")
    suspend fun updateAgent(
        @Body agent: Agent
    ): Response<AgentItem>

    /**
     * prompt 详情。:id 服务端兼容数字 id 与 documentId，客户端统一传 documentId（空时降级数字 id）。
     */
    @GET("api/prompts/{id}")
    suspend fun fetchPrompt(
        @Path("id") id: String
    ): Response<ChatPromptItem>

    @PUT("api/prompts")
    suspend fun updatePrompt(
        @Body prompt: ChatPrompt
    ): Response<ChatPromptItem>

    /**
     * 获取 AI 引擎和模型配置
     * @param configVersion 当前本地配置版本号，用于增量更新
     */
    @GET("api/ai/config")
    suspend fun fetchAiConfig(
        @Query("configVersion") configVersion: String? = null
    ): Response<com.shifenmiao.model.ai.config.AiConfigResponse>

    /**
     * 提交 AI 模型到后台数据库
     */
    @POST("api/ai/models")
    suspend fun submitAiModels(
        @Body request: com.shifenmiao.model.ai.SubmitModelsRequest
    ): Response<Any>

    @GET("/api/announcements")
    suspend fun fetchAnnouncements(
        @Query("filters[type]") type: String? = null
    ): Response<DataList<AnnouncementItem>>


    @GET("/api/update-logs")
    suspend fun checkUpdate(
        @Query("filters[versionCode][\$gt]") versionCode: Int = 100
    ): Response<UpdateLogs>

    @GET("api/categories")
    suspend fun fetchCategories(
        @Query(UrlConstants.NEED_CACHE_PARAM_NAME) needCache: Int = 720,
    ): Response<CategoryList>

    /**
     * 增量同步分类。
     * @param updatedAfter ISO8601 时间戳，只拉取 updated_at > updatedAfter 的数据。
     */
    @GET("api/categories/sync")
    suspend fun fetchCategoriesSync(
        @Query("updatedAfter") updatedAfter: String?,
        @Header("X-Force-Refresh") forceRefresh: Boolean? = null,
    ): Response<CategoryList>

    @GET("user/delete")
    suspend fun loginOut(
        @Query("userId") userId: Int = 0
    ): Response<Status>

    @POST("user/send-code")
    suspend fun sendCode(
        @Body smsRequest: SMSRequest
    ): Response<Status>

    @POST("api/auth/send-code")
    suspend fun sendCodeNoCheck(
        @Body smsRequest: SMSRequest
    ): Response<Status>

    @POST("user/bind-phone")
    suspend fun bindPhone(
        @Body verifyCodeRequest: VerifyCodeRequest
    ): Response<Login>

    @POST("api/auth/phone")
    suspend fun loginByPhone(
        @Body verifyCodeRequest: VerifyCodeRequest
    ): Response<Login>

    @GET("api/remote-configs")
    suspend fun updateRemoteConfig(
        @Query("versionCode") versionCode: Int = BuildConfig.VersionCode.toInt(),
        @Query("channel") channel: String = BuildConfig.FLAVOR
    ): Response<RemoteConfigListResponse>

    @POST("api/content/check-sensitive")
    suspend fun checkSensitiveWords(
        @Body request: SensitiveWordCheckRequest
    ): Response<SensitiveWordCheckResponse>

    /**
     * 使用@Body注解将一个Item对象作为请求参数。
     * Retrofit会将此对象转换为JSON格式并将其包含在请求体中。
     */
    @GET("wechat/login")
    suspend fun wechatPublicAccountLogin(
        @Query("code") code: Int
    ): Response<Login>

    @POST("api/wechat-login")
    suspend fun wechatLogin(@Body loginRequest: WechatLoginRequest): Response<Login>

    @POST("api/google-login")
    suspend fun googleLogin(@Body loginRequest: GoogleLoginRequest): Response<Login>

    @POST("api/auth/local")
    suspend fun login(@Body loginRequest: LoginRequest): Response<Login>

    @POST("api/register")
    suspend fun register(@Body loginRequest: RegisterRequest): Response<Login>

    @POST("api/login")
    suspend fun loginUnified(@Body loginRequest: LoginRequest): Response<Login>

    @POST("api/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Status>

    @POST("api/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Status>

    @POST("user/invite")
    suspend fun applyInvitationCode(@Body userInviteRequest: UserInviteRequest): Response<Login>

    @POST("user/consume-points")
    suspend fun consumePoints(@Body consumePoints: ConsumePoints): Response<Login>

    @POST("user/reward-points")
    suspend fun rewardPoints(@Body rewardPoints: RewardPoints): Response<Login>


    @POST("alipay/alipay-return")
    suspend fun alipayReturnVerify(
        @Body alipayResult: AlipayResult
    ): Response<Login>

    @POST("alipay/payment")
    suspend fun getAliPayOrder(
        @Body payParams: PayParams
    ): Response<PayEncodeParamResult>

    @POST("wechat/pay/transaction")
    suspend fun wechatReturnVerify(
        @Body payRequest: WechatPayQueryRequest
    ): Response<Login>

    @POST("wechat/pay/prepare")
    suspend fun wechatPayOrder(
        @Body payParams: WechatPrepayRequest
    ): Response<WechatPrepayResponse>

    // ─────────────── Google Play Billing (google 渠道积分商品) ───────────────

    /** google 渠道商品目录: productId 与积分的映射 */
    @GET("google/pay/products")
    suspend fun googlePlayProducts(): Response<List<GooglePlayProduct>>

    /** 服务端验单(Google Play Developer API)并幂等发放积分, 返回最新用户信息 */
    @POST("google/pay/verify")
    suspend fun googlePlayVerify(
        @Body request: GooglePayVerifyRequest
    ): Response<Login>


    // ─────────────── 评论 (strapi-plugin-comments via go-proxy) ───────────────
    //
    // 路径对齐 go-proxy 任务三输出:
    //   GET    /api/comments/{documentId}                 -> 列表 (允许游客)
    //   POST   /api/comments/{documentId}                 -> 发表评论 (JWT)
    //   POST   /api/comments/{documentId}/{parentId}      -> 回复评论 (JWT)
    //   DELETE /api/comments/admin/{commentId}            -> 管理员删除 (JWT+Admin)
    //   PUT    /api/comments/admin/{commentId}            -> 管理员屏蔽 (JWT+Admin)

    @GET("api/comments/{documentId}")
    suspend fun listComments(
        @Path("documentId") documentId: String,
        @Query("uid") uid: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): Response<CommentListResponse>

    @POST("api/comments/{documentId}")
    suspend fun createComment(
        @Path("documentId") documentId: String,
        @Query("uid") uid: String? = null,
        @Body body: CreateCommentRequest,
    ): Response<CommentEnvelope>

    @POST("api/comments/{documentId}/{parentId}")
    suspend fun replyComment(
        @Path("documentId") documentId: String,
        @Path("parentId") parentId: Int,
        @Query("uid") uid: String? = null,
        @Body body: CreateCommentRequest,
    ): Response<CommentEnvelope>

    @DELETE("api/comments/admin/{commentId}")
    suspend fun adminDeleteComment(
        @Path("commentId") commentId: Int,
    ): Response<CommentEnvelope>

    @PUT("api/comments/admin/{commentId}")
    suspend fun adminUpdateComment(
        @Path("commentId") commentId: Int,
        @Body body: UpdateCommentRequest,
    ): Response<CommentEnvelope>
}
