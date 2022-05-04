package chloeprime.botserver

import chloeprime.botserver.common.*
import chloeprime.botserver.webServer.*
import com.sun.net.httpserver.*
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.minecraftforge.fml.common.*
import net.minecraftforge.fml.common.event.*
import org.apache.logging.log4j.*
import java.io.*
import java.net.*

/**
 * @author ChloePrime
 */
@Mod(
    modid = BotServerMod.MODID,
    name = BotServerMod.NAME,
    version = BotServerMod.VERSION,
    modLanguage = "kotlin",
    acceptableRemoteVersions = "*",
    acceptedMinecraftVersions = "*"
)
class BotServerMod {
    companion object {
        const val MODID = "chloebot"
        const val NAME = "Chloe's Bot"
        const val VERSION = "1.2.1"
        lateinit var logger: Logger @JvmStatic get private set

        private var httpServer: HttpServer? = null

    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger = event.modLog

        ModConfig.file =
            File(event.suggestedConfigurationFile.parentFile, "$MODID.json")
        ModConfig.reload()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {

        val port = ModConfig.INSTANCE.port

        httpServer = HttpServer.create(InetSocketAddress(port), 0).apply {
            createContext("/", BotHttpHandler())
            start()
        }

        logger.info("Chloe Bot server is listening to port $port")

        embeddedServer(Netty, port = 8080, module = Application::module).start()
    }

    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(CommandReloadConfig())
    }

    @Mod.EventHandler
    fun serverStopping(event: FMLServerStoppingEvent) {
        httpServer?.stop(0)
        httpServer = null
    }
}