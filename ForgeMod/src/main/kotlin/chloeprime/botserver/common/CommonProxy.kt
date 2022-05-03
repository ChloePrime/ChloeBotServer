package chloeprime.botserver.common

import chloeprime.botserver.server.bukkit.BukkitProxy
import net.minecraft.entity.player.EntityPlayer

/**
 * @author ChloePrime
 */
open class CommonProxy {
    companion object {
        @JvmStatic
        val INSTANCE = createInstance()

        private fun createInstance(): CommonProxy {
            try {
                Class.forName("org.bukkit.Bukkit")
                return BukkitProxy()
            } catch (ignored: ClassNotFoundException) {
                // pass
            }

            return CommonProxy()
        }
    }

    open fun getWorldName(pl: EntityPlayer): String {
        return pl.world.worldInfo.worldName
    }

    open fun isPlayerVisible(pl: EntityPlayer): Boolean {
        return true
    }
}