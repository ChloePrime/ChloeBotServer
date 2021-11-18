package chloeprime.botserver.bot.command

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister

internal fun registerBotCommands() = processBotCommands { this.register() }
internal fun unregisterBotCommands() = processBotCommands { this.unregister() }

internal fun processBotCommands(method: Command.() -> Unit) {
    ShowTpsCommand.method()
}