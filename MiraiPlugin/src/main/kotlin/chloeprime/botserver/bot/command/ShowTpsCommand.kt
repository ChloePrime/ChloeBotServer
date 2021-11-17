package chloeprime.botserver.bot.command

import chloeprime.botserver.bot.ChloeServerBot
import net.mamoe.mirai.console.command.SimpleCommand

object ShowTpsCommand: SimpleCommand(
    ChloeServerBot,
    primaryName = "tps",
    secondaryNames = arrayOf("好卡的服", "好卡の服")
) {
}