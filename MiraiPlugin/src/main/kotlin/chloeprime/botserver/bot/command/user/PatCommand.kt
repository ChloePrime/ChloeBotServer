package chloeprime.botserver.bot.command.user

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.MinecraftUserCommand
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.protocol.RequestContext
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.getGroupOrNull

/**
 * .pat
 * .好卡的（の）服
 */
object PatCommand : SimpleCommand(
    ChloeServerBot, "pat", "拍一拍", "戳一戳",
    description = "查询服务器tps"
), MinecraftUserCommand {

    @Handler
    suspend fun CommandSender.handle(target: String, text: String?) {
        val userName = user?.nick ?: "控制台"
        val groupName = getGroupOrNull()?.name

        val patContext = RequestContext.Pat().apply {
            this.userName = userName
            this.groupName = groupName
            this.playerName = target
            this.text = text
        }
        // 发送请求
        val response = runOnMinecraft(UserCommands.PAT, patContext) ?: return

        val resp = ChloeServerBot.GSON.fromJson(response, ResponsePO.Pat::class.java)

        val feedbackTemplate = if (resp.errorCode == ResponsePO.Pat.OK) {
            Resources.PAT_SUCCESS
        } else when (resp.errorCode) {
            // 处理服务器返回 Error 的情况
            ResponsePO.Pat.ERR_PLAYER_NOT_ONLINE -> Resources.PAT_ERROR_TARGET_OFFLINE
            // Error^2
            else -> throw IllegalStateException(
                ".pat returned error code ${resp.errorCode} which itself is another error :("
            )
        }
        sendMessage(feedbackTemplate.format(target))
    }
}