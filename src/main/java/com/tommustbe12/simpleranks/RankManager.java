package com.tommustbe12.simpleranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RankManager {
    private final Simpleranks plugin;
    private final Map<UUID, String> playerRanks = new HashMap<>();
    private final Map<String, RankInfo> rankData = new LinkedHashMap<>(); // Keep insertion order
    private final @NotNull Scoreboard scoreboard;

    private String defaultRank = "default";

    public static class RankInfo {
        public final String prefix;
        public final String bracketColor;
        public final boolean importantText;

        public RankInfo(String prefix, String bracketColor, boolean importantText) {
            this.prefix = prefix;
            this.bracketColor = bracketColor;
            this.importantText = importantText;
        }
    }

    public RankManager(Simpleranks plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        loadRanks();
    }

    public void loadRanks() {
        FileConfiguration config = plugin.getConfig();

        defaultRank = config.getString("default-rank", "default");

        playerRanks.clear();
        if (config.isConfigurationSection("player-ranks")) {
            for (String uuidStr : config.getConfigurationSection("player-ranks").getKeys(false)) {
                String rank = config.getString("player-ranks." + uuidStr);
                playerRanks.put(UUID.fromString(uuidStr), rank);
            }
        }

        rankData.clear();
        if (config.isConfigurationSection("ranks")) {
            for (String rankKey : config.getConfigurationSection("ranks").getKeys(false)) {
                String prefix = config.getString("ranks." + rankKey + ".prefix", "");
                String bracketColor = config.getString("ranks." + rankKey + ".bracketColor", "&7");
                boolean importantText = config.getBoolean("ranks." + rankKey + ".importantText", false);
                rankData.put(rankKey, new RankInfo(prefix, bracketColor, importantText));
            }
        }
    }

    public void saveRanks() {
        FileConfiguration config = plugin.getConfig();
        config.set("player-ranks", null);
        for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
            config.set("player-ranks." + entry.getKey().toString(), entry.getValue());
        }
        plugin.saveConfig();
    }

    public void createRank(String rank) {
        plugin.getConfig().set("ranks." + rank + ".prefix", "&f" + rank);
        plugin.getConfig().set("ranks." + rank + ".bracketColor", "&7");
        plugin.getConfig().set("ranks." + rank + ".importantText", false);
        plugin.saveConfig();
        loadRanks();
    }



    public void deleteRank(String rank) {
        plugin.getConfig().set("ranks." + rank, null);
        plugin.saveConfig();
        loadRanks();
    }

    public boolean rankExists(String rank) {
        return rankData.containsKey(rank);
    }

    public void setRank(UUID uuid, String rank) {
        playerRanks.put(uuid, rank);
        saveRanks();
    }

    public String getRank(UUID uuid) {
        return playerRanks.getOrDefault(uuid, defaultRank);
    }

    public RankInfo getRankInfo(String rank) {
        return rankData.getOrDefault(rank, new RankInfo("&f" + rank, "&7", false));
    }

    public RankInfo getRankInfo(UUID uuid) {
        return getRankInfo(getRank(uuid));
    }

    public void setDefaultRank(String rank) {
        defaultRank = rank;
        plugin.getConfig().set("default-rank", defaultRank);
        plugin.saveConfig();
    }

    public void setImportantText(String rank, boolean important) {
        plugin.getConfig().set("ranks." + rank + ".importantText", important);
        plugin.saveConfig();
        loadRanks();
    }

    public Set<String> getAllRanks() {
        return rankData.keySet();
    }

    public Map<UUID, String> getAllPlayerRanks() {
        return playerRanks;
    }

    public String getRankPrefix(String rank) {
        if (!rankExists(rank)) {
            return "&7[&f" + rank + "&7]&r ";
        }

        String prefix = plugin.getConfig().getString("ranks." + rank + ".prefix", "&f" + rank);
        String bracketColor = plugin.getConfig().getString("ranks." + rank + ".bracketColor", "&7");

        return bracketColor + "[" + prefix + bracketColor + "]&r";
    }

    public void updateDisplay(Player player) {
        String prefix = ChatColor.translateAlternateColorCodes('&', getRankPrefix(getRank(player.getUniqueId())));

        player.setPlayerListName(prefix + ChatColor.RESET + " " + player.getName());

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String safeRankName = ChatColor.stripColor(prefix).replaceAll("[^a-zA-Z0-9]", "");
        if (safeRankName.length() > 12) safeRankName = safeRankName.substring(0, 12);
        String teamName = "sr_" + safeRankName;

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.setPrefix(prefix + ChatColor.WHITE + " ");
        team.setSuffix("");
        team.setColor(ChatColor.WHITE);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);

        for (Team other : scoreboard.getTeams()) {
            if (other.hasEntry(player.getName()) && !other.getName().equals(teamName)) {
                other.removeEntry(player.getName());
            }
        }

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        // scoreboard applies to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.setScoreboard(scoreboard);
        }
    }
}
