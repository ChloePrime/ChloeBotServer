package chloeprime.botserver.bot.command

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.bot.ServerSelector
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

/**
 * .server
 * 选择当前上下文（私聊 / 群聊）使用的服务器。
 */
object ChooseServerCommand : SimpleCommand(
    ChloeServerBot, "server",
    description = "设置当前聊天上下文使用的 MC 服务器"
) {
    @Handler
    suspend fun CommandSender.handle(selection: String? = null) {
        if (selection == null) {
            ServerSelector.put(this, "🐢🐢🐢")
            sendMessage(Resources.CHANGE_SERVER_RESET)
            return
        }
        val feedback: String =
            if (!ServerSelector.hasOverrideForServerName(selection)) {
                Resources.CHANGE_SERVER_NONEXIST
            } else {
                ServerSelector.put(this, selection)
                Resources.CHANGE_SERVER_SUCCESS
            }
        sendMessage(feedback.format(selection))
    }
}