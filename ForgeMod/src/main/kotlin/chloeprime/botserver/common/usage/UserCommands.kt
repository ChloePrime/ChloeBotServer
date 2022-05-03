package chloeprime.botserver.common.usage

import chloeprime.botserver.common.CommonProxy
import chloeprime.botserver.common.util.ASYNC_EXECUTOR
import chloeprime.botserver.common.util.mcServer
import chloeprime.botserver.common.util.ok
import chloeprime.botserver.protocol.RequestContext
import chloeprime.botserver.protocol.RequestPO
import chloeprime.botserver.protocol.ResponsePO
import com.google.gson.GsonBuilder
import com.sun.net.httpserver.HttpExchange
import kotlin.math.min
import kotlin.streams.toList

private val GSON = GsonBuilder().create()

internal fun showTps(request: RequestPO, httpExchange: HttpExchange) {
    val server = mcServer
    server.addScheduledTask {
        val mspt = server.tickTimeArray.average() * 1e-6
        val tps = min(1000.0 / mspt, 20.0)

        ASYNC_EXECUTOR.execute {
            val po = ResponsePO.Tps(tps, mspt)
            httpExchange.ok(GSON.toJson(po))
        }
    }
}

internal fun listPlayers(request: RequestPO, httpExchange: HttpExchange) {
    val server = mcServer
    val isAdminQuery = request.msgContext
        ?.split(' ')
        ?.contains("-admin")
        ?: false

    server.addScheduledTask {
        val maxPlayer = server.maxPlayers
        val players = server.playerList.players.stream()
            .filter {
                isAdminQuery || CommonProxy.INSTANCE.isPlayerVisible(it)
            }
            .map { pl ->
                val rpgInfo = null
                val loc = ResponsePO.PlayerList.Location(
                    pl.posX, pl.posY, pl.posZ, pl.dimension,
                    CommonProxy.INSTANCE.getWorldName(pl)
                )

                ResponsePO.PlayerList.Entry(
                    pl.name, pl.uniqueID, pl.health, pl.ping,
                    rpgInfo, loc
                )
            }
            .toList()

        val response = ResponsePO.PlayerList(maxPlayer, players)
        ASYNC_EXECUTOR.execute {
            httpExchange.ok(GSON.toJson(response))
        }
    }
}

internal fun pat(request: RequestPO, httpExchange: HttpExchange) {
    val server = mcServer
    val ctx = GSON.fromJson(request.msgContext, RequestContext.Pat::class.java)
    server.addScheduledTask {
        val player = server.playerList.getPlayerByUsername(ctx.playerName)

        val response = ResponsePO.Pat(0)
        if (player == null) {
            response.errorCode = ResponsePO.Pat.ERR_PLAYER_NOT_ONLINE
        } else {
            // ==> .pat 的核心逻辑 <==
            pat0(player, request, ctx)
        }

        ASYNC_EXECUTOR.execute {
            httpExchange.ok(GSON.toJson(response))
        }
    }
}