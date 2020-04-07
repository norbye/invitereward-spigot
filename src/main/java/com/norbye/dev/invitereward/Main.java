package com.norbye.dev.invitereward;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    FileConfiguration config = getConfig();

    final String username = config.getString("dbuser", "");
    final String password = config.getString("dbpass", "");
    final String url = "jdbc:mysql://"
            + config.getString("dbhost", "localhost") + ":"
            + config.getInt("dbport", 3306) + "/"
            + config.getString("dbname", "");

    static Connection connection;

    @Override
    public void onEnable() {
        // Set the default config
        config.addDefault("dbhost", "localhost");
        config.addDefault("dbport", 3306);
        config.addDefault("dbuser", "");
        config.addDefault("dbpass", "");
        config.addDefault("dbname", "");
        config.options().copyDefaults(true);
        this.saveDefaultConfig();

        this.getCommand("invitereward").setExecutor(new CommandInviteReward(this));

        // Launch the mysql connections
        if ("".equals(config.getString("dbuser", ""))) {
            // The config variables must be set before the stuffs will be loaded
            return;
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("jdbc driver unavailable!");
            return;
        }

        try {
            connection = DriverManager.getConnection(
                    url,
                    username,
                    password
            );

            // Default db setup
            String sql = "CREATE TABLE IF NOT EXISTS invites(invite_id char(7))";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            // For more stuffs: https://www.spigotmc.org/wiki/mysql-database-integration-with-your-plugin/
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
