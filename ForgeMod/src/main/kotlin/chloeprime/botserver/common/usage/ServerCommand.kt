package chloeprime.botserver.common.usage

import chloeprime.botserver.*
import chloeprime.botserver.common.*
import chloeprime.botserver.common.util.*
import chloeprime.botserver.webServer.*
import com.google.common.util.concurrent.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.coroutines.*

internal suspend fun serverCommand(request: RequestPO, call: ApplicationCall) {
    // 命令必须以斜杠开头
    if (!isAuthorized(request)) {
        call.respond(HttpStatusCode.Forbidden, 0L)
        return
    }

    handle0(call, request)
}

/**
 * 判断当前用户是否有权限执行命令
 * **1.0.0 版本之后将在 bot 侧做权限控制。**
 */
private fun isAuthorized(request: RequestPO): Boolean {
    return true
}

private suspend fun handle0(call: ApplicationCall, request: RequestPO) {
    // 获得真正执行的命令
    // 方便把一些原版命令重定向到插件命令上
    val command = redirectCommand(request.msg)
    // 以下的代码需要切换到主线程执行
    val task = mcServer.callFromMainThread {
        val sender = BotCommandSender(mcServer)

        try {
            BotServerMod.logger.info("QQ用户 ${request.user} 执行命令 $command")
            mcServer.commandManager.executeCommand(sender, command)
        } catch (ex: Exception) {
            sender.sendMessage("命令执行过程中遇到了未知的错误: $ex")
        }
        sender
    }

    var response: BotCommandSender? = null
    Futures.addCallback(task, object : FutureCallback<BotCommandSender?> {
        override fun onSuccess(result: BotCommandSender?) {
            response = result
        }

        override fun onFailure(t: Throwable) {
            BotCommandSender(mcServer).sendMessage("命令缺少参数")
        }
    }, MoreExecutors.directExecutor())

    while (response == null) {
        delay(1)
        continue
    }

    // 发送命令执行结果
    call.respond(response!!.getMessage())
}