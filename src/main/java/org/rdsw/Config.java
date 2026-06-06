package org.rdsw;

public class Config {

    private static final int MIN_AROUND          = 1;
    private static final int MAX_AROUND          = 30_000;
    private static final int MIN_TIMEOUT_SECS    = 5;
    private static final int MAX_TIMEOUT_SECS    = 300;

    private final main plugin;
    private volatile int     around;
    private volatile boolean enabled;
    private volatile boolean spawnOnEveryLogin;
    private volatile int     respawnTimeoutTicks; // stored as ticks internally
    private volatile boolean debug;

    public Config(main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        int rawAround = plugin.getConfig().getInt("randomspawn.around", 1000);
        around = Math.max(MIN_AROUND, Math.min(rawAround, MAX_AROUND));

        enabled           = plugin.getConfig().getBoolean("randomspawn.enabled", true);
        spawnOnEveryLogin = plugin.getConfig().getBoolean("randomspawn.spawn_on_every_login", false);
        debug             = plugin.getConfig().getBoolean("randomspawn.debug", false);

        int rawSecs = plugin.getConfig().getInt("randomspawn.respawn_timeout", 30);
        int clampedSecs = Math.max(MIN_TIMEOUT_SECS, Math.min(rawSecs, MAX_TIMEOUT_SECS));
        respawnTimeoutTicks = clampedSecs * 20; // convert seconds → ticks
    }

    public int     getAround()              { return around; }
    public boolean isEnabled()              { return enabled; }
    public boolean spawnOnEveryLogin()      { return spawnOnEveryLogin; }
    public int     getRespawnTimeoutTicks() { return respawnTimeoutTicks; }
    public boolean isDebug()               { return debug; }
}
