package org.rdsw;

import org.bukkit.plugin.java.JavaPlugin;

public final class main extends JavaPlugin {

    private static main instance;
    private Config config;
    private Spawn spawn;
    private Platform platform;

    @Override
    public void onEnable() {
        instance = this;
        platform = Platform.detect(this);
        config = new Config(this);
        spawn = new Spawn(this);

        new PlayerSpawn(this).register();
        new Command(this).register();

        printBanner();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void printBanner() {
        String mode = (platform instanceof Folia) ? "Folia" : "Bukkit";
        String ver  = getDescription().getVersion();
        getLogger().info("  ██████╗  ███████╗");
        getLogger().info("  ██╔══██╗ ██╔════╝");
        getLogger().info("  ██████╔╝ ███████╗");
        getLogger().info("  ██╔══██╗ ╚════██║");
        getLogger().info("  ██║  ██║ ███████║");
        getLogger().info("  ╚═╝  ╚═╝ ╚══════╝");
        getLogger().info("  ");
        getLogger().info("  RandomSpawn v" + ver + " running on " + mode);
    }

    public static main getInstance() { return instance; }
    public Config cfg()              { return config; }
    public Spawn getSpawn()          { return spawn; }
    public Platform getPlatform()    { return platform; }
}
