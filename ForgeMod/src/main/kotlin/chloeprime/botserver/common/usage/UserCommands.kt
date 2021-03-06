package chloeprime.botserver.common.usage

import chloeprime.botserver.common.*
import chloeprime.botserver.common.util.*
import chloeprime.botserver.protocol.*
import chloeprime.botserver.webServer.*
import com.google.common.util.concurrent.*
import com.google.gson.*
import io.ktor.application.*
import io.ktor.response.*
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.streams.*


private val GSON = GsonBuilder().create()
internal suspend fun showTps(request: RequestPO, call: ApplicationCall) {
    val server = mcServer
    val task = server.callFromMainThread {
        val mspt = server.tickTimeArray.average() * 1e-6
        ResponsePO.Tps(
            min(1000.0 / mspt, 20.0),
            mspt
        )
    }
    var po: ResponsePO.Tps? = null
    Futures.addCallback(task, object : FutureCallback<ResponsePO.Tps?> {
        override fun onSuccess(result: ResponsePO.Tps?) {
            po = result
        }

        override fun onFailure(t: Throwable) {
            t.printStackTrace()
            throw t
        }
    }, MoreExecutors.directExecutor())

    while (po == null) {
        delay(1)
        continue
    }

    call.respond(GSON.toJson(po))
}

internal suspend fun listPlayers(request: RequestPO, call: ApplicationCall) {
    val server = mcServer
    val isAdminQuery = request.msgContext
        ?.split(' ')
        ?.contains("-admin")
        ?: false

    val task = server.callFromMainThread {
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
        ResponsePO.PlayerList(
            maxPlayer,
            players
        )
    }

    var response: ResponsePO.PlayerList? = null
    Futures.addCallback(task, object : FutureCallback<ResponsePO.PlayerList?> {
        override fun onSuccess(result: ResponsePO.PlayerList?) {
            response = result
        }

        override fun onFailure(t: Throwable) {
            t.printStackTrace()
        }
    }, MoreExecutors.directExecutor())

    while (response == null) {
        delay(1)
        continue
    }

    call.respond(GSON.toJson(response))
}

internal suspend fun pat(request: RequestPO, call: ApplicationCall) {
    val server = mcServer
    val ctx = GSON.fromJson(request.msgContext, RequestContext.Pat::class.java)
    val task = server.callFromMainThread {
        val player = server.playerList.getPlayerByUsername(ctx.playerName)

        val response = ResponsePO.Pat(0)
        if (player == null) {
            response.errorCode = ResponsePO.Pat.ERR_PLAYER_NOT_ONLINE
        } else {
            // ==> .pat ??????????????? <==
            pat0(player, request, ctx)
        }
        response
    }

    var response: ResponsePO.Pat? = null
    Futures.addCallback(task, object : FutureCallback<ResponsePO.Pat?> {
        override fun onSuccess(result: ResponsePO.Pat?) {
            response = result
        }

        override fun onFailure(t: Throwable) {
            response = ResponsePO.Pat(
                if (t is NullPointerException)
                    ResponsePO.Pat.MISSING_PARAMETERS //??????????????????
                else
                    ResponsePO.Pat.INTERNAL_ERROR //??????????????????
            )
        }
    }, MoreExecutors.directExecutor())

    while (response == null) {
        delay(1)
        continue
    }

    call.respond(GSON.toJson(response))
}