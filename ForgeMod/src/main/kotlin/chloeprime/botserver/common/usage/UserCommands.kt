package chloeprime.botserver.common.usage

import chloeprime.botserver.common.*
import chloeprime.botserver.common.util.*
import chloeprime.botserver.protocol.*
import chloeprime.botserver.webServer.*
import com.google.common.util.concurrent.*
import com.google.gson.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.streams.*


private val GSON = GsonBuilder().create()

private data class Tps(
    val mspt: Double,
    val tps: Double
)

internal suspend fun showTps(request: RequestPO, call: ApplicationCall) {
    val server = mcServer
    val task = server.callFromMainThread {
        val mspt = server.tickTimeArray.average() * 1e-6
        Tps(
            mspt,
            min(1000.0 / mspt, 20.0)
        )
    }
    var tps: Tps? = null
    Futures.addCallback(task, object : FutureCallback<Tps?> {
        override fun onSuccess(result: Tps?) {
            tps = result
        }

        override fun onFailure(t: Throwable) {
            t.printStackTrace()
        }
    }, MoreExecutors.directExecutor())

    while (tps == null) {
        delay(1)
        continue
    }

    val po = ResponsePO.Tps(tps!!.tps, tps!!.mspt)
    call.respond(GSON.toJson(po))
}

internal suspend fun listPlayers(request: RequestPO, call: ApplicationCall) {
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
        CoroutineScope(Dispatchers.IO).launch {
            call.respond(GSON.toJson(response))
        }
    }
}

internal suspend fun pat(request: RequestPO, call: ApplicationCall) {
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

        CoroutineScope(Dispatchers.IO).launch {
            call.respond(GSON.toJson(response))
        }
    }
}