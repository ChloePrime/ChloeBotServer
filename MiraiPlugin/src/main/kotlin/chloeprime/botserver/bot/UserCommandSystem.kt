package chloeprime.botserver.bot

import chloeprime.botserver.protocol.RequestOperations
import chloeprime.botserver.protocol.RequestPO
import chloeprime.botserver.protocol.UserCommands
import io.ktor.network.sockets.*
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender

interface MinecraftUserCommand : Command {
    /**
     * @param command 可用选项见 [UserCommands]
     * @return JSON 字符串
     */
    suspend fun CommandSender.runOnMinecraft(
        command: String,
        context: Any? = null
    ): String? {
        val user: Long
        val group: Long
        val commandUser = this.user

        if (commandUser == null) {
            // 执行者为 Mirai 控制台
            user = -1L
            group = -1L
        } else {
            user = commandUser.id
            group = if (this is MemberCommandSender) this.group.id else -1L
        }

        val request = RequestPO(user, group, RequestOperations.USER_COMMAND, command)
        if (context != null) {
            request.msgContext = ChloeServerBot.GSON.toJson(context)
        }

        return try {
            ServerSelector.get(this).sendRequestTo(request)
        } catch (ex: SocketTimeoutException) {
            sendMessage(Resources.SOCKET_TIMEOUT_MSG)
            null
        }
    }
}
