package chloeprime.botserver.bot.command

import chloeprime.botserver.bot.command.user.ListPlayerCommand
import chloeprime.botserver.bot.command.user.ShowTpsCommand
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister

internal fun registerBotCommands() = processBotCommands { this.register() }
internal fun unregisterBotCommands() = processBotCommands { this.unregister() }

internal fun processBotCommands(action: Command.() -> Unit) {
    ShowTpsCommand.action()
    ListPlayerCommand.action()
    ChooseServerCommand.action()
}