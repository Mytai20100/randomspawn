package org.rdsw;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class Platform {
    protected final JavaPlugin plugin;
    protected Platform(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public abstract void runAsync(Runnable task);
    public abstract void runSync(Runnable task);
    public abstract void runSyncDelayed(Runnable task, long delayTicks);
    public abstract void runForEntity(org.bukkit.entity.Entity entity, Runnable task, long delayTicks);

    public static Platform detect(JavaPlugin plugin) {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            plugin.getLogger().info("Platform: Folia");
            return new Folia(plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Platform: Bukkit/Paper");
            return new Bukkit(plugin);
        }
    }
}
