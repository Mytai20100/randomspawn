package org.rdsw;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Respawn implements Listener {
    private final main plugin;

    // Players who died and still need a random location applied once
    // Folia actually respawns them (we don't know yet if they have a bed).
    private final Set<java.util.UUID> pending = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Respawn(main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.cfg().isEnabled()) return;

        // Don't touch getBedSpawnLocation()/getRespawnLocation() here — on Folia
        // that can force a synchronous chunk load from the wrong region thread.
        // Just flag the player; PlayerRespawnEvent tells us safely whether they
        // have a real bed/anchor spawn once Folia resolves it.
        pending.add(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!pending.remove(player.getUniqueId())) return;
        if (!plugin.cfg().isEnabled()) return;

        // Folia has already safely determined this for us — no manual chunk access needed.
        if (event.isBedSpawn() || event.isAnchorSpawn()) return;

        World world = getTargetWorld(player);

        debug(player.getName() + " respawning without bed/anchor spawn"
                + " — searching random spawn in: " + world.getName());

        plugin.getSpawn().findSafeAsync(world, plugin.cfg().getAround())
                .thenAccept(loc -> {
                    Location target = (loc != null) ? loc : world.getSpawnLocation();
                    debug("Location ready for " + player.getName()
                            + " -> " + target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ());
                    pollUntilAlive(player, target, 0);
                });
    }

    private World getTargetWorld(Player player) {
        String name = plugin.cfg().getMainWorld();
        World w = org.bukkit.Bukkit.getWorld(name);
        if (w == null) {
            plugin.getLogger().warning("[RandomSpawn] main_world '" + name
                    + "' not found — falling back to player's world.");
            return player.getWorld();
        }
        return w;
    }

    private void pollUntilAlive(Player player, Location target, int tick) {
        if (tick > plugin.cfg().getRespawnTimeoutTicks()) {
            debug("Poll timeout for " + player.getName() + " — giving up.");
            return;
        }
        if (!player.isOnline()) return;
        player.getScheduler().runDelayed(plugin, t -> {
            if (!player.isOnline()) return;
            if (!player.isDead()) {
                debug(player.getName() + " alive after " + tick + " ticks — teleporting.");
                player.teleportAsync(target);
            } else {
                pollUntilAlive(player, target, tick + 1);
            }
        }, null, 1L);
    }

    private void debug(String msg) {
        if (plugin.cfg().isDebug()) {
            plugin.getLogger().info("[DEBUG] " + msg);
        }
    }
}