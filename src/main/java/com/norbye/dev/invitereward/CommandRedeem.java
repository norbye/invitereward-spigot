package com.norbye.dev.invitereward;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.sql.*;

public class CommandRedeem implements CommandExecutor {
    private Main plugin;

    public CommandRedeem(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendMessage(commandSender, "Usage: /redeem <code>");
            return true;
        }
        onRewardCommand(commandSender, args);
        return true;
    }

    private void onRewardCommand(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player)) {
            // Limit to version info
            PluginDescriptionFile pdf = plugin.getDescription();
            sendMessage(commandSender,"&6[" + pdf.getName() + "] v" + pdf.getVersion());
            sendMessage(commandSender,"&4Command can only be ran as a player");
            return;
        }
        Player player = (Player) commandSender;

        if (args.length != 1) {
            // Error, should only pass one variable
            PluginDescriptionFile pdf = plugin.getDescription();
            sendMessage(player,"&6[" + pdf.getName() + "] v" + pdf.getVersion());
            sendMessage(player,"&4Usage: /redeem <code>");
            return;
        }

        // Verify that the db is connected
        if (plugin.connection == null) {
            sendMessage(player,"Failed to connect to database. Contact an administrator.");
            return;
        }

        // Check if the reward code is active in the db
        String rewardCode = args[0];
        String rewardCommand;
        int commandId;
        try {
            String sql = "SELECT i.`used`, c.`command_id`, c.`command` FROM invitation AS i " +
                    "LEFT JOIN `commands` AS c ON i.`command_id` = c.`command_id` " +
                    "WHERE i.`invite_id`=? AND c.active=1";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            stmt.setString(1, rewardCode);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                plugin.debug("Invalid reward code");
                sendMessage(player,"Invalid reward code");
                return;
            } else {
                if (results.getTimestamp("used") != null) {
                    sendMessage(player,"The reward code inserted has already been used");
                    return;
                }
                commandId = results.getInt("command_id");
                rewardCommand = results.getString("command");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendMessage(player,"Failed to verify reward code");
            return;
        }
        if ("".equals(rewardCommand)) {
            sendMessage(player,"Failed to execute reward");
            return;
        }
        if (rewardCommand == null) {
            sendMessage(player,"Invalid reward");
            return;
        }
        // Verify that the reward has not already been used
        try {
            String sql = "SELECT `invite_id` FROM `invitation` WHERE `command_id` = ? AND `used_by` = ?";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            stmt.setInt(1, commandId);
            stmt.setString(2, player.getUniqueId().toString());
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                sendMessage(player,"You have already gotten this reward..");
                return;
            }
        } catch (SQLException e) {
            sendMessage(player,"Failed to verify reward code");
            return;
        }
        // Replace variables
        rewardCommand = rewardCommand.replaceAll("\\{playername}", player.getName());
        // Update db that the command was executed
        try {
            String sql = "UPDATE `invitation` SET `used`=?, `used_by`=? WHERE `invite_id`=?";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, String.valueOf(player.getUniqueId()));
            stmt.setString(3, rewardCode);
            int updatedIndex = stmt.executeUpdate();
            if (updatedIndex > 0) {
                plugin.debug("Successfully registered reward as used");
                // Execute command
                plugin.log("Command executed: /" + rewardCommand);
                sendMessage(player, "Reward granted!");
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
        }
    }

    private void sendMessage(CommandSender commandSender, String msg) {
        plugin.sendMessage(commandSender, msg);
    }
}
