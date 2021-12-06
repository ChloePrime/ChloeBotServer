package chloeprime.botserver.bot.command.user

import chloeprime.botserver.bot.ChloeServerBot
import chloeprime.botserver.protocol.RequestContext.Pat.Companion.Actions
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

/**
 * .pat
 */
object PatCommand: SimpleCommand(
    ChloeServerBot, "pat", "拍一拍", "戳一戳",
    description = "与服务器中玩家进行交互"
), InteractionCommand {
    override val animation = Actions.SHAKE_SCREEN
    override val sound = "minecraft:entity.creeper.hurt"
    override val action = "拍了拍"

    @Handler
    suspend fun CommandSender.handle(target: String, vararg text: String) {
        handle0(target, *text)
    }
}

/**
 * .prpr
 */
object PrprCommand: SimpleCommand(
    ChloeServerBot, "prpr", "舔一舔",
    description = "与服务器中玩家进行交互"
), InteractionCommand {
    override val sound = "customnpcs:human.girl.villager.heh"
    override val action = "舔了舔"

    @Handler
    suspend fun CommandSender.handle(target: String, vararg text: String) {
        handle0(target, *text)
    }
}

/**
 * .tell
 */
object TellCommand: SimpleCommand(
    ChloeServerBot, "tell", "msg", "m",
    description = "与服务器中玩家进行交互"
), InteractionCommand {
    override val sound = "customnpcs:human.female.villager.uhuh"
    override val action = ""

    @Handler
    suspend fun CommandSender.handle(target: String, vararg text: String) {
        handle0(target, *text)
    }
}