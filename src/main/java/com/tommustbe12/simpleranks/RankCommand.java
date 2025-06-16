package com.tommustbe12.simpleranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;

public class RankCommand implements CommandExecutor {

    private final RankManager manager;

    public RankCommand(RankManager manager) {
        this.manager = manager;
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
                // Translate color codes in rank name (args[2])
                String coloredRank = ChatColor.translateAlternateColorCodes('&', args[2]) + ChatColor.RESET;
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s rank to " + coloredRank);
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
                // Translate color codes in rank name (args[2])
                String coloredRankGive = ChatColor.translateAlternateColorCodes('&', args[2]) + ChatColor.RESET;
                sender.sendMessage(ChatColor.GREEN + "Set " + targetGive.getName() + "'s rank to " + coloredRankGive);
                break;

            case "list":
                sender.sendMessage(ChatColor.GOLD + "Available Ranks:");
                for (String r : manager.getAllRanks()) {
                    String prefix = manager.getRankPrefix(r);
                    // translate color codes so the colored prefix shows correctly
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
    }
}
