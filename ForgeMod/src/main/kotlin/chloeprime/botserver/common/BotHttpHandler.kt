package chloeprime.botserver.common

import chloeprime.botserver.BotServerMod
import chloeprime.botserver.common.usage.RequestDispatcher
import chloeprime.botserver.protocol.RequestPO
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

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

        if (ModConfig.INSTANCE.forceLocalHost) {
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

        RequestDispatcher.dispatchRequest(request, httpExchange)
    }

    companion object {
        private val LOGGER by lazy { BotServerMod.logger }
        private val GSON = GsonBuilder().create()
    }
}