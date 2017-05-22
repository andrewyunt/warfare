package com.andrewyunt.warfare.purchases;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

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

        return 15000;
    }


    public PurchaseType getType() {
        return PurchaseType.PERK;
    }

    @Override
    public ItemStack getDisplayItem() {

        if (this == RESIST) {
            return new ItemStack(Material.IRON_INGOT);
        } else if (this == SWIFTNESS) {
            return new ItemStack(Material.SUGAR);
        } else if (this == BOOMERANG) {
            return new ItemStack(Material.LEASH);
        } else if (this == WEAKENING_ARROW) {
            return new ItemStack(Material.BLAZE_ROD);
        } else if (this == RECHARGE) {
            return new Potion(PotionType.REGEN, 2).toItemStack(1);
        } else if (this == FLURRY) {
            return new ItemStack(Material.FEATHER);
        } else if (this == EXPLOSIVE_WEAKNESS) {
            return new ItemStack(Material.TNT);
        } else if (this == SUPPORT) {
            return new Potion(PotionType.WEAKNESS, 2).toItemStack(1);
        } else if (this == WEAKENING_SWING) {
            return new ItemStack(Material.MONSTER_EGG, 1, (short) 95);
        } else if (this == SWIFT_BACKUP) {
            return new Potion(PotionType.INSTANT_HEAL, 2).toItemStack(1);
        } else if (this == SOUL_SUCKER) {
            return new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
        } else if (this == UNDEAD) {
            return new ItemStack(Material.ROTTEN_FLESH);
        }

        return null;
    }
}