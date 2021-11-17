package chloeprime.botserver

import com.sun.net.httpserver.HttpServer
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import java.net.InetSocketAddress

@RuntimeDependencies(
    RuntimeDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2"),
    RuntimeDependency("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")
)
object BotServerPlugin : Plugin() {
    private const val DEFAULT_PORT = 8086
    private var httpServer: HttpServer? = null

    @Config
    lateinit var config: SecuredFile

    internal val authorizeDataLock = Any()

    /**
     * QQ User
     */
    internal val authorizedUsers = LongOpenHashSet()

    /**
     * QQ Group
     */
    internal val authorizedGroups = LongOpenHashSet()

    @Volatile
    internal var forceLocalHost = true

    @Volatile
    internal var commandResponseWaitTime = 1000L

    override fun onEnable() {
        loadConfig()
        val port = config.getInt("Port", DEFAULT_PORT)

        httpServer = HttpServer.create(InetSocketAddress(port), 0).apply {
            createContext("/", BotHttpHandler())
            start()
        }

        // 注册命令
        command("chloebot") {
            literal("reloadConfig") {
                execute<ProxyCommandSender> { sender, _, _ ->
                    config.reload()
                    loadConfig()
                    sender.sendMessage("§aReload Complete!")
                }
            }
        }

        info("Chloe Bot server is listening to port $port")
    }

    override fun onDisable() {
        httpServer?.stop(0)
        httpServer = null
    }

    private fun loadConfig() {
        /*
        if (config.file?.exists() != true) {
            config.set("Port", DEFAULT_PORT)
            config.set("AuthorizedUsers", arrayOf(-10000L))
            config.set("AuthorizedGroups", arrayOf(-10000L))
            config.saveToFile()
        } else {
            config.reload()
        }
        */

        synchronized(authorizeDataLock) {
            authorizedUsers.clear()
            authorizedUsers.addAll(config.getLongList("AuthorizedUsers"))

            authorizedGroups.clear()
            authorizedGroups.addAll(config.getLongList("AuthorizedGroups"))
        }

        forceLocalHost = config.getBoolean("ForceLocalHost")
        commandResponseWaitTime = config.getLong("CommandResponseWaitTime", 1000L)
    }
}