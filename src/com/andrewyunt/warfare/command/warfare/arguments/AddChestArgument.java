package com.andrewyunt.warfare.command.warfare.arguments;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.game.loot.Island;
import com.andrewyunt.warfare.game.loot.LootChest;
import com.faithfulmc.util.command.CommandArgument;
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
        permission = "warfare.addchest";
    }

    @Override
    public String getUsage(String s) {
        return "/" + s + " " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        Game game = Warfare.getInstance().getGame();

        if (game == null) {
            sender.sendMessage(ChatColor.RED + "The arena is null.");
            return false;
        }

        Island island = null;

        try {
            if (game.getIsland(args[2]) == null) {
                island = new Island(args[2]);
                game.getIslands().add(island);
            } else {
                island = game.getIsland(args[2]);
            }
        } catch (IndexOutOfBoundsException e) {
            // do nothing
        }

        try {
            game.getLootChests().add(new LootChest(block.getLocation(), Byte.valueOf(args[1]), island));

            sender.sendMessage(ChatColor.YELLOW + "Loot chest has been added successfully.");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Usage: /warfare addchest [tier]");
        }

        Warfare.getInstance().getStorageManager().saveMap();

        return true;
    }
}