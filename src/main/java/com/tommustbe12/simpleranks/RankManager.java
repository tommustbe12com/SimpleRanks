package com.tommustbe12.simpleranks;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class RankManager {
    private final Simpleranks plugin;
    private final Map<UUID, String> playerRanks = new HashMap<>();
    private final Map<String, RankInfo> rankData = new LinkedHashMap<>(); // Keep insertion order

    private String defaultRank = "default";

    public static class RankInfo {
        public final String prefix;  // e.g. "&c[Admin]&r"
        public final boolean importantText;

        public RankInfo(String prefix, boolean importantText) {
            this.prefix = prefix;
            this.importantText = importantText;
        }
    }

    public RankManager(Simpleranks plugin) {
        this.plugin = plugin;
        loadRanks();
    }

    private void loadRanks() {
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
                boolean importantText = config.getBoolean("ranks." + rankKey + ".importantText", false);
                rankData.put(rankKey, new RankInfo(prefix, importantText));
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
        String prefix = "&7[&r" + rank + "&7]&r";
        plugin.getConfig().set("ranks." + rank + ".prefix", prefix);
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
        return rankData.getOrDefault(rank, new RankInfo("&7[Unknown]&r", false));
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
            return "&7[&f" + rank + "&7]&r"; // fallback if rank doesn't exist
        }
        return plugin.getConfig().getString("ranks." + rank + ".prefix", "&7[&f" + rank + "&7]&r");
    }
}
