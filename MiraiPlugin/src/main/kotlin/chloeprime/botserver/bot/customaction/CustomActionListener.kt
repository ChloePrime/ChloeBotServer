package chloeprime.botserver.bot.customaction

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.ChloeServerBot.Permissions.CUSTOM_MINECRAFT_ACTION
import chloeprime.botserver.bot.command.user.InteractiveCommand
import chloeprime.botserver.bot.runMinecraftUserCommand
import chloeprime.botserver.protocol.RequestContext
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

object CustomActionListener {
    /**
     * Channel 过滤器
     */
    fun filter(it: MessageEvent) = it.message.content.startsWith('#')

    /**
     * 消息处理代码
     */
    suspend fun onMessage(e: MessageEvent) {
        val sender = e.toCommandSender()
        if (!sender.hasPermission(CUSTOM_MINECRAFT_ACTION)) {
            return
        }

        // e.message should startswith '#'
        val message = e.message.content.substring(1)
        val feedbackTarget = if (e is GroupEvent) e.group else e.sender

        // 构造请求内容
        val patContext = parse(message) ?: return
        patContext.apply {
            userName = e.sender.nameCardOrNick
            groupName = if (e is GroupEvent) e.group.name else null
            animation = 0
        }

        // 发送 HTTP 请求
        val responseText = sender.runMinecraftUserCommand(
            UserCommands.PAT,
            patContext
        ) ?: return

        // 处理响应
        val resp = ChloeServerBot.GSON.fromJson(responseText, ResponsePO.Pat::class.java)
        val action = patContext.actionOverload
        val target = patContext.playerName
        val feedback = InteractiveCommand.getFeedback(action, resp)
        feedbackTarget.sendMessage(feedback.format(action, target))
    }

    private fun parse(msg: String): RequestContext.Pat? {
        val (action, target) = tokenize(msg)
        if (action.isEmpty() || target == null) {
            return null
        }

        val correctedAction = if (action[0].code < 128) {
            // English
            if (action.endsWith('e')) {
                action + "d"
            } else {
                action + "ed"
            }
        } else {
            // 中文
            if (action.length == 1) {
                // 中文单字：xxx抱了抱yyy
                action + "了" + action
            } else {
                // 中文多字动词：xxx拥抱了yyy
                action + "了"
            }
        }

        return RequestContext.Pat().apply {
            playerName = target
            actionOverload = correctedAction
        }
    }

    private fun tokenize(msg: String): Pair<String, String?> {
        var action = ""
        var target = null as String?

        val trimmed = msg.trim()
        for (i0 in msg.indices) {
            val i = msg.length - i0 - 1
            val char = trimmed[i]
            if (char.code >= 128 || char.isWhitespace()) {
                action = trimmed.substring(0, i + 1).trim()
                target = trimmed.substring(i + 1)
                break
            }
        }

        return Pair(action, target)
    }
}