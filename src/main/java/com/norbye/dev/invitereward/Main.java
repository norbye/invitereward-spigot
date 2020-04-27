package com.norbye.dev.invitereward;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class Main extends JavaPlugin {

    FileConfiguration config = getConfig();

    Connection connection;

    public String PERMISSION_ALL = "invitereward.*";
    public String PERMISSION_REDEEM = "invitereward.redeem";
    public String PERMISSION_RELOAD = "invitereward.reload";
    public String PERMISSION_LIST = "invitereward.list";

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

        PluginCommand inviterewardCommand = this.getCommand("invitereward");
        if (inviterewardCommand == null) {
            // Invalid plugin
            error("Failed to enable invitereward command");
        } else {
            inviterewardCommand.setExecutor(new CommandInviteReward(this));
        }

        PluginCommand redeemCommand = this.getCommand("redeem");
        if (redeemCommand == null) {
            // Invalid plugin
            error("Failed to enable redeem command");
        } else {
            redeemCommand.setExecutor(new CommandRedeem(this));
        }

        // Launch the mysql connections
        initializeDBConnection();
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

    public void initializeDBConnection() {
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
            debug("mySQL connection established");

            // For more stuffs: https://www.spigotmc.org/wiki/mysql-database-integration-with-your-plugin/
        } catch (SQLException e) {
            connection = null;
            error("Failed to setup SQL connection. Enable debug in the config to view stacktrace.");
            if (getConfig().getBoolean("debug", false)) {
                // Only display failed to connect stacktrace if debug mode is enabled
                e.printStackTrace();
            }
            return;
        }

        try {
            updateDBStructure();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDBStructure() throws SQLException {
        // `db`.`invitations`
        String sql = "CREATE TABLE IF NOT EXISTS invitation(" +
                "`invite_id` char(7) primary key not null, " +
                "`command_id` int(11) not null," +
                "`expires` datetime default null," +
                "`used` datetime default null," +
                "`used_by` char(36) default null" +
                ")";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
        // `db`.`commands`
        sql = "CREATE TABLE IF NOT EXISTS `commands`(" +
                "`command_id` int(11) auto_increment primary key," +
                "`command` text not null," +
                "`active` tinyint(1) default 1" +
                ")";
        stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
        // `db`.`invitereward`
        sql = "CREATE TABLE IF NOT EXISTS `invitereward`(" +
                "`key` varchar(45) primary key not null," +
                "`value` text not null" +
                ")";
        stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();

        sql = "SELECT `key`, `value` FROM invitereward WHERE `key` = 'dbversion'";
        stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        int dbVersion = 0;
        if (rs.next()) {
            dbVersion = rs.getInt("value");
        }
        debug("dbVersion " + dbVersion);
        if (dbVersion == 0) {
            // Insert dbVersion
            sql = "INSERT INTO invitereward (`key`, `value`) VALUES (?,?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, "dbversion");
            stmt.setInt(2, 1);
            if (stmt.executeUpdate() != 0) {
                debug("Updated dbVersion to 1");
            } else {
                debug("Failed to update dbVersion to 1");
            }
        }
    }

    public void log(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        sendMessage(console,"&7[InviteReward] " + s);
    }

    public void debug(String s) {
        if (!getConfig().getBoolean("debug", false)) {
            return;
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        sendMessage(console,"&7[InviteReward][debug] " + s);
    }

    public void error(String s) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        sendMessage(console,"&4[InviteReward][error] " + s);
    }

    public void sendMessage(CommandSender commandSender, String msg) {
        commandSender.sendMessage(
                msg
                        .replaceAll("&4", ChatColor.DARK_RED + "")
                        .replaceAll("&c", ChatColor.RED + "")
                        .replaceAll("&6", ChatColor.GOLD + "")
                        .replaceAll("&e", ChatColor.YELLOW + "")
                        .replaceAll("&2", ChatColor.DARK_GREEN + "")
                        .replaceAll("&a", ChatColor.GREEN + "")
                        .replaceAll("&b", ChatColor.AQUA + "")
                        .replaceAll("&3", ChatColor.DARK_AQUA + "")
                        .replaceAll("&1", ChatColor.DARK_BLUE + "")
                        .replaceAll("&9", ChatColor.BLUE + "")
                        .replaceAll("&d", ChatColor.LIGHT_PURPLE + "")
                        .replaceAll("&5", ChatColor.DARK_PURPLE + "")
                        .replaceAll("&f", ChatColor.WHITE + "")
                        .replaceAll("&7", ChatColor.GRAY + "")
                        .replaceAll("&8", ChatColor.DARK_GRAY + "")
                        .replaceAll("&0", ChatColor.BLACK + "")
        );
    }
}
