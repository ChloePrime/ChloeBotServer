package chloeprime.botserver.bot.command.user

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.MinecraftUserCommand
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

/**
 * .tps
 * .好卡的（の）服
 */
object ShowTpsCommand : SimpleCommand(
    ChloeServerBot, "tps", "好卡的服", "好卡の服",
    description = "查询服务器tps"
), MinecraftUserCommand {

    @Handler
    suspend fun CommandSender.handle() {
        val response = runOnMinecraft(UserCommands.SHOW_TPS) ?: return

        val tps = ChloeServerBot.GSON.fromJson(response, ResponsePO.Tps::class.java)
        sendMessage(Resources.TPS_FORMAT.format(tps.tps, tps.mspt))
    }
}