package com.tommustbe12.simpleranks;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class RankListener implements Listener {

    private final RankManager manager;

    public RankListener(RankManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        RankManager.RankInfo info = manager.getRankInfo(player.getUniqueId());

        String prefix = ChatColor.translateAlternateColorCodes('&', manager.getRankPrefix(manager.getRank(player.getUniqueId())));
        String messageColor = info.importantText ? ChatColor.WHITE.toString() : ChatColor.GRAY.toString();

        // reset colors after prefix, no leaking colors
        event.setFormat(prefix + ChatColor.RESET + " " + player.getName() + ": " + messageColor + event.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    manager.updateDisplay(player);
                }
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("Simpleranks"), 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // cleanup
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }
}
