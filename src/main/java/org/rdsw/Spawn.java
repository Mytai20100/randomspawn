package org.rdsw;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Spawn {
    private final main plugin;
    public Spawn(main plugin) {
        this.plugin = plugin;
    }
    public void teleport(Player player) {
        if (!plugin.cfg().isEnabled()) return;
        teleport(player, plugin.cfg().getAround());
    }
    public void teleport(Player player, int radius) {
        if (!player.isOnline()) return;
        World world = player.getWorld();

        findSafeAsync(world, radius).thenAccept(loc -> {
            if (!player.isOnline()) return;
            Location target = (loc != null) ? loc : world.getSpawnLocation();
            debug("Teleporting " + player.getName()
                    + " -> " + target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ());
            player.teleportAsync(target);
        });
    }
    public CompletableFuture<Location> findSafeAsync(World world, int radius) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        tryNextAttempt(world, radius, 0, future);
        return future;
    }
    private void tryNextAttempt(World world, int radius, int attempt, CompletableFuture<Location> future) {
        if (attempt >= 25) {
            debug("findSafeAsync gave up after 25 attempts.");
            future.complete(null);
            return;
        }
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int x = rng.nextInt(-radius, radius + 1);
        int z = rng.nextInt(-radius, radius + 1);

        world.getChunkAtAsync(x >> 4, z >> 4, true).thenAccept(chunk -> {
            int y = safeY(world, x, z);
            if (y == -1) {
                tryNextAttempt(world, radius, attempt + 1, future);
            } else {
                future.complete(new Location(world, x + 0.5, y + 1, z + 0.5));
            }
        }).exceptionally(ex -> {
            tryNextAttempt(world, radius, attempt + 1, future);
            return null;
        });
    }
    private int safeY(World world, int x, int z) {
        int top = world.getHighestBlockYAt(x, z);
        for (int y = top; y > world.getMinHeight(); y--) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();
            if (!block.isPassable()
                    && type != Material.LAVA
                    && type != Material.WATER
                    && type != Material.MAGMA_BLOCK
                    && type != Material.POWDER_SNOW
                    && type != Material.CACTUS
                    && world.getBlockAt(x, y + 1, z).isPassable()
                    && world.getBlockAt(x, y + 2, z).isPassable()) {
                return y;
            }
        }
        return -1;
    }
    private void debug(String msg) {
        if (plugin.cfg().isDebug()) {
            plugin.getLogger().info("[DEBUG] " + msg);
        }
    }
}
