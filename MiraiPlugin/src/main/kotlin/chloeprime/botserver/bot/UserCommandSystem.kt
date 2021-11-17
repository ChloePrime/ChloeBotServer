package chloeprime.botserver.bot

import chloeprime.botserver.protocol.RequestOperations
import chloeprime.botserver.protocol.RequestPO
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands

/**
 * QQ 消息与内部交互用 UserCommand 名称的转化列表
 */
object UserCommandTranslationTable : MutableMap<String, String> by linkedMapOf() {
    init {
        this["好卡的服"] = UserCommands.SHOW_TPS
    }
}

suspend fun runUserCommandOnMinecraft(
    user: Long, group: Long, commandType: String
) {
    val request = RequestPO {
        this.user = user
        this.group = group
        operation = RequestOperations.USER_COMMAND
        msg = commandType
    }
    //val response = ChloeServerBot.sendHttpRequest()
}

fun getUserCommandFeedback(request: RequestPO, response: String): String {
    return when (request.msg) {
        UserCommands.SHOW_TPS -> {
            val tps = ChloeServerBot.GSON.fromJson(response, ResponsePO.Tps::class.java)
            val tpsStr = String.format("%.2f", tps.tps)
            val msptStr = String.format("%.2f", tps.mspt)
            "土豆性能状态: $tpsStr tps ($msptStr mspt)"
        }
        else -> throw IllegalArgumentException("Unknown user command: ${request.msg}")
    }
}