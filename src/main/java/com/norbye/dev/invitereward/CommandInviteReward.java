package com.norbye.dev.invitereward;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.sql.*;

public class CommandInviteReward implements CommandExecutor {

    private Main plugin;

    public CommandInviteReward(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        boolean hasReloadPermission = commandSender.hasPermission(plugin.PERMISSION_RELOAD);
        boolean hasListPermission = commandSender.hasPermission(plugin.PERMISSION_LIST);
        if (!(commandSender instanceof Player) || commandSender.hasPermission(plugin.PERMISSION_ALL)) {
            hasReloadPermission = true;
            hasListPermission = true;
        }
        if (!hasReloadPermission && !hasListPermission) {
            sendMessage(commandSender, "&4You have no permission to perform this command.");
            return true;
        }
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0])) {
                onReloadCommand(commandSender);
                return true;
            } else if ("list".equalsIgnoreCase(args[0])) {
                onListCommand(commandSender, args);
                return true;
            }
        }
        PluginDescriptionFile pdf = plugin.getDescription();
        sendMessage(commandSender,"&6[" + pdf.getName() + "] v" + pdf.getVersion());
        if (hasReloadPermission) {
            sendMessage(commandSender, "/invitereward reload");
        }
        if (hasListPermission) {
            sendMessage(commandSender, "/invitereload list");
        }
        return true;
    }

    private void onReloadCommand(CommandSender commandSender) {
        if (commandSender instanceof Player && !commandSender.hasPermission(plugin.PERMISSION_RELOAD)) {
            sendMessage(commandSender, "&4You have do not have permission to execute this command.");
            return;
        }
        // Regenerate config if deleted
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();
        // Reload config values
        plugin.reloadConfig();
        plugin.config = plugin.getConfig();
        // Restart DB connection
        try {
            if (plugin.connection != null) {
                plugin.connection.close();
            }
        } catch (SQLException e) {
            plugin.error("Failed to stop mysql connection on reload");
            e.printStackTrace();
        }
        plugin.initializeDBConnection();
        // Notify console
        PluginDescriptionFile pdf = plugin.getDescription();
        if (commandSender instanceof Player) {
            plugin.log("[" + pdf.getName() + "] v" + pdf.getVersion());
            plugin.log(pdf.getName() + " reloaded");
        }
        // Notify sender
        sendMessage(commandSender,"&6[" + pdf.getName() + "] v" + pdf.getVersion());
        sendMessage(commandSender,"&6" + pdf.getName() + " reloaded");
    }

    private void onListCommand(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player && !commandSender.hasPermission(plugin.PERMISSION_LIST)) {
            sendMessage(commandSender, "&4You have do not have permission to execute this command.");
            return;
        }
        sendMessage(commandSender, "List of registered rewards:");
        // List active commands
        try {
            String sql = "SELECT c.command_id, c.command, c.active" +
                    ", COUNT(IF(i.used IS NOT NULL, 1, NULL)) AS used" +
                    ", COUNT(IF(i.used IS NULL, 1, NULL)) AS notUSed " +
                    "FROM `commands` AS c " +
                    "LEFT JOIN `invitation` AS i ON c.`command_id` = i.`command_id` " +
                    "GROUP BY `command_id`";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            while(results.next()) {
                sendMessage(commandSender, "/" + results.getString("command"));
                sendMessage(commandSender, "   active: " + results.getBoolean("active")
                        + ", id: " + results.getInt("command_id")
                        + ", used " + results.getInt("used")
                        + ", not used " + results.getInt("notUsed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendMessage(commandSender,"Failed to fetch commands");
        }
    }

    private void sendMessage(CommandSender commandSender, String msg) {
        plugin.sendMessage(commandSender, msg);
    }
}
