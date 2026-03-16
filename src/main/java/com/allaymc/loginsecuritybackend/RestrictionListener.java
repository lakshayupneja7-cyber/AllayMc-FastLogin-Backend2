package com.allaymc.loginsecuritybackend;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class RestrictionListener implements Listener {

    private final AuthService authService;

    public RestrictionListener(AuthService authService) {
        this.authService = authService;
    }

    private boolean blocked(Player player) {
        return !authService.isAuthenticated(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!blocked(event.getPlayer())) return;
        if (event.getTo() == null) return;

        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (blocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (blocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (blocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!blocked(event.getPlayer())) return;

        String cmd = event.getMessage().toLowerCase();
        List<String> allowed = List.of("/login", "/register");

        if (allowed.stream().noneMatch(cmd::startsWith)) {
            event.setCancelled(true);
        }
    }
}
