package com.allaymc.loginsecuritybackend;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AuthListener implements Listener {

    private final BackendTrustService trustService;
    private final FileConfiguration config;

    public AuthListener(BackendTrustService trustService, FileConfiguration config) {
        this.trustService = trustService;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        boolean premium = trustService.isPremium(player.getName());

        if (premium) {

            try {

                AuthMeApi.getInstance().forceLogin(player);

                player.sendMessage(ChatColor.GREEN + "Premium account detected. Auto logged in.");

            } catch (Exception e) {

                player.sendMessage(ChatColor.RED + "Premium verification failed.");

                e.printStackTrace();
            }
        }
        else {

            player.sendMessage(ChatColor.YELLOW + "Please login or register.");

        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        trustService.clearSession(player.getName());

    }
}
