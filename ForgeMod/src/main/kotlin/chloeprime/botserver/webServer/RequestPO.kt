package chloeprime.botserver.webServer

/**
 * @param user 发送者的QQ号
 * @param group 发送者所在群的QQ群号，-1表示当前上下文不在群聊中。
 * @param operation 请求的操作类型，见 [RequestOperations]
 * @param msg 消息内容
 * @param msgContext 一些复杂消息（例如 .pat）承载的内容
 */
data class RequestPO(
    val user: Long,
    val group: Long = -1L,
    val operation: String,
    val msg: String,
    val msgContext: String? = null
)