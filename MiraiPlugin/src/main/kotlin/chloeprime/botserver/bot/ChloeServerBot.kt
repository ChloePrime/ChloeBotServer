package chloeprime.botserver.bot

import chloeprime.botserver.protocol.RequestOperations
import chloeprime.botserver.protocol.RequestPO
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.*

object ChloeServerBot : KotlinPlugin(
    JvmPluginDescription(
        id = "chloeprime.botserver.bot.ChloeServerBot",
        name = "ChloeServerBot",
        version = "0.0.1",
    ) {
        author("ChloePrime")
    }
) {
    internal val GSON = GsonBuilder().create()
    private val httpClient = HttpClient()

    override fun onEnable() {
        super.onEnable()

        logger.info("ChloeServerBot loaded")
        BotConfig.reload()

        val channel = globalEventChannel().filterIsInstance<MessageEvent>()
        channel.subscribeAlways(this::onMessage)
    }

    override fun onDisable() {
        super.onDisable()

        logger.info("ChloeServerBot unloaded")
        httpClient.close()
    }

    private suspend fun onMessage(e: MessageEvent) {
        val sender = e.sender.id

        val (operation, msgContent) = parseMessage(e.message)
        if (operation == null) {
            return
        }

        val contact: Contact
        val request: RequestPO
        if (e is GroupEvent) {
            contact = e.group
            request = RequestPO {
                user = sender
                group = e.group.id
                this.operation = operation
                msg = msgContent
            }
        } else {
            contact = e.sender
            request = RequestPO {
                user = sender
                this.operation = operation
                this.msg = msgContent
            }
        }

        try {
            withContext(Dispatchers.IO) {
                val response = sendHttpRequest(e.message.content, contact, request) ?: return@withContext
                contact.sendMessage(getFeedback(request, response))
            }
        } catch (ex: Exception) {
            contact.sendMessage("[Error] $ex")
        }
    }

    /**
     * 获取消息的行为和内容。
     *
     * 可用的行为列表见 [RequestOperations]
     * 消息的内容指该消息去除指令前缀后所剩余的内容 (例如去除了开头 '/' 的 MC 服务器指令)
     *
     * @return (operation, content), operation 为 null 则表示这条消息不是 bot 指令。
     * @see RequestOperations
     */
    private fun parseMessage(message: MessageChain): Pair<String?, String> {

        if (!isPlainMessage(message)) {
            return Pair(null, "")
        }

        val plainMessage = message.content
        val operation: String?
        val content: String

        when (plainMessage[0]) {
            '/' -> {
                operation = RequestOperations.SERVER_COMMAND
                content = plainMessage.substring(1)
            }
            '.' -> {
                val translatedBody = UserCommandTranslationTable[plainMessage.substring(1)]
                operation = if (translatedBody != null) {
                    RequestOperations.USER_COMMAND
                } else {
                    RequestOperations.NO_OP
                }
                content = translatedBody ?: ""
            }
            else -> {
                operation = RequestOperations.NO_OP
                content = ""
            }
        }

        return Pair(operation, content)
    }

    /**
     * 判断消息是否以纯文本开头
     */
    private fun isPlainMessage(message: MessageChain): Boolean {
        return message.firstIsInstanceOrNull<MessageContent>() is PlainText
    }

    /**
     * 向 MC 侧发送请求执行指令，并获取返回结果
     * @return 如果无需发送反馈则返回 null
     */
    internal suspend fun sendHttpRequest(
        message: String,
        responseTarget: Contact,
        request: RequestPO,
    ): String? {
        val httpBody = GSON.toJson(request)

        try {
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
        } catch (ex: SocketTimeoutException) {
            val feedback = if (message == "/stop") {
                "服务器开始关闭"
            } else {
                "连接超时，服务器未响应"
            }
            responseTarget.sendMessage(feedback)
        }

        return null
    }

    private fun getFeedback(request: RequestPO, response: String): String {
        return when (request.operation) {
            RequestOperations.SERVER_COMMAND -> response
            RequestOperations.USER_COMMAND -> getUserCommandFeedback(request, response)
            else -> throw IllegalArgumentException("Unknown request operation: ${request.operation}")
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