package com.andrewyunt.warfare.player;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public enum Kit {
	
	UHC("UHC"),
	POT("Pot"),
	SOUP("Soup");
	
	@Getter private final String name;
	
	Kit(String name) {
		
		this.name = name;
	}
	
	public void giveItems(GamePlayer player) {
		
		PlayerInventory inv = player.getBukkitPlayer().getInventory();
		
		if (this == UHC) {
			inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
			inv.setItem(1, new ItemStack(Material.FISHING_ROD, 1));
			inv.setItem(2, new ItemStack(Material.IRON_PICKAXE, 1));
			inv.setItem(3, new ItemStack(Material.COBBLESTONE, 32));
			inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 6));
		} else if (this == POT) {
			inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
			Potion speedPotion = new Potion(PotionType.SPEED, 2);
			inv.setItem(1, speedPotion.toItemStack(1));
			Potion healPotion = new Potion(PotionType.INSTANT_HEAL, 2);
			healPotion.setSplash(true);
			for (int i = 2; i < 7; i++) {
                inv.setItem(i, healPotion.toItemStack(1));
            }
			inv.setItem(8, new ItemStack(Material.BAKED_POTATO, 5));
		} else if (this == SOUP) {
			inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
			inv.setItem(1, new ItemStack(Material.ENCHANTMENT_TABLE, 1));
			inv.setItem(2, new ItemStack(Material.EXP_BOTTLE, 16));
			for (int i = 3; i < 8; i++) {
                inv.setItem(i, new ItemStack(Material.MUSHROOM_SOUP, 1));
            }
			inv.setItem(8, new ItemStack(Material.MELON));
		}
	}
}