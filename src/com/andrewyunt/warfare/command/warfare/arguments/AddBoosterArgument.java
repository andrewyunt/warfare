package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Booster;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;

public class AddBoosterArgument extends CommandArgument {

    public AddBoosterArgument() {
        super("addbooster", "Give a player a coin booster");

        isPlayerOnly = true;
        permission = "warfare.addbooster";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(args.length >= 4)) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addbooster [player] [level] [duration]");
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> {
            OfflinePlayer targetPlayer = Bukkit.getServer().getPlayer(args[1]);
            GamePlayer targetGP = Warfare.getInstance().getPlayerManager().getPlayer(targetPlayer.getUniqueId());

            int boost;
            int duration;

            try {
                boost = Integer.valueOf(args[2]);
                duration = Integer.valueOf(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Usage: /warfare addbooster [player] [amount]");
                return;
            }

            targetGP.getBoosters().add(new Booster(boost, LocalDateTime.now().plusHours(duration)));

            sender.sendMessage(String.format(ChatColor.YELLOW + "You set " + ChatColor.GOLD + "%s" + ChatColor.YELLOW
                            + "'s booster level to " + ChatColor.GOLD + "%s"+ ChatColor.YELLOW + " for "
                            + ChatColor.GOLD + "%s" + ChatColor.YELLOW + " hours.",
                    targetGP.getBukkitPlayer().getDisplayName(),
                    String.valueOf(boost),
                    String.valueOf(duration)));

            if (!targetPlayer.isOnline()) {
                Warfare.getInstance().getPlayerManager().deletePlayer(targetGP);
            }
        });

        return true;
    }
}