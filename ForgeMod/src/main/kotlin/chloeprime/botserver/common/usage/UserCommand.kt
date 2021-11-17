package chloeprime.botserver.common.usage

import chloeprime.botserver.common.util.ASYNC_EXECUTOR
import chloeprime.botserver.common.util.mcServer
import chloeprime.botserver.common.util.ok
import chloeprime.botserver.protocol.RequestPO
import chloeprime.botserver.protocol.UserCommands
import com.sun.net.httpserver.HttpExchange
import java.lang.IllegalArgumentException
import kotlin.math.min

internal fun userCommand(request: RequestPO, httpExchange: HttpExchange) {
    when (request.msg.trim()) {
        UserCommands.SHOW_TPS -> showTps(httpExchange)
        else -> throw IllegalArgumentException("Unknown user command: ${request.msg.trim()}")
    }
}

private fun showTps(httpExchange: HttpExchange) {
    mcServer.addScheduledTask {
        val mspt = mcServer.tickTimeArray.average() * 1e-6
        val tps = min(1000.0 / mspt, 20.0)

        ASYNC_EXECUTOR.execute {
            httpExchange.ok("{tps=$tps,mspt=$mspt}")
        }
    }
}