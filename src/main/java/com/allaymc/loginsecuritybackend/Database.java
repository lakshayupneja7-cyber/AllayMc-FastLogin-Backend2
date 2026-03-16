package com.allaymc.loginsecuritybackend;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final JavaPlugin plugin;
    private Connection connection;

    public Database(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "accounts.db").getAbsolutePath());

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth_accounts (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT,
                    premium INTEGER NOT NULL DEFAULT 0,
                    official_uuid TEXT,
                    last_ip TEXT,
                    last_login_at INTEGER NOT NULL DEFAULT 0
                )
            """);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
