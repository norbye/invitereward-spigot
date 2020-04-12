package com.norbye.dev.invitereward;

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
        plugin.initialiseDBConnection();
        // Notify console
        PluginDescriptionFile pdf = plugin.getDescription();
        if (commandSender instanceof Player) {
            plugin.log("[" + pdf.getName() + "] v" + pdf.getVersion());
            plugin.log(pdf.getName() + " reloaded");
        }
        // Notify sender
        commandSender.sendMessage(ChatColor.GOLD + "[" + pdf.getName() + "] v" + pdf.getVersion());
        commandSender.sendMessage(ChatColor.GOLD + pdf.getName() + " reloaded");
        return false;
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

        // TODO update the db connection stuffs
        // Check if the reward code is active in the db
        String rewardCode = args[0];
        try {
            String sql = "SELECT * FROM invitations WHERE Something='Something'";
            PreparedStatement stmt = plugin.connection.prepareStatement(sql);
            ResultSet results = stmt.executeQuery();
            if (!results.next()) {
                System.out.println("Failed");
            } else {
                System.out.println("Success");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
