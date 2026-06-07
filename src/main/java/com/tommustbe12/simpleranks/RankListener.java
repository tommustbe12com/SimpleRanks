package com.tommustbe12.simpleranks;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class RankListener implements Listener {

    private final RankManager manager;
    private final SimpleRanks plugin;

    public RankListener(SimpleRanks plugin, RankManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rank = manager.getRank(player.getUniqueId());
        if (rank == null || rank.isBlank()) {
            event.setFormat("%1$s: %2$s");
            return;
        }

        RankManager.RankInfo info = manager.getRankInfo(rank);

        String prefix = ChatColor.translateAlternateColorCodes(
                '&',
                manager.getRankPrefix(rank)
        );
        String messageColor = info.importantText ? ChatColor.WHITE.toString() : ChatColor.GRAY.toString();

        // Bukkit chat format must keep both placeholders or the event throws.
        event.setFormat(prefix + ChatColor.RESET + " %1$s: " + messageColor + "%2$s");
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
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // cleanup only teams owned by this plugin
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("sr") && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }

    // when player dies, optional include the rank prefix and stuff
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!SimpleRanks.getInstance().getConfig()
                .getBoolean("death-messages.enabled", true)) {
            return;
        }

        if (SimpleRanks.getInstance().getConfig()
                .getBoolean("death-messages.include-rank", true)) {
            // ranks on: let teams handle it
            return;
        }

        Player player = event.getEntity();
        String message = event.getDeathMessage();

        if (message == null) return;

        // Remove team prefix safely
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getEntryTeam(player.getName());

        if (team != null && team.getPrefix() != null && !team.getPrefix().isEmpty()) {
            String prefix = ChatColor.translateAlternateColorCodes('&', team.getPrefix());
            event.setDeathMessage(message.replace(prefix, ""));
        }
    }


}
