package chloeprime.botserver.bot

import chloeprime.botserver.bot.command.processBotCommands
import chloeprime.botserver.bot.command.registerBotCommands
import chloeprime.botserver.bot.command.unregisterBotCommands
import chloeprime.botserver.protocol.RequestPO
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel

object ChloeServerBot : KotlinPlugin(
    JvmPluginDescription(
        id = "chloeprime.botserver.bot",
        name = "ChloeServerBot",
        version = "0.0.1",
    ) {
        author("ChloePrime")
    }
) {
    internal val GSON = GsonBuilder().create()
    private val httpClient = HttpClient {
        expectSuccess = false
    }

    override fun onEnable() {
        super.onEnable()

        BotConfig.reload()
        registerBotCommands()

        val channel = globalEventChannel().filterIsInstance<MessageEvent>()
        ServerCommandSystem.registerListener(channel)

        logger.info("ChloeServerBot loaded")
    }

    override fun onDisable() {
        super.onDisable()

        unregisterBotCommands()
        httpClient.close()

        logger.info("ChloeServerBot unloaded")
    }

    /**
     * 向 MC 侧发送请求执行指令，并获取返回结果
     *
     * @return 如果无需发送反馈则返回 null
     * @throws SocketTimeoutException
     */
    internal suspend fun sendHttpRequest(
        request: RequestPO,
    ): String? {
        val httpBody = GSON.toJson(request)

        val response: HttpResponse = httpClient.post {
            port = BotConfig.minecraftPort
            body = httpBody
        }

        return when (response.status) {
            HttpStatusCode.OK -> response.receive()
            // 无权执行命令，直接无视
            HttpStatusCode.Forbidden -> return null
            // 打印状态码
            else -> response.status.toString()
        }
    }
}

internal object BotConfig : ReadOnlyPluginConfig("Config") {
    /**
     * MC 侧配套插件/mod监听的端口，
     * 不是 MC 服务器的端口
     */
    @ValueDescription("MC侧配套插件/mod监听的端口，不是MC服务器的端口")
    val minecraftPort by value(8086)
}