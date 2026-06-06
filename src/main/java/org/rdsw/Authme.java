package org.rdsw;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;

public class Authme implements Listener {
    private final main plugin;
    public Authme(main plugin) {
        this.plugin = plugin;
    }
    @SuppressWarnings("unchecked")
    public boolean register() {
        Class<? extends Event> loginEventClass;
        try {
            loginEventClass = (Class<? extends Event>)
                    Class.forName("fr.xephi.authme.events.LoginEvent");
        } catch (ClassNotFoundException e) {
            return false;
        }
        EventExecutor executor = (listener, event) -> {
            if (!loginEventClass.isInstance(event)) return;
            handleLogin(event);
        };
        plugin.getServer().getPluginManager().registerEvent(
                loginEventClass, this, EventPriority.NORMAL, executor, plugin, false
        );
        return true;
    }
    private void handleLogin(Event event) {
        Player player;
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            player = (Player) getPlayer.invoke(event);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not get player from AuthMe LoginEvent: " + e.getMessage());
            return;
        }
        boolean isFirstTime = !player.hasPlayedBefore();
        plugin.getPlatform().runSyncDelayed(() -> {
            if (!player.isOnline()) return;
            if (isFirstTime || plugin.cfg().spawnOnEveryLogin()) {
                plugin.getSpawn().teleport(player);
            }
        }, 10L);
    }
}
