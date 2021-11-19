package chloeprime.botserver.common.usage

import chloeprime.botserver.common.util.ASYNC_EXECUTOR
import chloeprime.botserver.common.util.mcServer
import chloeprime.botserver.common.util.ok
import chloeprime.botserver.protocol.RequestPO
import chloeprime.botserver.protocol.ResponsePO
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.HttpExchange
import kotlin.math.min

private val GSON = GsonBuilder().create()

internal fun showTps(request: RequestPO, httpExchange: HttpExchange) {
    val server = mcServer
    server.addScheduledTask {
        val mspt = server.tickTimeArray.average() * 1e-6
        val tps = min(1000.0 / mspt, 20.0)

        ASYNC_EXECUTOR.execute {
            httpExchange.ok("{tps=$tps,mspt=$mspt}")
        }
    }
}

internal fun listPlayers(request: RequestPO, httpExchange: HttpExchange) {
    val server = mcServer
    server.addScheduledTask {
        val maxPlayer = server.maxPlayers
        val players = server.playerList.players.map {
            ResponsePO.PlayerList.Entry(it.name, it.uniqueID)
        }
        val response = ResponsePO.PlayerList(maxPlayer, players.toTypedArray())

        ASYNC_EXECUTOR.execute {
            httpExchange.ok(GSON.toJson(response))
        }
    }
}