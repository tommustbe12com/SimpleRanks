package com.tommustbe12.simpleranks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleRanksExpansion extends PlaceholderExpansion {

    private final Simpleranks plugin;
    private final RankManager rankManager;

    public SimpleRanksExpansion(Simpleranks plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "simpleranks";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TomMustBe12";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String id) {
        if (player == null) return "";

        switch (id.toLowerCase()) {
            case "rank":
                // Get the rank name
                String rankName = rankManager.getRank(player.getUniqueId());
                // Get its prefix from config
                String coloredPrefix = rankManager.getRankInfo(rankName).prefix;
                // Translate color codes and return
                return ChatColor.translateAlternateColorCodes('&', coloredPrefix);

            case "raw_rank":
                return rankManager.getRank(player.getUniqueId());

            case "prefix":
                return ChatColor.translateAlternateColorCodes(
                        '&',
                        rankManager.getRankPrefix(
                                rankManager.getRank(player.getUniqueId())
                        )
                );

            case "important":
                return String.valueOf(
                        rankManager.getRankInfo(player.getUniqueId()).importantText
                );

            default:
                return null;
        }
    }
}
