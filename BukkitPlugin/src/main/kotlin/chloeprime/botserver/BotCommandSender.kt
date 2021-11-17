package chloeprime.botserver

import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.ChatColor
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.permissions.*
import taboolib.common.platform.function.warning
import java.util.*

class BotCommandSender(
    private val server: Server
): PermissibleBase(FakeOp), CommandSender {

    /**
     * 获取 message 后会阻止后续的 sendMessage 写入
     */
    internal fun getMessage(): String {
        disabled = true
        return stringBuilder.trim().toString()
    }

    private fun appendMessage(message: String) {
        if (disabled) {
            return
        }
        stringBuilder.append(ChatColor.stripColor(message)).append('\n')
    }

    override fun sendMessage(message: String) {
        appendMessage(message)
    }

    override fun sendMessage(vararg messages: String) {
        if (disabled) {
            return
        }
        for (message in messages) {
            appendMessage(message)
        }
    }

    override fun sendMessage(p0: UUID?, message: String) {
        sendMessage(message)
    }

    override fun sendMessage(p0: UUID?, vararg messages: String) {
        sendMessage(*messages)
    }

    override fun getServer() = server
    override fun getName() = "Chloe's Bot"
    override fun spigot(): CommandSender.Spigot = spigot

    @Volatile
    private var disabled = false
    private val stringBuilder = StringBuffer()
    private val spigot = Spigot()

    private inner class Spigot : CommandSender.Spigot() {
        /**
         * 避免 Overload 二义性导致潜在的递归函数
         */
        private fun sendMessage0(component: BaseComponent) {
            if (disabled) {
                return
            }
            this@BotCommandSender.sendMessage(component.toPlainText())
        }

        override fun sendMessage(component: BaseComponent) =
            sendMessage0(component)

        override fun sendMessage(vararg components: BaseComponent) {
            if (disabled) {
                return
            }
            for (component in components) {
                sendMessage0(component)
            }
        }

        override fun sendMessage(sender: UUID?, component: BaseComponent) =
            sendMessage(component)

        override fun sendMessage(sender: UUID?, vararg components: BaseComponent) =
            sendMessage(*components)
    }
}

internal object FakeOp: ServerOperator {
    override fun isOp() = true

    override fun setOp(p0: Boolean) {
        if (!p0) {
            warning("Trying to deop the Chloe Bot, which is not supported.")
        }
    }
}
