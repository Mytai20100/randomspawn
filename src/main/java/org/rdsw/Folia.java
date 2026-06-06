package org.rdsw;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class Folia extends Platform {

    public Folia(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void runAsync(Runnable task) {
        org.bukkit.Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
    }

    @Override
    public void runSync(Runnable task) {
        org.bukkit.Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
    }

    @Override
    public void runSyncDelayed(Runnable task, long delayTicks) {
        // Folia global scheduler uses milliseconds for delays, not ticks.
        // 1 tick ≈ 50 ms.
        long ms = delayTicks * 50L;
        org.bukkit.Bukkit.getAsyncScheduler().runDelayed(plugin, t ->
            org.bukkit.Bukkit.getGlobalRegionScheduler().run(plugin, r -> task.run()),
            Math.max(ms, 50), TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void runForEntity(Entity entity, Runnable task, long delayTicks) {
        // Use the entity scheduler so the task runs in the correct region for that entity.
        entity.getScheduler().runDelayed(plugin, t -> task.run(), null, Math.max(delayTicks, 1L));
    }
}
