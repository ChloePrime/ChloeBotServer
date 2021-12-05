package chloeprime.botserver.bot.command.user

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.bot.MinecraftUserCommand
import chloeprime.botserver.bot.Resources
import chloeprime.botserver.protocol.ResponsePO
import chloeprime.botserver.protocol.UserCommands
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.message.data.buildForwardMessage
import java.util.*

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

        val subject = this.subject ?: run {
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

        val sortedPlayerNames = playerList.entries.sortedBy {
            it.name.lowercase(Locale.ENGLISH)
        }

        val fullMessage = buildForwardMessage(subject) {
            subject.bot says Resources.PLAYERLIST_HEADER

            // 一次转发太多消息的话会发不出来，
            // 所以将多个玩家的信息合并为一条消息。
            val maxBatches = 12
            val batchStep = (sortedPlayerNames.size - 1) / maxBatches + 1

            val i = sortedPlayerNames.iterator()
            while (i.hasNext()) {
                val sb = StringBuilder()

                repeat(batchStep) {
                    if (i.hasNext()) {
                        sb.append(i.next().name).append('\n')
                    }
                }

                // 去除末尾的换行符
                sb.setLength(sb.length - 1)
                subject.bot says sb.toString()
            }
        }

        sendMessage(fullMessage)
    }
}