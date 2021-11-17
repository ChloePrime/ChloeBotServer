package chloeprime.botserver.common.usage

import chloeprime.botserver.protocol.RequestOperations
import chloeprime.botserver.protocol.RequestPO
import com.sun.net.httpserver.HttpExchange
import java.net.HttpURLConnection

object RequestDispatcher {
    private val operations = linkedMapOf<String, (RequestPO, HttpExchange) -> Unit>()

    init {
        operations[RequestOperations.SERVER_COMMAND] = ::serverCommand
        operations[RequestOperations.USER_COMMAND] = ::userCommand
    }

    fun dispatchRequest(requestPO: RequestPO, httpExchange: HttpExchange) {
        operations[requestPO.operation]?.invoke(requestPO, httpExchange) ?: kotlin.run {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0L)
            httpExchange.close()
        }
    }
}