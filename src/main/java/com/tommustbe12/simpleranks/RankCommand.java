package com.tommustbe12.simpleranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class RankCommand implements CommandExecutor {

    private final RankManager manager;
    private final Simpleranks plugin;

    public RankCommand(RankManager manager, Simpleranks plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank create <rank>");
                    return true;
                }
                manager.createRank(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Created rank: " + args[1]);
                break;

            case "setdefault":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank setdefault <rank>");
                    return true;
                }
                if (!manager.rankExists(args[1])) {
                    sender.sendMessage(ChatColor.RED + "That rank does not exist!");
                    return true;
                }
                manager.setDefaultRank(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Default rank set to: " + args[1]);
                break;

            case "set":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank set <player> <rank>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!manager.rankExists(args[2])) {
                    sender.sendMessage(ChatColor.RED + "That rank does not exist!");
                    return true;
                }
                manager.setRank(target.getUniqueId(), args[2]);
                String coloredRank = ChatColor.translateAlternateColorCodes('&', args[2]) + ChatColor.RESET;
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s rank to " + coloredRank);

                if (target.isOnline()) {
                    manager.updateDisplay((Player) target);
                }
                break;

            case "give":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank give <player> <rank>");
                    return true;
                }
                OfflinePlayer targetGive = Bukkit.getOfflinePlayer(args[1]);
                if (!manager.rankExists(args[2])) {
                    sender.sendMessage(ChatColor.RED + "That rank does not exist!");
                    return true;
                }
                manager.setRank(targetGive.getUniqueId(), args[2]);
                String coloredRankGive = ChatColor.translateAlternateColorCodes('&', args[2]) + ChatColor.RESET;
                sender.sendMessage(ChatColor.GREEN + "Set " + targetGive.getName() + "'s rank to " + coloredRankGive);

                if (targetGive.isOnline()) {
                    manager.updateDisplay((Player) targetGive);
                }
                break;

            case "list":
                sender.sendMessage(ChatColor.GOLD + "Available Ranks:");
                for (String r : manager.getAllRanks()) {
                    String prefix = manager.getRankPrefix(r);
                    // color code translation and prefix show correctly
                    sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.translateAlternateColorCodes('&', prefix));
                }
                break;

            case "get":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank get <player>");
                    return true;
                }
                OfflinePlayer targetGet = Bukkit.getOfflinePlayer(args[1]);
                String rank = manager.getRank(targetGet.getUniqueId());
                String prefix = manager.getRankPrefix(rank);
                sender.sendMessage(ChatColor.YELLOW + targetGet.getName() + "'s rank: " + ChatColor.translateAlternateColorCodes('&', prefix));
                break;

            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank delete <rank>");
                    return true;
                }
                if (!manager.rankExists(args[1])) {
                    sender.sendMessage(ChatColor.RED + "That rank does not exist!");
                    return true;
                }
                manager.deleteRank(args[1]);
                sender.sendMessage(ChatColor.GREEN + "Deleted rank: " + args[1]);
                break;

            case "importanttext":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank importanttext <rank> <true|false>");
                    return true;
                }
                if (!manager.rankExists(args[1])) {
                    sender.sendMessage(ChatColor.RED + "That rank does not exist!");
                    return true;
                }
                boolean important;
                if (args[2].equalsIgnoreCase("true")) {
                    important = true;
                } else if (args[2].equalsIgnoreCase("false")) {
                    important = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "Please specify true or false for importanttext.");
                    return true;
                }
                manager.setImportantText(args[1], important);
                sender.sendMessage(ChatColor.GREEN + "Set important text for rank " + args[1] + " to " + important);
                break;

            case "bracketcolor":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rank bracketcolor <rank> <color>");
                    return true;
                }
                if (!manager.rankExists(args[1])) {
                    sender.sendMessage(ChatColor.RED + "That rank does not exist!");
                    return true;
                }

                Map<String, String> colorNameMap = Map.ofEntries(
                        Map.entry("black", "&0"),
                        Map.entry("dark_blue", "&1"),
                        Map.entry("dark_green", "&2"),
                        Map.entry("dark_aqua", "&3"),
                        Map.entry("dark_red", "&4"),
                        Map.entry("dark_purple", "&5"),
                        Map.entry("gold", "&6"),
                        Map.entry("gray", "&7"),
                        Map.entry("dark_gray", "&8"),
                        Map.entry("blue", "&9"),
                        Map.entry("green", "&a"),
                        Map.entry("aqua", "&b"),
                        Map.entry("red", "&c"),
                        Map.entry("light_purple", "&d"),
                        Map.entry("yellow", "&e"),
                        Map.entry("white", "&f")
                );

                String colorInput = args[2].toLowerCase();
                String colorCode = colorNameMap.get(colorInput);

                if (colorCode == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid color name. Valid options: " +
                            String.join(", ", colorNameMap.keySet()));
                    return true;
                }

                plugin.getConfig().set("ranks." + args[1] + ".bracketColor", colorCode);
                plugin.saveConfig();
                manager.loadRanks();
                sender.sendMessage(ChatColor.GREEN + "Set bracket color of " + args[1] + " to " +
                        ChatColor.translateAlternateColorCodes('&', colorCode) + colorInput);
                break;



            default:
                sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "SimpleRanks Commands:");
        sender.sendMessage(ChatColor.AQUA + "/rank create <rank>");
        sender.sendMessage(ChatColor.AQUA + "/rank setdefault <rank>");
        sender.sendMessage(ChatColor.AQUA + "/rank set <player> <rank>");
        sender.sendMessage(ChatColor.AQUA + "/rank get <player>");
        sender.sendMessage(ChatColor.AQUA + "/rank list");
        sender.sendMessage(ChatColor.AQUA + "/rank delete <rank>");
        sender.sendMessage(ChatColor.AQUA + "/rank importanttext <rank> <true|false>");
        sender.sendMessage(ChatColor.AQUA + "/rank bracketcolor <rank> <&color>");
    }
}
