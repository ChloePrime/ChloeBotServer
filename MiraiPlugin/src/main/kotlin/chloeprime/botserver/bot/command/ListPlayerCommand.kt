package chloeprime.botserver.bot.command

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.MinecraftUserCommand
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

/**
 * .list
 */
object ListPlayerCommand : SimpleCommand(
    ChloeServerBot, "list",
    description = "查询服务器tps"
), MinecraftUserCommand {

    @Handler
    suspend fun CommandSender.handle() {
        val response = runOnMinecraft(UserCommands.LIST_PLAYERS) ?: return

        val tps = ChloeServerBot.GSON.fromJson(response, ResponsePO.Tps::class.java)
        sendMessage(String.format(Resources.TPS_FORMAT, tps.tps, tps.mspt))
    }
}