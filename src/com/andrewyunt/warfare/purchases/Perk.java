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
    SUPPORT("Support"),
    WEAKENING_SWING("Weakening Swing"),
    SWIFT_BACKUP("Swift Backup"),
    SOUL_SUCKER("Soul Sucker"),
    UNDEAD("Undead");

    private final String name;

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

    @Override
    public PurchaseType getType() {
        return PurchaseType.PERK;
    }

    @Override
    public ItemStack getDisplayItem() {
        switch (this) {
            case RESIST:
                return new ItemStack(Material.IRON_INGOT);
            case SWIFTNESS:
                return new ItemStack(Material.SUGAR);
            case BOOMERANG:
                return new ItemStack(Material.LEASH);
            case WEAKENING_ARROW:
                return new ItemStack(Material.BLAZE_ROD);
            case RECHARGE:
                return new Potion(PotionType.REGEN, 2).toItemStack(1);
            case FLURRY:
                return new ItemStack(Material.FEATHER);
            case SUPPORT:
                return new ItemStack(Material.TNT);
            case WEAKENING_SWING:
                return new Potion(PotionType.WEAKNESS, 2).toItemStack(1);
            case SWIFT_BACKUP:
                return new ItemStack(Material.MONSTER_EGG, 1, (short) 95);
            case SOUL_SUCKER:
                return new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
            case UNDEAD:
                return new ItemStack(Material.ROTTEN_FLESH);
            default:
                return null;
        }
    }
}