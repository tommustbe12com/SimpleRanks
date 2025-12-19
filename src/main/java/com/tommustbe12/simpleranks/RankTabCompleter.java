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
            // subcommand first arg
            List<String> subs = List.of("create", "delete", "give", "importanttext", "setdefault", "set", "get", "list", "bracketcolor", "deathmessages");
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
            case "delete":
            case "setdefault":
                // rank name
                if (args.length == 2) {
                    String partial = args[1];
                    if (subCommand.equals("create")) {
                        return Collections.emptyList();
                    } else {
                        // delete and setdefault, suggest existing ranks starting with partial
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
                    // suggest online players
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
                // /rank list no args
                return Collections.emptyList();

            case "bracketcolor":
                if (args.length == 2) {
                    String partialRank = args[1].toLowerCase();
                    return manager.getAllRanks().stream()
                            .filter(rank -> rank.toLowerCase().startsWith(partialRank))
                            .sorted()
                            .collect(Collectors.toList());
                } else if (args.length == 3) {
                    String partialColor = args[2].toLowerCase();
                    List<String> colorNames = List.of(
                            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
                            "gold", "gray", "dark_gray", "blue", "green", "aqua",
                            "red", "light_purple", "yellow", "white"
                    );
                    return colorNames.stream()
                            .filter(name -> name.startsWith(partialColor))
                            .sorted()
                            .collect(Collectors.toList());
                }
                break;

            case "deathmessages":
                if (args.length == 2) {
                    String partial = args[1].toLowerCase();
                    return List.of("on", "off").stream()
                            .filter(s -> s.startsWith(partial))
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();



            default:
                return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}
