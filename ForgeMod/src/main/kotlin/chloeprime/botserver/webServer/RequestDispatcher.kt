package chloeprime.botserver.webServer

import chloeprime.botserver.common.usage.*
import chloeprime.botserver.protocol.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.coroutines.*

object RequestDispatcher {
    private val operations = linkedMapOf<String, suspend (RequestPO, ApplicationCall) -> Unit>()
    private val userCommands = linkedMapOf<String, suspend (RequestPO, ApplicationCall) -> Unit>()

    init {
        operations[RequestOperations.SERVER_COMMAND] = ::serverCommand
        operations[RequestOperations.USER_COMMAND] = ::userCommand
        userCommands[UserCommands.SHOW_TPS] = ::showTps
        userCommands[UserCommands.LIST_PLAYERS] = ::listPlayers
        userCommands[UserCommands.PAT] = ::pat
    }

    suspend fun dispatchRequest(requestPO: RequestPO, call: ApplicationCall) {
        operations[requestPO.operation]?.invoke(requestPO, call) ?: kotlin.run {
            call.respond(HttpStatusCode.NotFound, 0L)
        } }

    private suspend fun userCommand(request: RequestPO, call: ApplicationCall) {
        val cmd = request.msg.trim().substringAfter(';')
        val action = userCommands[cmd] ?: throw IllegalArgumentException(
            "Unknown user command: ${request.msg.trim()}"
        )

        // 满足IDE
        withContext(
            Dispatchers.IO
        ) {
            action(request, call)
        }
    }
}