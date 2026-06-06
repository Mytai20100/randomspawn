package org.rdsw;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerSpawn implements Listener {
    private final main plugin;
    public PlayerSpawn(main plugin) {
        this.plugin = plugin;
    }
    public void register() {
        Authme authMeListener = new Authme(plugin);
        if (authMeListener.register()) {
            plugin.getLogger().info("AuthMe detected — using LoginEvent for first-join spawn.");
        } else {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("AuthMe not found — using PlayerJoinEvent for first-join spawn.");
        }
        plugin.getServer().getPluginManager().registerEvents(new Respawn(plugin), plugin);
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            plugin.getPlatform().runSyncDelayed(() -> {
                if (player.isOnline()) plugin.getSpawn().teleport(player);
            }, 5L);
        }
    }
}
