/**
 * BotServer 使用的 HTTP API 协议相关的资源，
 * 为方便在不同项目间复制，请不要更改任何数据的类型，包名，类名，字段名和方法名。
 */
@file:Suppress("UNUSED")

package chloeprime.botserver.protocol

/**
 * @param user 发送者的QQ号
 * @param group 发送者所在群的QQ群号，-1表示当前上下文不在群聊中。
 * @param operation 请求的操作类型，见 [RequestOperations]
 * @param msg 消息内容
 */
data class RequestPO(
    @JvmField var user: Long,
    @JvmField var group: Long = -1L,
    @JvmField var operation: String,
    @JvmField var msg: String
)

inline fun RequestPO(builder: RequestPO.() -> Unit) =
    RequestPO(-1L, -1L, "", "").apply(builder)

object RequestOperations {
    /**
     * 无操作 (null)
     */
    val NO_OP = null as String?

    /**
     * '/' 开头的命令
     * 响应内容为 MC 服务器向命令执行者发送的消息 (Raw String)。
     */
    const val SERVER_COMMAND = "Command"

    /**
     * '.' 开头的命令
     * 响应内容为可由 [ResponsePO] 中的成员类表示 JSON 字符串。
     *
     * @see UserCommands 可用的 UserCommand 列表
     * @see ResponsePO 响应内容的 JSON 结构
     */
    const val USER_COMMAND = "UserCommand"
}

object UserCommands {
    /**
     * .好卡的服
     * @see [ResponsePO.Tps] 响应包的 JSON 结构
     */
    const val SHOW_TPS = "ShowTps"

    /**
     * .list
     * @see [ResponsePO.PlayerList] 响应包的 JSON 结构
     */
    const val LIST_PLAYERS = "ListPlayers"
}

object ResponsePO {
    class Tps(
        val tps: Double,
        val mspt: Double
    )

    class PlayerList(
        val names: Array<String>,
    )
}