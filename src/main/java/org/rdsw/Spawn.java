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
        World world = getTargetWorld(player);

        findSafeAsync(world, radius).thenAccept(loc -> {
            if (!player.isOnline()) return;
            Location target = (loc != null) ? loc : world.getSpawnLocation();
            debug("Teleporting " + player.getName()
                    + " to " + world.getName()
                    + " -> " + target.getBlockX() + "," + target.getBlockY() + "," + target.getBlockZ());
            player.teleportAsync(target);
        });
    }

    private World getTargetWorld(Player player) {
        String name = plugin.cfg().getMainWorld();
        World w = org.bukkit.Bukkit.getWorld(name);
        if (w == null) {
            debug("main_world '" + name + "' not found — using player's world.");
            return player.getWorld();
        }
        return w;
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
    // How many blocks below the world's "highest motion-blocking" surface we still
    // accept as a valid spawn. Keeping this small stops the search from tunneling
    // down into caves — a cave roof block is technically "solid" but may be dozens
    // of blocks below daylight.
    private static final int MAX_DEPTH_BELOW_SURFACE = 3;

    private int safeY(World world, int x, int z) {
        // MOTION_BLOCKING_NO_LEAVES ignores leaves but still counts water/ice as solid,
        // so this gives us the real outdoor surface height (ground OR top of water/ocean).
        int top = world.getHighestBlockYAt(x, z, org.bukkit.HeightMap.MOTION_BLOCKING_NO_LEAVES);

        Material topType = world.getBlockAt(x, top - 1, z).getType();
        if (isLiquid(topType)) {
            // Surface here is open water (ocean/lake) — reject the whole column so we
            // don't spawn the player on the seabed or floating over the sea.
            return -1;
        }

        int minY = Math.max(world.getMinHeight(), top - MAX_DEPTH_BELOW_SURFACE);
        for (int y = top; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();
            if (!block.isPassable()
                    && !isLiquid(type)
                    && type != Material.MAGMA_BLOCK
                    && type != Material.POWDER_SNOW
                    && type != Material.CACTUS
                    && isBreathable(world.getBlockAt(x, y + 1, z))
                    && isBreathable(world.getBlockAt(x, y + 2, z))) {
                return y;
            }
        }
        return -1;
    }

    private boolean isLiquid(Material type) {
        return type == Material.WATER || type == Material.LAVA;
    }

    // Passable is not enough — water/kelp/seagrass are all passable but unbreathable.
    private boolean isBreathable(Block b) {
        if (!b.isPassable()) return false;
        Material t = b.getType();
        return t != Material.WATER
                && t != Material.LAVA
                && t != Material.BUBBLE_COLUMN
                && t != Material.KELP
                && t != Material.KELP_PLANT
                && t != Material.SEAGRASS
                && t != Material.TALL_SEAGRASS;
    }
    private void debug(String msg) {
        if (plugin.cfg().isDebug()) {
            plugin.getLogger().info("[DEBUG] " + msg);
        }
    }
}