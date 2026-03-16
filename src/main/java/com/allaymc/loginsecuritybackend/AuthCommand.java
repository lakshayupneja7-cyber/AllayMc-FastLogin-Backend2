package com.allaymc.loginsecuritybackend;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {

    public enum Type {
        REGISTER,
        LOGIN
    }

    private final AllayMcLoginSecurityBackend plugin;
    private final AuthService authService;
    private final Type type;

    public AuthCommand(AllayMcLoginSecurityBackend plugin, AuthService authService, Type type) {
        this.plugin = plugin;
        this.authService = authService;
        this.type = type;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        FileConfiguration msg = plugin.getMessagesConfig();

        if (type == Type.REGISTER) {
            if (args.length < 2) {
                return true;
            }

            if (!args[0].equals(args[1])) {
                return true;
            }

            boolean ok = authService.register(player.getName(), args[0], player);
            if (ok) {
                authService.markAuthenticated(player);
                player.sendMessage(color(msg.getString("prefix")) + color(msg.getString("register-success")));
            }

            return true;
        }

        if (args.length < 1) {
            return true;
        }

        boolean ok = authService.login(player.getName(), args[0], player);
        if (ok) {
            authService.markAuthenticated(player);
            player.sendMessage(color(msg.getString("prefix")) + color(msg.getString("login-success")));
        } else {
            player.sendMessage(color(msg.getString("prefix")) + color(msg.getString("wrong-password")));
        }

        return true;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
