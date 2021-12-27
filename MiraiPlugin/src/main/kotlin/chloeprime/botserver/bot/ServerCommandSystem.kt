package chloeprime.botserver.bot

import chloeprime.botserver.protocol.RequestOperations
import chloeprime.botserver.protocol.RequestPO
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.*

internal object ServerCommandSystem {

    suspend fun onServerCommand(e: MessageEvent) {

        if (!isValidCommand(e.message)) {
            return
        }

        if (!e.toCommandSender().hasPermission(ChloeServerBot.Permissions.RUN_SERVER_COMMAND)) {
            return
        }

        val user = e.sender.id

        val feedbackTarget: Contact
        val group: Long
        val mcServer: MinecraftBotServer
        val command = e.message.content.substring("/".length)

        if (e is GroupEvent) {
            feedbackTarget = e.group
            group = e.group.id
            mcServer = ServerSelector.getByGroup(e.group)
        } else {
            feedbackTarget = e.sender
            group = -1L
            mcServer = ServerSelector.getByUser(e.sender)
        }

        try {
            withContext(Dispatchers.IO) {
                val feedback = runServerCommandOnMinecraft(user, group, mcServer, command) ?: return@withContext
                feedbackTarget.sendMessage(feedback.ifEmpty { "指令执行成功" })
            }
        } catch (ex: Exception) {
            feedbackTarget.sendMessage("[Error] $ex")
        }
    }

    /**
     * 判断消息是否以纯文本开头
     */
    private fun isValidCommand(message: MessageChain): Boolean {
        val startsWithText = message.firstIsInstanceOrNull<MessageContent>() is PlainText
        return startsWithText && message.content.startsWith('/')
    }


    /**
     * @param command 不带 '/' 的 Minecraft 服务器命令。
     * @return 发送给 QQ 用户的反馈消息
     */
    private suspend fun runServerCommandOnMinecraft(
        user: Long, group: Long, mcServer: MinecraftBotServer, command: String
    ): String? {
        val request = RequestPO(user, group, RequestOperations.SERVER_COMMAND, command)

        return try {
            mcServer.sendRequestTo(request)
        } catch (ex: SocketTimeoutException) {
            if (command == "stop" || command == "restart") {
                Resources.SERVER_STOP_MSG
            } else {
                Resources.SOCKET_TIMEOUT_MSG
            }
        }
    }
}