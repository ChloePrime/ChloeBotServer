package chloeprime.botserver.server.bukkit

import chloeprime.botserver.common.CommonProxy
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import moe.gensoukyo.lib.server.bukkit
import net.minecraft.entity.player.EntityPlayer
import java.util.*
import java.util.concurrent.TimeUnit

class BukkitProxy : CommonProxy() {

    override fun getWorldName(pl: EntityPlayer): String {
        return pl.bukkit.world.name
    }

    private val permlessPlayers: Cache<UUID, Boolean> =
        CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build()

    override fun isPlayerVisible(pl: EntityPlayer): Boolean {
        val server = pl.server ?: return super.isPlayerVisible(pl)
        if (server.playerList.currentPlayerCount < 2) {
            return true
        }
        val pidTested = pl.uniqueID
        val bukkitPlayer = pl.bukkit

        /**
         * If any permless players can see [pl],
         * then the player is surely visible.
         */
        val surelyVisible = permlessPlayers.asMap().keys.any { pidPermless ->
            if (pidPermless == pidTested) {
                return false
            }

            val permless = server.playerList.getPlayerByUUID(pidPermless)
            permless.bukkit.canSee(bukkitPlayer)
        }
        if (surelyVisible) {
            return true
        }

        val allPlayers = bukkitPlayer.server.onlinePlayers

        /**
         * Return whether everybody on the server can see [pl],
         * and records anybody who can't see [pl] as permless players.
         */
        return allPlayers.all { tester ->
            if (tester.uniqueId == pidTested) {
                return@all true
            }

            val canSee = tester.canSee(bukkitPlayer)
            if (!canSee) {
                permlessPlayers.put(tester.uniqueId, false)
            }
            canSee
        }
    }
}