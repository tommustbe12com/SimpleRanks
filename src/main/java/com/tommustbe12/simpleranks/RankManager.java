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
    private final SimpleRanks plugin;
    private final Map<UUID, String> playerRanks = new HashMap<>();
    private final Map<String, RankInfo> rankData = new LinkedHashMap<>(); // Keep insertion order
    private final @NotNull Scoreboard scoreboard;

    private String defaultRank = "default";
    private boolean globalBracketsEnabled = true;
    private static final int DEFAULT_PRIORITY = 9999;

    public static class RankInfo {
        public final String prefix;
        public final String bracketColor;
        public final boolean importantText;
        public final int priority;
        public final boolean bracketsEnabled;

        public RankInfo(String prefix, String bracketColor, boolean importantText, int priority, boolean bracketsEnabled) {
            this.prefix = prefix;
            this.bracketColor = bracketColor;
            this.importantText = importantText;
            this.priority = priority;
            this.bracketsEnabled = bracketsEnabled;
        }
    }

    public RankManager(SimpleRanks plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        loadRanks();
    }

    public void loadRanks() {
        FileConfiguration config = plugin.getConfig();

        defaultRank = config.getString("default-rank", "default");
        globalBracketsEnabled = config.getBoolean("brackets.enabled", true);

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
                int priority = config.getInt("ranks." + rankKey + ".priority", DEFAULT_PRIORITY);
                boolean bracketsEnabled = config.getBoolean("ranks." + rankKey + ".brackets", true);
                rankData.put(rankKey, new RankInfo(prefix, bracketColor, importantText, priority, bracketsEnabled));
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
        plugin.getConfig().set("ranks." + rank + ".priority", DEFAULT_PRIORITY);
        plugin.getConfig().set("ranks." + rank + ".brackets", true);
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
        playerRanks.put(uuid, rank == null ? "" : rank);
        saveRanks();
    }

    public String getRank(UUID uuid) {
        return playerRanks.getOrDefault(uuid, defaultRank);
    }

    public boolean hasAssignedRank(UUID uuid) {
        return playerRanks.containsKey(uuid) && !playerRanks.get(uuid).isBlank();
    }

    public RankInfo getRankInfo(String rank) {
        return rankData.getOrDefault(rank, new RankInfo("&f" + rank, "&7", false, DEFAULT_PRIORITY, true));
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
        if (rank == null || rank.isBlank()) {
            return "";
        }
        RankInfo info = getRankInfo(rank);
        String prefix = info.prefix == null || info.prefix.isEmpty() ? ("&f" + rank) : info.prefix;
        if (!globalBracketsEnabled || !info.bracketsEnabled) {
            return prefix + "&r";
        }
        String bracketColor = info.bracketColor == null || info.bracketColor.isEmpty() ? "&7" : info.bracketColor;
        return bracketColor + "[" + prefix + bracketColor + "]&r";
    }

    public void updateDisplay(Player player) {
        String rank = getRank(player.getUniqueId());
        if (rank == null || rank.isBlank()) {
            player.setPlayerListName(player.getName());
            removePlayerFromRankTeams(player);
            player.setScoreboard(scoreboard);
            return;
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', getRankPrefix(rank));

        player.setPlayerListName(prefix + ChatColor.RESET + " " + player.getName());

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String teamName = buildTeamName(rank);

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

    private void removePlayerFromRankTeams(Player player) {
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }

    public int getRankPriority(String rank) {
        return getRankInfo(rank).priority;
    }

    public void setRankPriority(String rank, int priority) {
        plugin.getConfig().set("ranks." + rank + ".priority", priority);
        plugin.saveConfig();
        loadRanks();
    }

    public boolean areBracketsEnabled(String rank) {
        return getRankInfo(rank).bracketsEnabled;
    }

    public boolean areGlobalBracketsEnabled() {
        return globalBracketsEnabled;
    }

    public void setGlobalBracketsEnabled(boolean enabled) {
        plugin.getConfig().set("brackets.enabled", enabled);
        plugin.saveConfig();
        loadRanks();
    }

    public void setBracketsEnabled(String rank, boolean enabled) {
        plugin.getConfig().set("ranks." + rank + ".brackets", enabled);
        plugin.saveConfig();
        loadRanks();
    }

    private String buildTeamName(String rank) {
        int priority = Math.max(0, getRankPriority(rank));
        String priorityPart = String.format("%04d", Math.min(priority, 9999));

        String safeRank = rank == null ? "" : rank;
        safeRank = safeRank.replaceAll("[^a-zA-Z0-9]", "");
        if (safeRank.isEmpty()) safeRank = "rank";

        // Team name max length is 16. "sr" + 4 digits + "_" => 7 chars, leaving 9.
        if (safeRank.length() > 9) {
            String suffix = Integer.toString(Math.abs(safeRank.hashCode()) % 1296, 36); // 0..zz
            if (suffix.length() == 1) suffix = "0" + suffix;
            safeRank = safeRank.substring(0, 7) + suffix;
        }

        return "sr" + priorityPart + "_" + safeRank;
    }
}
