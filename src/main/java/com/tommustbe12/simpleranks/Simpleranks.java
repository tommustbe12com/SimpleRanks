package com.tommustbe12.simpleranks;

import org.bukkit.plugin.java.JavaPlugin;

public final class Simpleranks extends JavaPlugin {

    private static Simpleranks instance;
    private RankManager rankManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.rankManager = new RankManager(this);
        getCommand("rank").setExecutor(new RankCommand(rankManager));
        getCommand("rank").setTabCompleter(new RankTabCompleter(rankManager));

        getServer().getPluginManager().registerEvents(new RankListener(rankManager), this);

        getLogger().info("SimpleRanks enabled!");
    }

    @Override
    public void onDisable() {
        rankManager.saveRanks();
        getLogger().info("SimpleRanks disabled.");
    }

    public static Simpleranks getInstance() {
        return instance;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
