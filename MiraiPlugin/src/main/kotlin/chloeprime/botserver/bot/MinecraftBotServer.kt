package chloeprime.botserver.bot

import chloeprime.botserver.protocol.RequestPO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User

@Serializable
class MinecraftBotServer(
    /**
     * 可选的，默认使用 localhost。
     */
    private val host: String = "localhost",
    private val port: Int
) {
    companion object {
        internal val httpClient = HttpClient {
            expectSuccess = false
        }
    }

    /**
     * 向 MC 侧发送请求执行指令，并获取返回结果
     *
     * @return 如果无需发送反馈（例如没有权限在服务器执行操作）则返回 null
     * @throws SocketTimeoutException
     */
    suspend fun sendRequestTo(
        request: RequestPO,
    ): String? {
        val httpBody = ChloeServerBot.GSON.toJson(request)

        val response: HttpResponse = httpClient.post {
            host = this@MinecraftBotServer.host
            port = this@MinecraftBotServer.port
            body = httpBody
        }

        return when (response.status) {
            HttpStatusCode.OK -> response.receive()
            // 无权执行命令，直接无视
            HttpStatusCode.Forbidden -> return null
            // 打印状态码
            else -> response.status.toString()
        }
    }
}

object ServerSelector {

    fun getByGroup(group: Group): MinecraftBotServer {
        return byGroup[group.id] ?: default
    }

    fun getByUser(user: User): MinecraftBotServer {
        return byUser[user.id] ?: default
    }

    fun hasOverrideForServerName(selection: String): Boolean {
        return getServer(selection) !== default
    }

    /**
     * 获取当前上下文使用的 Minecraft 服务器。
     */
    fun get(sender: CommandSender): MinecraftBotServer {
        val group = sender.getGroupOrNull()
        val user = sender.user

        return if (group != null) {
            getByGroup(group)
        } else if (user != null) {
            getByUser(user)
        } else {
            consoles ?: default
        }
    }

    fun put(sender: CommandSender, selection: String) {
        val group = sender.getGroupOrNull()
        val user = sender.user
        val selectedMcServer = getServer(selection)

        if (group != null) {
            byGroup[group.id] = selectedMcServer
        } else if (user != null) {
            byUser[user.id] = selectedMcServer
        } else {
            consoles = selectedMcServer
        }
    }

    private val byUser = linkedMapOf<Long, MinecraftBotServer>()
    private val byGroup = linkedMapOf<Long, MinecraftBotServer>()
    private var consoles: MinecraftBotServer? = null

    private fun getServer(name: String): MinecraftBotServer {
        return BotConfig.servers.getOrDefault(name, default)
    }

    private val default get() = BotConfig.defaultServer
}