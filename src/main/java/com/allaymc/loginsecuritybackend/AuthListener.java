package com.allaymc.loginsecuritybackend;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Method;

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

            boolean authMeForceLogged = tryForceLoginWithAuthMe(player);

            if (authMeForceLogged) {
                player.sendActionBar(Component.text(color(msg.getString("premium-welcome", "&aPremium session detected."))));
                plugin.getLogger().info("Premium auto-login applied through AuthMe for " + player.getName());
            } else {
                player.sendMessage(color(msg.getString("prefix")) + "&aPremium session detected, but AuthMe API was not available.");
                plugin.getLogger().warning("AuthMe API not found or forceLogin failed for " + player.getName());
            }
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

    private boolean tryForceLoginWithAuthMe(Player player) {
        try {
            Class<?> apiClass = Class.forName("fr.xephi.authme.api.v3.AuthMeApi");
            Method getInstance = apiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);

            Method forceLogin = apiClass.getMethod("forceLogin", org.bukkit.entity.Player.class);
            forceLogin.invoke(api, player);

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("AuthMe reflection forceLogin failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
