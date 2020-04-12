package com.norbye.dev.invitereward;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    FileConfiguration config = getConfig();

    static Connection connection;

    @Override
    public void onEnable() {
        // Set the default config
        config.addDefault("db.host", "localhost");
        config.addDefault("db.port", 3306);
        config.addDefault("db.user", "");
        config.addDefault("db.pass", "");
        config.addDefault("db.name", "");
        config.options().copyDefaults(true);
        this.saveDefaultConfig();

        this.getCommand("invitereward").setExecutor(new CommandInviteReward(this));

        // Launch the mysql connections
        initialiseDBConnection();
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialiseDBConnection() {
        final String username = config.getString("db.user", "");
        final String password = config.getString("db.pass", "");
        final String url = "jdbc:mysql://"
                + config.getString("db.host", "localhost") + ":"
                + config.getInt("db.port", 3306) + "/"
                + config.getString("db.name", "");

        if ("".equals(username)) {
            // The config variables must be set before the stuffs will be loaded
            error("db credentials are required for the plugin to work");
            return;
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            debug("mySQL driver exists");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            error("jdbc driver unavailable!");
            return;
        }

        try {
            connection = DriverManager.getConnection(
                    url,
                    username,
                    password
            );
            debug("Connection established");

            // TODO update the table setup
            // Default db setup
            String sql = "CREATE TABLE IF NOT EXISTS invites(invite_id char(7))";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            debug("Default table created");
            // For more stuffs: https://www.spigotmc.org/wiki/mysql-database-integration-with-your-plugin/
        } catch (SQLException e) {
            error("Failed to setup SQL connection");
            e.printStackTrace();
        }
    }

    public void log(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_GRAY + "[InviteReward] " + s);
    }

    public void debug(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_GRAY + "[InviteReward][debug] " + s);
    }

    public void error(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(ChatColor.DARK_RED + "[InviteReward][error] " + s);
    }
}
