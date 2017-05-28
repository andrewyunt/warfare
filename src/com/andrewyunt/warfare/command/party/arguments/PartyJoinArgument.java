package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyJoinArgument extends CommandArgument {

    public PartyJoinArgument() {
        super("join", "Join an exiting party");

        this.isPlayerOnly = true;
        permission = "warfare.party.join";
    }

    public String getUsage(String s) {
        return "/" + s + " " + getName() + " <party>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + getUsage(label));
            return true;
        }

        Player player = (Player) sender;

        if (Warfare.getInstance().getPartyManager().getParty(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.YELLOW + "You are already in a party");
            return false;
        }

        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[1]);

        if (target == null) {
            player.sendMessage(ChatColor.YELLOW + "Party not found");
            return false;
        }

        Party targetParty = Warfare.getInstance().getPartyManager().getParty(target.getUniqueId());

        if (targetParty == null){
            player.sendMessage(ChatColor.YELLOW + "Party not found");
            return false;
        }

        if (targetParty.getMembers().size() >= 100) {
            player.sendMessage(ChatColor.YELLOW + "This party is full");
        } else if (!targetParty.getInvites().contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are not invited to this party");
        } else {
            targetParty.getMembers().add(player.getUniqueId());
        }

        Warfare.getInstance().getStorageManager().saveParty(targetParty);

        return true;
    }
}