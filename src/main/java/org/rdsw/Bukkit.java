package org.rdsw;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class Bukkit extends Platform {
    public Bukkit(JavaPlugin plugin) {
        super(plugin);
    }
    @Override
    public void runAsync(Runnable task) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
    @Override
    public void runSync(Runnable task) {
        org.bukkit.Bukkit.getScheduler().runTask(plugin, task);
    }
    @Override
    public void runSyncDelayed(Runnable task, long delayTicks) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
    @Override
    public void runForEntity(Entity entity, Runnable task, long delayTicks) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
}
