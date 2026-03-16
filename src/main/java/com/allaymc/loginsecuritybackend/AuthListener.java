package com.allaymc.loginsecuritybackend;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthListener implements Listener {

    private final AllayMcLoginSecurityBackend plugin;
    private final AuthService authService;
    private final BackendTrustService trustService;

    public AuthListener(AllayMcLoginSecurityBackend plugin, AuthService authService, BackendTrustService trustService) {
        this.plugin = plugin;
        this.authService = authService;
        this.trustService = trustService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration msg = plugin.getMessagesConfig();

        AuthMode mode = trustService.getTrustedMode(player.getName());
        if (mode == AuthMode.PREMIUM) {
            authService.trustPremium(player);
            player.sendActionBar(Component.text(color(msg.getString("premium-welcome", "&aPremium session detected."))));
            return;
        }

        if (authService.isRegistered(player.getName())) {
            player.sendMessage(color(msg.getString("prefix")) + color(msg.getString("login-prompt")));
        } else {
            player.sendMessage(color(msg.getString("prefix")) + color(msg.getString("register-prompt")));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        authService.clear(event.getPlayer());
        trustService.clear(event.getPlayer().getName());
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
