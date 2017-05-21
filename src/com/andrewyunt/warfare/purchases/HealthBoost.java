
package com.andrewyunt.warfare.purchases;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum HealthBoost implements Purchasable {
	
	HEALTH_BOOST_I("Health Boost I", 250000),
	HEALTH_BOOST_II("Health Boost II", 500000),
	HEALTH_BOOST_III("Health Boost III", 1000000);

	private final String name;
	private final int price;
	
	HealthBoost(String name, int price) {
		
		this.name = name;
		this.price = price;
	}
	
	@Override
	public String getName() {
		
		return name;
	}

	@Override
	public int getPrice(int level) {
		
		return price;
	}

	@Override
	public ItemStack getDisplayItem() {
		
		return new ItemStack(Material.GOLDEN_APPLE);
	}


	public PurchaseType getType() {
		return PurchaseType.HEALTH_BOOST;
	}
}