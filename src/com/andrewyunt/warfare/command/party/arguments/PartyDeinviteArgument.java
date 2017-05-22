package com.andrewyunt.warfare.command.party.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Party;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyDeinviteArgument extends CommandArgument {

    public PartyDeinviteArgument() {

        super("deinvite", "Remove a player's invite to your party");

        this.isPlayerOnly = true;
    }

    public String getUsage(String s)  {

        return "/" + s + " " + getName() + " <name>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {
            sender.sendMessage(ChatColor.YELLOW + getUsage(label));
            return true;
        }

        Player player = (Player) sender;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.YELLOW + "You need to be in a party to do this");
            return false;
        }

        if (party.getLeader() != player.getUniqueId()) {
            player.sendMessage(ChatColor.YELLOW + "You must be the leader of the party to do this");
            return false;
        }

        UUID removeUUID = Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId(); //TODO: Don't use Bukkit.getOfflinePlayer()

        if (!party.getInvites().contains(removeUUID)) {
            player.sendMessage(ChatColor.YELLOW + "That player was not invited");
        } else {
            party.getInvites().remove(removeUUID);
        }

        return true;
    }
}