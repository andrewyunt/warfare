
package com.andrewyunt.warfare.purchases;

import org.bukkit.inventory.ItemStack;

/**
 * The Purchasable interface is used to guarantee that the classes / enums
 * implementing it will have the same methods and will be able to be used
 * for the methods that use Purchasable as a parameter.
 * 
 * @author Andrew Yunt
 */
public interface Purchasable {
	
	/**
	 * @return The display name of the Purchasable.
	 */
	String getName();
	
	/**
	 * @param level The level of the upgradable if applicable. If not applicable, then enter 0.
	 * @return The price of the purchasable
	 */
	int getPrice(int level);
	
	/**
	 * @return The display item of the Purchasable.
	 */
	ItemStack getDisplayItem();

	PurchaseType getType();
}