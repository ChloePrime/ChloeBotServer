package chloeprime.botserver.common.usage

import chloeprime.botserver.common.util.mcServer
import chloeprime.botserver.protocol.RequestOperations
import chloeprime.botserver.protocol.RequestPO
import chloeprime.botserver.protocol.UserCommands
import com.sun.net.httpserver.HttpExchange
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection

object RequestDispatcher {
    private val operations = linkedMapOf<String, (RequestPO, HttpExchange) -> Unit>()
    private val userCommands = linkedMapOf<String, (RequestPO, HttpExchange) -> Unit>()

    init {
        operations[RequestOperations.SERVER_COMMAND] = ::serverCommand
        operations[RequestOperations.USER_COMMAND] = ::userCommand
        userCommands[UserCommands.SHOW_TPS] = ::showTps
        userCommands[UserCommands.LIST_PLAYERS] = ::listPlayers
        userCommands[UserCommands.PAT] = ::pat
    }

    fun dispatchRequest(requestPO: RequestPO, httpExchange: HttpExchange) {
        operations[requestPO.operation]?.invoke(requestPO, httpExchange) ?: kotlin.run {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0L)
            httpExchange.close()
        }
    }

    private fun userCommand(request: RequestPO, httpExchange: HttpExchange) {
        val cmd = request.msg.trim().substringAfter(';')
        val action = userCommands[cmd] ?: throw IllegalArgumentException(
            "Unknown user command: ${request.msg.trim()}"
        )

        action(request, httpExchange)
    }
}