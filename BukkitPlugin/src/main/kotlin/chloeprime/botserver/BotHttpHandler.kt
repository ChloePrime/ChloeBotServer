package chloeprime.botserver

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.apache.logging.log4j.LogManager
import org.bukkit.Bukkit
import taboolib.common.platform.function.submit
import taboolib.common.reflect.Reflex.Companion.unsafeInstance
import taboolib.common.util.sync
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * 解析发往 Bot Server 的 HTTP 包，
 * 判断发送人/群是否位于白名单中，如果是则执行指令。
 */
class BotHttpHandler : HttpHandler {

    /**
     * Handle the given request and generate an appropriate response.
     * See [HttpExchange] for a description of the steps
     * involved in handling an exchange.
     * @param httpExchange the exchange containing the request from the
     * client and used to send the response
     * @throws NullPointerException if exchange is `null`
     */
    override fun handle(httpExchange: HttpExchange?) {
        if (httpExchange == null) {
            throw NullPointerException()
        }

        if (BotServerPlugin.forceLocalHost) {
            val invalidServer = !httpExchange.localAddress.address.isLoopbackAddress
            val invalidClient = !httpExchange.remoteAddress.address.isLoopbackAddress

            if (invalidServer || invalidClient) {
                httpExchange.close()
                return
            }
        }


        if (httpExchange.requestMethod != "POST") {
            LOGGER.warn("Request to the Bot Server should use the POST method.")
            httpExchange.responseHeaders.add("Allow", "POST")
            httpExchange.sendResponseHeaders(405, 0)
            httpExchange.close()
            return
        }
        // 解析请求
        val request = try {
            httpExchange.requestBody.bufferedReader().use { reader ->
                GSON.fromJson(reader, RequestPO::class.java)
            }
        } catch (ex: JsonSyntaxException) {
            // 如果发送过来的文本不是合法的的JSON的话
            LOGGER.error("Malformed json sent to Bot Server", ex)
            httpExchange.sendResponseHeaders(400, 0)
            httpExchange.close()
            return
        }
        // 命令必须以斜杠开头
        if (!request.msg.startsWith('/')) {
            httpExchange.sendResponseHeaders(200, 0L)
            httpExchange.close()
            return
        }
        if (!isAuthorized(request)) {
            httpExchange.sendResponseHeaders(200, noPermMessage.size.toLong())
            httpExchange.responseBody.write(noPermMessage)
            httpExchange.close()
            return
        }

        handle0(httpExchange, request)
    }

    /**
     * 判断当前用户是否有权限执行命令
     */
    private fun isAuthorized(request: RequestPO): Boolean {
        return with(BotServerPlugin) {
            synchronized(authorizeDataLock) {
                request.user in authorizedUsers || request.group in authorizedGroups
            }
        }
    }

    private fun handle0(httpExchange: HttpExchange, request: RequestPO) {
        // 执行命令
        val command = request.msg.substring(1)
        Objects::class.java.unsafeInstance()
        // 以下的代码需要切换到主线程执行
        submit {
            val sender = BotCommandSender(Bukkit.getServer())
            val startTime = System.currentTimeMillis()

            try {
                Bukkit.dispatchCommand(sender, command)
            } catch (ex: Exception) {
                sender.sendMessage("命令执行过程中遇到了未知的错误: $ex")
            }

            ASYNC.execute {
                val delayTime = BotServerPlugin.commandResponseWaitTime -
                        // 执行命令本身消耗的时间
                        max(0, System.currentTimeMillis() - startTime)

                if (delayTime > 0) {
                    Thread.sleep(delayTime)
                }

                // 发送命令执行结果
                val responseBody = sender.getMessage().toByteArray(Charsets.UTF_8)
                httpExchange.sendResponseHeaders(200, responseBody.size.toLong())
                httpExchange.responseBody.write(responseBody)
                httpExchange.close()
            }
        }
    }

    /**
     * @param user 发送者的QQ号
     * @param group 发送者所在群的QQ群号，-1表示当前上下文不在群聊中。
     * @param msg 消息内容
     */
    data class RequestPO(
        @JvmField var user: Long,
        @JvmField var group: Long = -1L,
        @JvmField var msg: String
    )

    companion object {
        @JvmStatic
        private val LOGGER = LogManager.getLogger()
        private val GSON = GsonBuilder().create()
        private val ASYNC: Executor = run {
            val threadFactory = ThreadFactoryBuilder()
                .setNameFormat("bot-response-pool-%d")
                .build()

            ThreadPoolExecutor(
                2, 4, 0L, TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(128), threadFactory, ThreadPoolExecutor.AbortPolicy()
            )
        }
        private val noPermMessage = "你没有权限执行服务器命令哦".toByteArray(Charsets.UTF_8)
    }
}