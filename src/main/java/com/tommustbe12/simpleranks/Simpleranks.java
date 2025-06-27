package com.tommustbe12.simpleranks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Simpleranks extends JavaPlugin {

    private static Simpleranks instance;
    private RankManager rankManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Auto-migrate old rank formats
        migrateOldPrefixes();

        this.rankManager = new RankManager(this);
        getCommand("rank").setExecutor(new RankCommand(rankManager, this));
        getCommand("rank").setTabCompleter(new RankTabCompleter(rankManager));

        getServer().getPluginManager().registerEvents(new RankListener(rankManager), this);

        getLogger().info("SimpleRanks enabled!");
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

            // Check if prefix contains brackets and color codes (like &7[...&7])
            if (prefix.matches("(?i)&[0-9a-fk-or]*\\[.*\\][^\\[]*")) {
                // Attempt to extract bracket color before '['
                int leftBracketIndex = prefix.indexOf('[');
                if (leftBracketIndex > 0) {
                    String before = prefix.substring(0, leftBracketIndex);
                    String colorCode = null;

                    // Search for last &x color code before the bracket
                    for (int i = before.length() - 2; i >= 0; i--) {
                        if (before.charAt(i) == '&' && i + 1 < before.length()) {
                            colorCode = before.substring(i, i + 2);
                            break;
                        }
                    }

                    if (colorCode != null) {
                        // Save bracketColor
                        config.set("ranks." + rank + ".bracketColor", colorCode);

                        // Strip the brackets from the prefix (keep inside contents only)
                        String inside = prefix.substring(leftBracketIndex + 1, prefix.lastIndexOf(']'));
                        config.set("ranks." + rank + ".prefix", inside);

                        getLogger().info("Migrated rank '" + rank + "': bracketColor set to " + colorCode);
                    }
                }
            }
        }

        saveConfig();
    }

    public static Simpleranks getInstance() {
        return instance;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
