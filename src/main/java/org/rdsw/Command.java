package org.rdsw;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command implements CommandExecutor, TabCompleter {
    private static final int MAX_RADIUS = 30_000;
    private final main plugin;
    public Command(main plugin) {
        this.plugin = plugin;
    }
    public void register() {
        var cmd = plugin.getCommand("rdsw");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!sender.hasPermission("randomspawn.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.cfg().load();
                sender.sendMessage("§aRDSW config reloaded.");
            }
            case "around" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this subcommand.");
                    return true;
                }
                if (args.length < 2) {
                    sendUsage(sender);
                    return true;
                }
                int radius;
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number: " + args[1]);
                    return true;
                }
                if (radius < 1 || radius > MAX_RADIUS) {
                    sender.sendMessage("§cRadius must be between 1 and " + MAX_RADIUS + ".");
                    return true;
                }
                sender.sendMessage("§aTeleporting within §e" + radius + "§a blocks...");
                plugin.getSpawn().teleport(player, radius);
            }
            default -> sendUsage(sender);
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (!sender.hasPermission("randomspawn.admin")) return Collections.emptyList();
        if (args.length == 1) return Arrays.asList("around", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("around")) return Arrays.asList("500", "1000", "1900");
        return Collections.emptyList();
    }
    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§eUsage: /rdsw around <radius> | /rdsw reload");
    }
}
