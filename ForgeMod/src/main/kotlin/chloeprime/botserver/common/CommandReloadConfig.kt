package chloeprime.botserver.common

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraftforge.server.permission.PermissionAPI
import chloeprime.botserver.BotServerMod
import net.minecraft.util.text.TextComponentString

/**
 * 命令 /chloebot-reloadConfig
 * 权限节点 chloebot.reload
 */
class CommandReloadConfig: CommandBase() {
    companion object {
        private const val PERMISSION = "${BotServerMod.MODID}.reload"
    }

    override fun getName() = "chloebot-reloadConfig"
    override fun getUsage(sender: ICommandSender) = "/$name"

    override fun execute(
        server: MinecraftServer, sender: ICommandSender, args: Array<out String>
    ) {
        ModConfig.reload()
        sender.sendMessage(TextComponentString("Reload Complete!"))
    }

    override fun checkPermission(server: MinecraftServer, sender: ICommandSender): Boolean {
        return if (sender is EntityPlayer) {
            super.checkPermission(server, sender) || PermissionAPI.hasPermission(sender, PERMISSION)
        } else {
            super.checkPermission(server, sender)
        }
    }
}