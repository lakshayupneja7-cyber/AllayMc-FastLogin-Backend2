package com.allaymc.loginsecuritybackend;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AllayMcLoginSecurityBackend extends JavaPlugin {

    private Database database;
    private AuthService authService;
    private BackendTrustService trustService;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");

        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));

        database = new Database(this);
        try {
            database.connect();
        } catch (Exception e) {
            getLogger().severe("Database connection failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        authService = new AuthService(database);
        trustService = new BackendTrustService(this, getConfig().getString("plugin-message-channel", "allaymc:auth"));
        trustService.register();

        getServer().getPluginManager().registerEvents(new AuthListener(this, authService, trustService), this);
        getServer().getPluginManager().registerEvents(new RestrictionListener(authService), this);

        if (getCommand("register") != null) {
            getCommand("register").setExecutor(new AuthCommand(this, authService, AuthCommand.Type.REGISTER));
        }

        if (getCommand("login") != null) {
            getCommand("login").setExecutor(new AuthCommand(this, authService, AuthCommand.Type.LOGIN));
        }

        getLogger().info("AllayMcLoginSecurityBackend enabled.");
    }

    private void saveResourceIfMissing(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
}
