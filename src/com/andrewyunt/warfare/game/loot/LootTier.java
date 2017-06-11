package com.andrewyunt.warfare.game.loot;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class LootTier {

    public byte getNum() {
        if (this instanceof Tier1) {
            return 1;
        } else if (this instanceof Tier2) {
            return 2;
        } else if (this instanceof Tier3) {
            return 3;
        } else {
            return 0;
        }
    }

    private static final ItemStack BOW = new ItemStack(Material.BOW, 1);
    private static final ItemStack IRON_SWORD = new ItemStack(Material.STONE_SWORD, 1);
    private static final ItemStack DIAMOND_SWORD = new ItemStack(Material.IRON_SWORD, 1);
    private static final ItemStack DIAMOND_CHESTPLATE = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
    private static final ItemStack DIAMOND_BOOTS = new ItemStack(Material.IRON_SWORD, 1);
    private static final ItemStack DIAMOND_BOOTS_FALLING = new ItemStack(Material.IRON_SWORD, 1);
    private static final ItemStack BOW_POWER = new ItemStack(Material.BOW, 1);
    private static final ItemStack POTION_FIRE_RESISTANCE = new ItemStack(Material.POTION, 3);
    private static final ItemStack POTION_SPEED = new ItemStack(Material.POTION, 3);

    static {
        BOW.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        IRON_SWORD.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        DIAMOND_SWORD.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        DIAMOND_CHESTPLATE.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        //DIAMOND_BOOTS.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        //DIAMOND_BOOTS_FALLING.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        //DIAMOND_BOOTS_FALLING.addEnchantment(Enchantment.PROTECTION_FALL, 2);

        PotionMeta frMeta = (PotionMeta) POTION_FIRE_RESISTANCE.getItemMeta();
        PotionEffect frEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9600, 0, false);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "Fire Resistance");
        frMeta.setLore(lore);
        frMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Fire Resistance Potion");
        frMeta.setMainEffect(PotionEffectType.FIRE_RESISTANCE);
        frMeta.addCustomEffect(frEffect, true);
        POTION_FIRE_RESISTANCE.setItemMeta(frMeta);

        PotionMeta speedPotionMeta = (PotionMeta) POTION_SPEED.getItemMeta();
        PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 1800, 1, false);
        lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "Speed 2");
        speedPotionMeta.setLore(lore);
        speedPotionMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Speed Potion");
        speedPotionMeta.setMainEffect(PotionEffectType.SPEED);
        speedPotionMeta.addCustomEffect(speedEffect, true);
        POTION_SPEED.setItemMeta(speedPotionMeta);
    }

    public static class Tier1 extends LootTier {
        @Getter
        private ItemStack[] group1Items = new ItemStack[] {
                new ItemStack(Material.FISHING_ROD, 1),
                new ItemStack(Material.DIAMOND_HELMET, 1),
                new ItemStack(Material.DIAMOND_LEGGINGS, 1),
                new ItemStack(Material.ARROW, 64),
                new ItemStack(Material.TNT, 16),
                new ItemStack(Material.SNOW_BALL, 64),
                new ItemStack(Material.ENDER_PEARL, 2),
                new ItemStack(Material.ENDER_PEARL, 4),
                new ItemStack(Material.GOLDEN_APPLE, 2),
                new ItemStack(Material.GOLDEN_APPLE, 4),
                DIAMOND_SWORD,
                BOW_POWER,
                DIAMOND_CHESTPLATE,
                DIAMOND_BOOTS,
                DIAMOND_BOOTS_FALLING
        };
    }

    public static class Tier2 extends LootTier {
        @Getter
        private ItemStack[] group1Items = new ItemStack[] {
                new ItemStack(Material.FISHING_ROD, 1),
                new ItemStack(Material.IRON_HELMET, 1),
                new ItemStack(Material.IRON_CHESTPLATE, 1),
                new ItemStack(Material.IRON_LEGGINGS, 1),
                new ItemStack(Material.IRON_BOOTS, 1),
                new ItemStack(Material.SNOW_BALL, 16),
                new ItemStack(Material.COOKED_BEEF, 16),
                new ItemStack(Material.LOG, 64),
                new ItemStack(Material.ENCHANTMENT_TABLE, 1),
                new ItemStack(Material.EXP_BOTTLE, 32),
                new ItemStack(Material.GOLDEN_APPLE, 1),
                new ItemStack(Material.ENDER_PEARL, 1),
                new ItemStack(Material.ARROW, 32),
                BOW
        };

        @Getter
        private ItemStack[] group2Items = new ItemStack[] {
                new ItemStack(Material.WOOD, 64),
                new ItemStack(Material.COBBLESTONE, 64),
                POTION_FIRE_RESISTANCE,
                POTION_SPEED
        };

        @Getter
        private ItemStack[] group3Items = new ItemStack[] {
                IRON_SWORD,
                new ItemStack(Material.IRON_SWORD, 1),
                new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
                new ItemStack(Material.DIAMOND_BOOTS, 1),
                new ItemStack(Material.WATER_BUCKET, 1),
                new ItemStack(Material.LAVA_BUCKET, 1)
        };
    }

    public static class Tier3 extends LootTier {
        @Getter
        private ItemStack[] group1Items = new ItemStack[] {
                new ItemStack(Material.IRON_HELMET, 1),
                new ItemStack(Material.IRON_CHESTPLATE, 1),
                new ItemStack(Material.IRON_LEGGINGS, 1),
                new ItemStack(Material.IRON_BOOTS, 1),
                new ItemStack(Material.FISHING_ROD, 1),
                new ItemStack(Material.SNOW_BALL, 16),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.ARROW, 16)
        };

        @Getter
        private ItemStack[] group2Items = new ItemStack[] {
                new ItemStack(Material.WOOD, 16),
                new ItemStack(Material.WOOD, 32),
                new ItemStack(Material.COBBLESTONE, 16),
                new ItemStack(Material.COBBLESTONE, 32),
                new ItemStack(Material.IRON_AXE, 1),
                new ItemStack(Material.WOOD_SWORD, 1),
                new ItemStack(Material.STONE_SWORD, 1)
        };

        @Getter
        private ItemStack[] group3Items = new ItemStack[] {
                new ItemStack(Material.COOKED_BEEF, 4),
                new ItemStack(Material.COOKED_BEEF, 16),
                new ItemStack(Material.BREAD, 4),
                new ItemStack(Material.BREAD, 16)
        };

        @Getter
        private ItemStack[] group4Items = new ItemStack[] {
                new ItemStack(Material.WATER_BUCKET, 1),
                new ItemStack(Material.LAVA_BUCKET, 1),
                new ItemStack(Material.FLINT_AND_STEEL, 1)
        };
    }
}