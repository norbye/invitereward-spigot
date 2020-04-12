package com.norbye.dev.invitereward;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            return onReloadCommand(commandSender);
        }
        return onRewardCommand(commandSender, args);
    }

    private boolean onReloadCommand(CommandSender commandSender) {
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
        commandSender.sendMessage(ChatColor.GOLD + "[" + pdf.getName() + "] v" + pdf.getVersion());
        commandSender.sendMessage(ChatColor.GOLD + pdf.getName() + " reloaded");
        return true;
    }

    private boolean onRewardCommand(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player)) {
            // Limit to version info
            PluginDescriptionFile pdf = plugin.getDescription();
            commandSender.sendMessage(ChatColor.GOLD + "[" + pdf.getName() + "] v" + pdf.getVersion());
            commandSender.sendMessage(ChatColor.DARK_RED + "Command can only be ran as a player");
            return true;
        }
        Player p = (Player) commandSender;

        if (args.length != 1) {
            // Error, should only pass one variable
            PluginDescriptionFile pdf = plugin.getDescription();
            p.sendMessage(ChatColor.GOLD + "[" + pdf.getName() + "] v" + pdf.getVersion());
            p.sendMessage(ChatColor.DARK_RED + "Usage: /invitereward <reward-code>");
            return true;
        }

        // Check if the reward code is active in the db
        String rewardCode = args[0];
        String rewardCommand = "";
        try {
            String sql = "SELECT i.`used`, c.`command` FROM invitations AS i " +
                    "LEFT JOIN `commands` AS c ON i.`command_id` = c.`command_id` " +
                    "WHERE i.`invite_id`=? AND c.active=1";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            stmt.setString(1, rewardCode);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                plugin.debug("Invalid reward code");
                commandSender.sendMessage("Invalid reward code");
                return true;
            } else {
                if (results.getTimestamp("used") != null) {
                    commandSender.sendMessage("The reward code inserted has already been used");
                    return true;
                }
                rewardCommand = results.getString("command");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            commandSender.sendMessage("Failed to verify reward code");
            return true;
        }
        if ("".equals(rewardCommand)) {
            commandSender.sendMessage("Failed to execute reward");
            return true;
        }
        if (rewardCommand == null) {
            commandSender.sendMessage("Invalid reward");
            return true;
        }
        // Replace variables
        rewardCommand = rewardCommand.replaceAll("\\{playername\\}", commandSender.getName());
        commandSender.sendMessage("Command: /" + rewardCommand);
        // Update db that the command was executed
        try {
            String sql = "UPDATE `invitations` SET `used`=?, `used_by`=? WHERE `invite_id`=?";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, String.valueOf(((Player) commandSender).getUniqueId()));
            stmt.setString(3, rewardCode);
            int updatedIndex = stmt.executeUpdate();
            if (updatedIndex > 0) {
                plugin.debug("Successfully registered reward as used");
                // Execute command
                plugin.log("Command executed: /" + rewardCommand);
                Bukkit.getServer().dispatchCommand(
                        Bukkit.getServer().getConsoleSender(),
                        rewardCommand
                );
            } else {
                plugin.error("Failed to register reward as used");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.error("Failed to cancel code");
            return true;
        }
        return true;
    }
}
