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
