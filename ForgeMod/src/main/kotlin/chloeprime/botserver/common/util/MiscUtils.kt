package chloeprime.botserver.common.util

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.sun.net.httpserver.HttpExchange
import net.minecraftforge.fml.common.FMLCommonHandler
import java.net.HttpURLConnection
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal val mcServer
    get() = FMLCommonHandler.instance().minecraftServerInstance

/**
 * 用于发送 HTTP 回执包使用的线程池
 */
internal val ASYNC_EXECUTOR: Executor = run {
    val threadFactory = ThreadFactoryBuilder()
        .setNameFormat("bot-response-pool-%d")
        .build()

    ThreadPoolExecutor(
        2, 4, 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(128), threadFactory, ThreadPoolExecutor.AbortPolicy()
    )
}

internal fun HttpExchange.ok(body: String? = null) {
    if (body == null) {
        sendResponseHeaders(HttpURLConnection.HTTP_OK, 0L)
        return
    }
    // Encode BODY
    val bodyAsBytes = body.toByteArray(Charsets.UTF_8)
    // Write HEAD
    sendResponseHeaders(HttpURLConnection.HTTP_OK, bodyAsBytes.size.toLong())
    // Write BODY
    responseBody.write(bodyAsBytes)
    // 关闭 HTTP 请求
    close()
}