package chloeprime.botserver.common

import com.google.gson.GsonBuilder
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.command.ICommandSender
import java.io.File

open class ModConfig {
    /**
     * @RequiresMcRestart
     */
    val port = 8086

    /**
     * QQ User
     */
    @JvmField
    val authorizedUsers = LongOpenHashSet()

    /**
     * QQ Group
     */
    @JvmField
    val authorizedGroups = LongOpenHashSet()

    /**
     * Bot 使用的 [ICommandSender] 的名称
     */
    @JvmField
    val botName = "Chloe's Bot"

    @JvmField
    val commandRedirects = linkedMapOf("tp" to "minecraft:teleport")

    /**
     * HTTP Server 和 HTTP Client 的 IP 地址是否必须都是 localhost
     */
    @JvmField
    val forceLocalHost = true

    @JvmField
    val commandResponseWaitTime = 1000L

    internal companion object {
        @Volatile
        lateinit var INSTANCE: ModConfig

        fun reload() {
            // 如果配置文件不存在，写入默认值
            if (!file.exists()) {
                file.bufferedWriter().use { w ->
                    GSON.toJson(ModConfig(), w)
                }
            }

            // 从磁盘重新加载配置文件
            INSTANCE = file.bufferedReader().use { r ->
                GSON.fromJson(r, ModConfig::class.java)
            }
        }

        private val GSON = GsonBuilder().setPrettyPrinting().create()
        internal lateinit var file: File
    }
}