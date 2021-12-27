package chloeprime.botserver.bot.command.user

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.MinecraftUserCommand
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.protocol.RequestContext
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.contact.nameCardOrNick

/**
 * .pat
 */
interface InteractiveCommand : MinecraftUserCommand {

    suspend fun CommandSender.handle0(target: String, vararg text: String) {
        val fullText = text.ifEmpty { null }?.joinToString(" ")
        val userName = user?.nameCardOrNick ?: "控制台"
        val groupName = getGroupOrNull()?.name

        val patContext = RequestContext.Pat().apply {
            this.userName = userName
            this.groupName = groupName
            this.playerName = target
            this.animation = this@InteractiveCommand.animation
            this.actionOverload = this@InteractiveCommand.action
            this.soundFx = this@InteractiveCommand.sound
            this.text = fullText
        }
        // 发送请求
        val response = runOnMinecraft(UserCommands.PAT, patContext) ?: return

        val resp = ChloeServerBot.GSON.fromJson(response, ResponsePO.Pat::class.java)

        val feedbackTemplate = getFeedback(action, resp)
        sendMessage(feedbackTemplate.format(action, target))
    }

    /**
     * 视觉效果
     * @see RequestContext.Pat.Animations
     */
    val animation
        get() = 0

    /**
     * 动作名称
     */
    val action: String?
        get() = null

    /**
     * Minecraft 音效路径
     */
    val sound: String?
        get() = "minecraft:entity.experience_orb.pickup"

    companion object {
        fun getFeedback(action:String?, resp: ResponsePO.Pat): String {
            return if (resp.errorCode == ResponsePO.Pat.OK) {
                if (action?.isEmpty() == false) {
                    Resources.PAT_SUCCESS
                } else {
                    Resources.TELL_SUCCESS
                }
            } else when (resp.errorCode) {
                // 处理服务器返回 Error 的情况
                ResponsePO.Pat.ERR_PLAYER_NOT_ONLINE -> Resources.PAT_ERROR_TARGET_OFFLINE
                // Error^2
                else -> throw IllegalStateException(
                    ".pat returned error code ${resp.errorCode} which itself is another error :("
                )
            }
        }
    }
}