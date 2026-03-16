package com.allaymc.loginsecuritybackend;

import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private final Database database;
    private final Set<UUID> authenticated = ConcurrentHashMap.newKeySet();

    public AuthService(Database database) {
        this.database = database;
    }

    public boolean isAuthenticated(UUID uuid) {
        return authenticated.contains(uuid);
    }

    public void markAuthenticated(Player player) {
        authenticated.add(player.getUniqueId());
    }

    public void clear(Player player) {
        authenticated.remove(player.getUniqueId());
    }

    public boolean isRegistered(String username) {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT username FROM auth_accounts WHERE username = ?"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean register(String username, String password, Player player) {
        if (isRegistered(username)) {
            return false;
        }

        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT INTO auth_accounts(username, password_hash, premium, last_ip, last_login_at) VALUES (?, ?, 0, ?, ?)"
        )) {
            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(12)));
            ps.setString(3, getIp(player));
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean login(String username, String password, Player player) {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT password_hash FROM auth_accounts WHERE username = ?"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String hash = rs.getString("password_hash");
                if (!BCrypt.checkpw(password, hash)) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE auth_accounts SET last_ip = ?, last_login_at = ? WHERE username = ?"
        )) {
            ps.setString(1, getIp(player));
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }

        return true;
    }

    public void trustPremium(Player player) {
        try (PreparedStatement ps = database.getConnection().prepareStatement("""
                INSERT INTO auth_accounts(username, premium, official_uuid, last_ip, last_login_at)
                VALUES (?, 1, ?, ?, ?)
                ON CONFLICT(username) DO UPDATE SET
                    premium = 1,
                    official_uuid = excluded.official_uuid,
                    last_ip = excluded.last_ip,
                    last_login_at = excluded.last_login_at
                """)) {
            ps.setString(1, player.getName());
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, getIp(player));
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (Exception ignored) {
        }

        markAuthenticated(player);
    }

    private String getIp(Player player) {
        InetSocketAddress address = player.getAddress();
        return address == null ? "unknown" : address.getAddress().getHostAddress();
    }
}
