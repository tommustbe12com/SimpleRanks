package com.tommustbe12.simpleranks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RankTabCompleter implements TabCompleter {

    private final RankManager manager;

    public RankTabCompleter(RankManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            // First argument: subcommands
            List<String> subs = List.of("create", "delete", "give", "importanttext", "setdefault", "set", "get", "list");
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
            case "delete":
            case "setdefault":
                // Second arg: rank name (case-sensitive)
                if (args.length == 2) {
                    String partial = args[1];
                    // For create: no existing ranks suggested (empty list), to avoid confusion
                    if (subCommand.equals("create")) {
                        return Collections.emptyList();
                    } else {
                        // For delete and setdefault, suggest existing ranks starting with partial
                        return manager.getAllRanks().stream()
                                .filter(rank -> rank.startsWith(partial))
                                .sorted()
                                .collect(Collectors.toList());
                    }
                }
                break;

            case "give":
            case "set":
                // /rank give <player> <rank> or /rank set <player> <rank>
                if (args.length == 2) {
                    // Suggest online players matching partial
                    String partialPlayer = args[1].toLowerCase();
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(partialPlayer))
                            .sorted()
                            .collect(Collectors.toList());
                } else if (args.length == 3) {
                    // Suggest ranks starting with partial
                    String partialRank = args[2];
                    return manager.getAllRanks().stream()
                            .filter(rank -> rank.startsWith(partialRank))
                            .sorted()
                            .collect(Collectors.toList());
                }
                break;

            case "importanttext":
                // /rank importanttext <rank> <true|false>
                if (args.length == 2) {
                    String partialRank = args[1];
                    return manager.getAllRanks().stream()
                            .filter(rank -> rank.startsWith(partialRank))
                            .sorted()
                            .collect(Collectors.toList());
                } else if (args.length == 3) {
                    String partialBool = args[2].toLowerCase();
                    return List.of("true", "false").stream()
                            .filter(b -> b.startsWith(partialBool))
                            .collect(Collectors.toList());
                }
                break;

            case "get":
                // /rank get <player>
                if (args.length == 2) {
                    String partialPlayer = args[1].toLowerCase();
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(partialPlayer))
                            .sorted()
                            .collect(Collectors.toList());
                }
                break;

            case "list":
                // /rank list has no further args
                return Collections.emptyList();

            default:
                return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}
