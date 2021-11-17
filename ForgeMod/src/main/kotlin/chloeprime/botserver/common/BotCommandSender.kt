package chloeprime.botserver.common

import net.minecraft.command.ICommandSender
import net.minecraft.network.rcon.RConConsoleSource
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.ITextComponent
import java.util.regex.Pattern

class BotCommandSender(
    server: MinecraftServer
): RConConsoleSource(server), ICommandSender {

    /**
     * 获取 message 后会阻止后续的 sendMessage 写入
     */
    internal fun getMessage(): String {
        disabled = true
        return stringBuilder.trim().toString()
    }

    fun sendMessage(message: String) {
        if (disabled) {
            return
        }

        val plainMessage = message.stripStyle()
        stringBuilder.append(plainMessage)
        // 在末尾加上换行
        if (!plainMessage.endsWith('\n')) {
            stringBuilder.append('\n')
        }
    }

    override fun sendMessage(component: ITextComponent) {
        sendMessage(component.formattedText)
    }

    override fun getName() = ModConfig.INSTANCE.botName
    override fun sendCommandFeedback() = server!!.sendCommandFeedback()

    @Volatile
    private var disabled = false
    private val stringBuilder = StringBuffer()

    companion object {
        private val STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[\u0000-\u007F]")!!

        private fun String.stripStyle() =
            STRIP_COLOR_PATTERN.matcher(this).replaceAll("")
    }
}