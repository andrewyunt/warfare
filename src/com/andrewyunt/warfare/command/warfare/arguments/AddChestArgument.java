package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.Arena;
import com.andrewyunt.warfare.objects.LootChest;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class AddChestArgument extends CommandArgument {

    public AddChestArgument() {

        super("addchest", "Adds a loot chest to the map");

        isPlayerOnly = true;
    }

    public String getUsage(String s) {

        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("warfare.addchest")) {
            sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addchest [tier]");
            return false;
        }

        BlockIterator iterator = new BlockIterator(((Player) sender).getLocation(), ((Player) sender).getEyeHeight());
        Block block = null;

        while (iterator.hasNext()) {
            block = iterator.next();

            if (!block.getType().equals(Material.AIR)) {
                break;
            }
        }

        if (block == null || block.getType() != Material.CHEST) {
            sender.sendMessage(ChatColor.RED + "The target block is not a chest.");
            return false;
        }

        Arena arena = Warfare.getInstance().getArena();

        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "The arena is null.");
            return false;
        }

        try {
            arena.getLootChests().add(new LootChest(block.getLocation(), Byte.valueOf(args[1])));

            sender.sendMessage(ChatColor.GOLD + "Loot chest has been added successfully.");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addchest [tier]");
        }

        Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getMySQLManager().saveArena());

        return true;
    }
}