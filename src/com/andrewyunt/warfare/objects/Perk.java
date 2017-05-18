package com.andrewyunt.warfare.objects;

import org.bukkit.inventory.ItemStack;

/**
 * The enumeration for abilities, their names, and the method to use them.
 *
 * @author Andrew Yunt
 */
public enum Perk implements Purchasable {

    RESIST("Resist"),
    SWIFTNESS("Swiftness"),
    BOOMERANG("Boomerang"),
    WEAKENING_ARROW("Weakening Arrow"),
    RECHARGE("Recharge"),
    FLURRY("Flurry"),
    EXPLOSIVE_WEAKNESS("Explosive Weakness"),
    SUPPORT("Support"),
    WEAKENING_SWING("Weakening Swing"),
    SWIFT_BACKUP("Swift Backup"),
    SOUL_SUCKER("Soul Sucker"),
    UNDEAD("Undead");

    final String name;

    Perk(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public int getPrice(int level) {

        return 0;
    }

    @Override
    public ItemStack getDisplayItem() {

        return null;
    }
}