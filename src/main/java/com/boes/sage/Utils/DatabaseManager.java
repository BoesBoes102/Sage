package com.boes.sage.Utils;

import com.boes.sage.Sage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final Sage plugin;
    private final String jdbcUrl;

    public DatabaseManager(Sage plugin) {
        this.plugin = plugin;

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File databaseFile = new File(dataFolder, "sage.db");
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }

        createTables();
    }

    public synchronized Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
                statement.execute("PRAGMA busy_timeout = 5000");
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open SQLite connection", e);
        }
    }

    public void close() {
    }

    private void createTables() {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS alt_players (" +
                "uuid TEXT PRIMARY KEY, " +
                "last_known_name TEXT, " +
                "last_ip TEXT" +
            ")",
            "CREATE TABLE IF NOT EXISTS alt_player_ips (" +
                "uuid TEXT NOT NULL, " +
                "ip TEXT NOT NULL, " +
                "PRIMARY KEY (uuid, ip)" +
            ")",
            "CREATE INDEX IF NOT EXISTS idx_alt_player_ips_ip ON alt_player_ips (ip)",

            "CREATE TABLE IF NOT EXISTS chat_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT NOT NULL, " +
                "player_name TEXT, " +
                "message TEXT, " +
                "type TEXT, " +
                "timestamp INTEGER" +
            ")",
            "CREATE INDEX IF NOT EXISTS idx_chat_log_uuid ON chat_log (uuid)",

            "CREATE TABLE IF NOT EXISTS item_db (" +
                "name TEXT PRIMARY KEY, " +
                "timestamp INTEGER, " +
                "serialized TEXT" +
            ")",

            "CREATE TABLE IF NOT EXISTS kits (" +
                "name TEXT PRIMARY KEY, " +
                "duration TEXT, " +
                "items TEXT, " +
                "armor TEXT, " +
                "offhand TEXT" +
            ")",
            "CREATE TABLE IF NOT EXISTS kit_cooldowns (" +
                "uuid TEXT PRIMARY KEY, " +
                "expires_at INTEGER" +
            ")",

            "CREATE TABLE IF NOT EXISTS messaging_spies (" +
                "uuid TEXT PRIMARY KEY" +
            ")",

            "CREATE TABLE IF NOT EXISTS offline_inventory_enderchest (" +
                "uuid TEXT PRIMARY KEY, " +
                "serialized TEXT" +
            ")",
            "CREATE TABLE IF NOT EXISTS offline_inventory_data (" +
                "uuid TEXT PRIMARY KEY, " +
                "items TEXT, " +
                "armor TEXT, " +
                "offhand TEXT" +
            ")",

            "CREATE TABLE IF NOT EXISTS player_times (" +
                "uuid TEXT PRIMARY KEY, " +
                "time INTEGER" +
            ")",
            "CREATE TABLE IF NOT EXISTS player_weather (" +
                "uuid TEXT PRIMARY KEY, " +
                "weather TEXT" +
            ")",
            "CREATE TABLE IF NOT EXISTS pending_teleports (" +
                "uuid TEXT PRIMARY KEY, " +
                "location TEXT" +
            ")",

            "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT NOT NULL, " +
                "title TEXT, " +
                "message TEXT, " +
                "reason TEXT, " +
                "duration TEXT" +
            ")",
            "CREATE INDEX IF NOT EXISTS idx_notifications_uuid ON notifications (uuid)",

            "CREATE TABLE IF NOT EXISTS punishment_stacks (" +
                "uuid TEXT NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "stack INTEGER NOT NULL, " +
                "PRIMARY KEY (uuid, reason)" +
            ")",
            "CREATE TABLE IF NOT EXISTS punishment_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT NOT NULL, " +
                "type TEXT, " +
                "reason TEXT, " +
                "punisher TEXT, " +
                "timestamp INTEGER, " +
                "duration TEXT" +
            ")",
            "CREATE INDEX IF NOT EXISTS idx_punishment_history_uuid ON punishment_history (uuid)",
            "CREATE TABLE IF NOT EXISTS punishment_banned_ips (" +
                "uuid TEXT PRIMARY KEY, " +
                "ip TEXT" +
            ")",

            "CREATE TABLE IF NOT EXISTS spy_command (" +
                "uuid TEXT PRIMARY KEY" +
            ")",

            "CREATE TABLE IF NOT EXISTS staffmode (" +
                "uuid TEXT PRIMARY KEY, " +
                "world TEXT, " +
                "x REAL, " +
                "y REAL, " +
                "z REAL, " +
                "yaw REAL, " +
                "pitch REAL, " +
                "gamemode TEXT" +
            ")",

            "CREATE TABLE IF NOT EXISTS usage_bossbar_enabled (" +
                "uuid TEXT PRIMARY KEY" +
            ")",

            "CREATE TABLE IF NOT EXISTS warps (" +
                "name TEXT PRIMARY KEY, " +
                "world TEXT, " +
                "x REAL, " +
                "y REAL, " +
                "z REAL, " +
                "yaw REAL, " +
                "pitch REAL, " +
                "hidden INTEGER" +
            ")"
        };

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize SQLite schema: " + e.getMessage());
            throw new RuntimeException("Failed to initialize SQLite schema", e);
        }
    }
}
