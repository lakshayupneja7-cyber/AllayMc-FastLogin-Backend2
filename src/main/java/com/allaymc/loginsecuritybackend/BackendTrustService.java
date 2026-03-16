package com.allaymc.loginsecuritybackend;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BackendTrustService implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final String channel;
    private final Map<String, AuthMode> trustedModes = new ConcurrentHashMap<>();

    public BackendTrustService(JavaPlugin plugin, String channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    public void register() {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel);
    }

    public AuthMode getTrustedMode(String username) {
        return trustedModes.get(username);
    }

    public void clear(String username) {
        trustedModes.remove(username);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!this.channel.equals(channel)) {
            return;
        }

        try {
            String payload = new String(message, StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|");

            if (parts.length < 4) {
                return;
            }

            if (!parts[0].equals("AUTH")) {
                return;
            }

            String username = parts[1];
            AuthMode mode = AuthMode.valueOf(parts[3]);

            trustedModes.put(username, mode);
        } catch (Exception ignored) {
        }
    }
}
