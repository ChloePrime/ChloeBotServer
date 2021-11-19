package chloeprime.botserver.bot.command

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.MinecraftUserCommand
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.buildForwardMessage

/**
 * .list
 */
object ListPlayerCommand : SimpleCommand(
    ChloeServerBot, "list", "土豆状态", "服务器状态", "破推头状态", "玩家列表",
    description = "获取玩家列表"
), MinecraftUserCommand {

    @Handler
    suspend fun CommandSender.handle() {
        val response = runOnMinecraft(UserCommands.LIST_PLAYERS) ?: return

        val subject = this.subject?: run {
            // 把 json 直接打印到控制台
            sendMessage(response)
            return
        }

        val playerList = ChloeServerBot.GSON.fromJson(response, ResponsePO.PlayerList::class.java)
        val playerCountInfo = Resources.PLAYERLIST_COUNT.format(
            playerList.entries.size, playerList.capacity
        )
        sendMessage(playerCountInfo)

        if (playerList.entries.isEmpty()) {
            return
        }

        val fullMessage = buildForwardMessage(subject) {
            subject.bot says Resources.PLAYERLIST_HEADER

            playerList.entries.sortedBy {
                it.name
            }.forEach {
                subject.bot says it.name
            }
        }
        sendMessage(fullMessage)
    }
}