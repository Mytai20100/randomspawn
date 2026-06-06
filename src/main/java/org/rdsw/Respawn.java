package org.rdsw;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Respawn implements Listener {
    private final main plugin;
    public Respawn(main plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.cfg().isEnabled()) return;
        if (event.getEntity().getBedSpawnLocation() != null) return;

        Player player = event.getEntity();
        World  world  = player.getWorld();

        debug(player.getName() + " died — searching for random spawn...");
        plugin.getSpawn().findSafeAsync(world, plugin.cfg().getAround())
            .thenAccept(loc -> {
                Location target = (loc != null) ? loc : world.getSpawnLocation();
                debug("Location ready for " + player.getName()
                        + " -> " + target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ());
                pollUntilAlive(player, target, 0);
            });
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
