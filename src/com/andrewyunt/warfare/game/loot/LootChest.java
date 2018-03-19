package com.andrewyunt.warfare.game.loot;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LootChest {

	@Getter private Location location;
	@Getter private LootTier tier;
	@Getter private Island island;

	public LootChest(Location location, byte tier, Island island) {
		this.location = location;

		if (tier == 1) {
			this.tier = new LootTier.Tier1();
		} else if (tier == 2) {
			this.tier = new LootTier.Tier2();
		} else if (tier == 3) {
			this.tier = new LootTier.Tier3();
		}

		this.island = island;

		if (island != null) {
			island.addChest(this);
		}
	}
	
	private ItemStack getRandomLootItem(ItemStack[] group) {
		List<ItemStack> groupArray = Arrays.asList(group);
		Collections.shuffle(groupArray);
		return groupArray.iterator().next();
	}
	
	public void fill() {
		Block block = location.getBlock();
		BlockState blockState = block.getState();

		if (blockState instanceof Chest) {
			Chest chest = (Chest) blockState;
			Inventory inv = chest.getBlockInventory();
			List<ItemStack> lootItems = new ArrayList<>();

			// Add a random item from guaranteed groups for each tier and give island items
			if (tier instanceof LootTier.Tier3) {
				for (LootType type : island.getChestItems().get(this)) {
					if (type.isGuaranteed() || (!type.isGuaranteed() && Math.random() > .5)) {
						lootItems.add(((LootTier.Tier3) tier).getItem(type));
					}
				}
			} else if (tier instanceof LootTier.Tier2) {
				for (LootType type : island.getChestItems().get(this)) {
					if (type.isGuaranteed() || (!type.isGuaranteed() && Math.random() > .5)) {
						lootItems.add(((LootTier.Tier2) tier).getItem(type));
					}
				}
			} else if (tier instanceof LootTier.Tier1) {
				lootItems.add(getRandomLootItem(((LootTier.Tier1) tier).getGroup1Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier1) tier).getGroup2Items()));
				lootItems.add(getRandomLootItem(((LootTier.Tier1) tier).getGroup3Items()));
			}

			// Give players items from two randomly chosen groups if the chest is tier 1
			if (tier instanceof LootTier.Tier1) {
				for (int i = 1; i < 3; i++) {
					List<ItemStack[]> toAdd = new ArrayList<>();

					toAdd.add(((LootTier.Tier1) tier).getGroup1Items());
					toAdd.add(((LootTier.Tier1) tier).getGroup2Items());
					toAdd.add(((LootTier.Tier1) tier).getGroup3Items());
					toAdd.add(((LootTier.Tier1) tier).getGroup4Items());
					toAdd.add(((LootTier.Tier1) tier).getGroup5Items());

					// Shuffle the randomly chosen group
					Collections.shuffle(toAdd);

					// The array of items from the group converted to a list
					List<ItemStack> toAddList = new LinkedList<>(Arrays.asList(toAdd.iterator().next()));

					// The number of items a player should receive from the randomly chosen group
					int random = -ThreadLocalRandom.current().nextInt(4 - 3 + 1) + 4;

					while (toAddList.size() > random) {
						toAddList.remove(toAddList.size() - 1);
					}

					lootItems.addAll(toAddList.stream().filter(addItem -> !lootItems.stream().map(ItemStack::getType)
							.collect(Collectors.toSet()).contains(addItem.getType())).collect(Collectors.toSet()));
				}
			}

			// Randomize item arrangement in chest inventory
			List<Integer> slots = new ArrayList<>();
			for (int i = 0; i < inv.getSize(); i ++) {
			    slots.add(i);
            }
            Collections.shuffle(slots);
			for (ItemStack is : lootItems) {
				int randomSlot = slots.remove(0);
				inv.setItem(randomSlot, is);
			}

			// Update chest
			chest.update();
		}
	}
}