package com.tommustbe12.simpleranks;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankListener implements Listener {

    private final RankManager manager;

    public RankListener(RankManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        RankManager.RankInfo info = manager.getRankInfo(player.getUniqueId());

        String prefix = ChatColor.translateAlternateColorCodes('&', info.prefix);
        String messageColor = info.importantText ? ChatColor.WHITE.toString() : ChatColor.GRAY.toString();

        // Reset colors after prefix to prevent leaking colors
        event.setFormat(prefix + ChatColor.RESET + " " + player.getName() + ": " + messageColor + event.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        RankManager.RankInfo info = manager.getRankInfo(player.getUniqueId());

        String prefix = ChatColor.translateAlternateColorCodes('&', info.prefix);

        // Reset colors after prefix to ensure proper coloring
        player.setPlayerListName(prefix + ChatColor.RESET + player.getName());
    }
}
