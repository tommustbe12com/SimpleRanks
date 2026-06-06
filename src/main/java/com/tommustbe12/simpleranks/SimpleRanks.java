package com.tommustbe12.simpleranks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleRanks extends JavaPlugin {

    private static SimpleRanks instance;
    private RankManager rankManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        migrateOldPrefixes();

        this.rankManager = new RankManager(this);
        getCommand("rank").setExecutor(new RankCommand(rankManager, this));
        getCommand("rank").setTabCompleter(new RankTabCompleter(rankManager));

        getServer().getPluginManager().registerEvents(new RankListener(this, rankManager), this);

        getLogger().info("SimpleRanks enabled!");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SimpleRanksExpansion(this, rankManager).register();
            getLogger().info("Hooked into PlaceholderAPI");
        }
    }

    @Override
    public void onDisable() {
        rankManager.saveRanks();
        getLogger().info("SimpleRanks disabled.");
    }

    @SuppressWarnings("unchecked")
    private void migrateOldPrefixes() {
        FileConfiguration config = getConfig();

        if (!config.isConfigurationSection("ranks")) return;

        getLogger().info("Checking for older config...");

        for (String rank : config.getConfigurationSection("ranks").getKeys(false)) {
            String prefix = config.getString("ranks." + rank + ".prefix");
            if (prefix == null) continue;

            if (prefix.matches("(?i)&[0-9a-fk-or]*\\[.*\\][^\\[]*")) {
                int leftBracketIndex = prefix.indexOf('[');
                if (leftBracketIndex > 0) {
                    String before = prefix.substring(0, leftBracketIndex);
                    String colorCode = null;

                    for (int i = before.length() - 2; i >= 0; i--) {
                        if (before.charAt(i) == '&' && i + 1 < before.length()) {
                            colorCode = before.substring(i, i + 2);
                            break;
                        }
                    }

                    if (colorCode != null) {
                        config.set("ranks." + rank + ".bracketColor", colorCode);
                        String inside = prefix.substring(leftBracketIndex + 1, prefix.lastIndexOf(']'));
                        config.set("ranks." + rank + ".prefix", inside);
                        getLogger().info("Migrated rank '" + rank + "': bracketColor set to " + colorCode);
                    }
                }
            }
        }

        saveConfig();
    }

    public static SimpleRanks getInstance() {
        return instance;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
