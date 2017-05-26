package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCreateArgument extends CommandArgument {

    public PartyCreateArgument() {

        super("create", "Create a party");

        this.isPlayerOnly = true;
        permission = "warfare.party.create";
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (Warfare.getInstance().getPartyManager().getParty(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.YELLOW + "You are already in a party");
        } else {
            Warfare.getInstance().getPartyManager().createParty(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You created a party");
        }

        return true;
    }
}