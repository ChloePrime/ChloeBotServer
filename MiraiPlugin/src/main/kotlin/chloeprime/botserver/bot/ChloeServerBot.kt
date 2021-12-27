package chloeprime.botserver.bot

import chloeprime.botserver.bot.command.registerBotCommands
import chloeprime.botserver.bot.command.unregisterBotCommands
import chloeprime.botserver.bot.customaction.CustomActionListener
import com.google.gson.GsonBuilder
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.data.content

object ChloeServerBot : KotlinPlugin(
    JvmPluginDescription(
        id = "chloeprime.botserver.bot",
        name = "ChloeServerBot",
        version = "0.0.1",
    ) {
        author("ChloePrime")
    }
) {
    object Permissions {
        val RUN_SERVER_COMMAND by lazy {
            PermissionService.INSTANCE.register(permissionId("server-command"), "发送 MC 服务器命令")
        }
        val CUSTOM_MINECRAFT_ACTION by lazy {
            PermissionService.INSTANCE.register(permissionId("custom-action"), "发送自定义 .pat 信息 ('#' + 动词 + 玩家id)")
        }

        internal fun init() {
            RUN_SERVER_COMMAND
            CUSTOM_MINECRAFT_ACTION
        }
    }

    internal val GSON = GsonBuilder().create()

    override fun onEnable() {
        super.onEnable()

        BotConfig.reload()
        registerBotCommands()
        Permissions.init()

        val channel = globalEventChannel().filterIsInstance<MessageEvent>()
        channel.subscribeAlways(ServerCommandSystem::onServerCommand)
        channel.filter(CustomActionListener::filter).subscribeAlways(CustomActionListener::onMessage)

        logger.info("ChloeServerBot loaded")
    }

    override fun onDisable() {
        super.onDisable()

        unregisterBotCommands()
        MinecraftBotServer.httpClient.close()

        logger.info("ChloeServerBot unloaded")
    }
}

internal object BotConfig : ReadOnlyPluginConfig("Config") {
    /**
     * .server 的可用目标
     */
    @ValueDescription("MC 服务器列表")
    val servers by value(
        mapOf(
            "test" to MinecraftBotServer(port = 8087)
        )
    )

    @ValueDescription("默认使用的 MC 服务器列表")
    val defaultServer by value(MinecraftBotServer(port = 8086))
}