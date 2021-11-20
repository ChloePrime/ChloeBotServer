package chloeprime.botserver.common.usage

import chloeprime.botserver.BotServerMod
import chloeprime.botserver.common.*
import chloeprime.botserver.common.redirectCommand
import chloeprime.botserver.common.util.ASYNC_EXECUTOR
import chloeprime.botserver.common.util.mcServer
import chloeprime.botserver.common.util.ok
import chloeprime.botserver.protocol.RequestPO
import com.sun.net.httpserver.HttpExchange
import java.net.HttpURLConnection
import kotlin.math.max

internal fun serverCommand(request: RequestPO, httpExchange: HttpExchange) {
    // 命令必须以斜杠开头
    if (!isAuthorized(request)) {
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0L)
        httpExchange.close()
        return
    }

    handle0(httpExchange, request)
}

/**
 * 判断当前用户是否有权限执行命令
 */
private fun isAuthorized(request: RequestPO): Boolean {
    return with(ModConfig.INSTANCE) {
        request.user in authorizedUsers || request.group in authorizedGroups
    }
}

private fun handle0(httpExchange: HttpExchange, request: RequestPO) {
    // 获得真正执行的命令
    // 方便把一些原版命令重定向到插件命令上
    val command = redirectCommand(request.msg)
    // 以下的代码需要切换到主线程执行
    mcServer.addScheduledTask {
        val sender = BotCommandSender(mcServer)
        val startTime = System.currentTimeMillis()

        try {
            BotServerMod.logger.info("QQ用户 ${request.user} 执行命令 $command")
            mcServer.commandManager.executeCommand(sender, command)
        } catch (ex: Exception) {
            sender.sendMessage("命令执行过程中遇到了未知的错误: $ex")
        }

        ASYNC_EXECUTOR.execute {
            val delayTime = ModConfig.INSTANCE.commandResponseWaitTime -
                    // 执行命令本身消耗的时间
                    max(0, System.currentTimeMillis() - startTime)

            if (delayTime > 0) {
                Thread.sleep(delayTime)
            }

            // 发送命令执行结果
            httpExchange.ok(sender.getMessage())
            httpExchange.close()
        }
    }
}