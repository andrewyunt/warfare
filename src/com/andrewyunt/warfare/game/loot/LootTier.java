package com.andrewyunt.warfare.game.loot;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public abstract class LootTier {

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
    private static final ItemStack DIAMOND_CHESTPLATE = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
    private static final ItemStack DIAMOND_BOOTS = new ItemStack(Material.IRON_SWORD, 1);
    private static final ItemStack DIAMOND_BOOTS_FALLING = new ItemStack(Material.IRON_SWORD, 1);
    private static final ItemStack BOW_POWER = new ItemStack(Material.BOW, 1);
    private static final ItemStack POTION_FIRE_RESISTANCE;
    private static final ItemStack POTION_SPEED;

    static {
        BOW.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        IRON_SWORD.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        DIAMOND_CHESTPLATE.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        Potion frPotion = new Potion(PotionType.FIRE_RESISTANCE, 1);
        POTION_FIRE_RESISTANCE = frPotion.toItemStack(1);

        Potion speedPotion = new Potion(PotionType.SPEED, 2);
        POTION_SPEED = speedPotion.toItemStack(1);
    }

    public static class Tier1 extends LootTier {
        @Getter
        private ItemStack[] group1Items = new ItemStack[] {
                new ItemStack(Material.IRON_HELMET, 1),
                new ItemStack(Material.IRON_CHESTPLATE, 1),
                new ItemStack(Material.IRON_LEGGINGS, 1),
                new ItemStack(Material.IRON_BOOTS, 1),
                DIAMOND_CHESTPLATE,
                new ItemStack(Material.DIAMOND_BOOTS, 1),
                new ItemStack(Material.DIAMOND_LEGGINGS, 1),
                new ItemStack(Material.DIAMOND_HELMET, 1)
        };

        @Getter
        private ItemStack[] group2Items = new ItemStack[] {
                new ItemStack(Material.FISHING_ROD, 1),
                new ItemStack(Material.SNOW_BALL, 32),
                new ItemStack(Material.EGG, 32),
                new ItemStack(Material.ENCHANTMENT_TABLE, 4),
                new ItemStack(Material.EXP_BOTTLE, 32),
                new ItemStack(Material.EXP_BOTTLE, 64),
        };

        @Getter
        private ItemStack[] group3Items = new ItemStack[] {
                new ItemStack(Material.GOLDEN_APPLE, 6),
                new ItemStack(Material.ENDER_PEARL, 2),
                new ItemStack(Material.ARROW, 32),
                new ItemStack(Material.TNT, 8)
        };

        @Getter
        private ItemStack[] group4Items = new ItemStack[] {
                new ItemStack(Material.WOOD, 32),
                new ItemStack(Material.COBBLESTONE, 64)
        };

        @Getter
        private ItemStack[] group5Items = new ItemStack[] {
                BOW,
                IRON_SWORD,
                POTION_FIRE_RESISTANCE,
                POTION_SPEED
        };
    }

    public static class Tier2 extends LootTier {
        public ItemStack getItem(LootType type) {
            double random = Math.random();

            switch (type) {
                case HELMET:
                    return new ItemStack(random > .5 ? Material.IRON_HELMET : Material.CHAINMAIL_HELMET);
                case CHESTPLATE:
                    return new ItemStack(random > .5 ? Material.IRON_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE);
                case LEGGINGS:
                    return new ItemStack(random > .5 ? Material.IRON_LEGGINGS : Material.CHAINMAIL_LEGGINGS);
                case BOOTS:
                    return new ItemStack(random > .5 ? Material.IRON_BOOTS : Material.CHAINMAIL_BOOTS);
                case AXE:
                    return new ItemStack(random > .5 ? Material.STONE_AXE : Material.IRON_AXE);
                case PICKAXE:
                    return new ItemStack(random > .5 ? Material.STONE_PICKAXE : Material.IRON_PICKAXE);
                case SWORD:
                    return new ItemStack(random > .5 ? Material.IRON_SWORD : Material.STONE_SWORD);
                case FISHING_ROD:
                    return new ItemStack(Material.FISHING_ROD);
                case BOW:
                    return new ItemStack(Material.BOW);
                case ARROWS:
                    return new ItemStack(Material.ARROW, 16);
                case POTION:
                    return random > .5 ? POTION_FIRE_RESISTANCE : POTION_SPEED;
                case BLOCKS:
                    return new ItemStack(random > .5 ? Material.WOOD : Material.STONE, Math.random() > .5 ? 64 : 32);
                case FOOD:
                    return new ItemStack(random > .5 ? Material.COOKED_BEEF : Material.BREAD, Math.random() > .5 ? 64 : 32);
                case ENDER_PEARL:
                    new ItemStack(Material.ENDER_PEARL, 2);
                    break;
                case GOLDEN_APPLE:
                    return new ItemStack(Material.GOLDEN_APPLE, 4);
                case THROWABLE:
                    return new ItemStack(random > .5 ? Material.SNOW_BALL : Material.EGG, 16);
                case LAVA_BUCKET:
                    return new ItemStack(Material.LAVA_BUCKET);
                case WATER_BUCKET:
                    return new ItemStack(Material.WATER_BUCKET);
                case FLINT_AND_STEEL:
                    return new ItemStack(Material.FLINT_AND_STEEL);
                case ENCHANTMENT_TABLE:
                    return new ItemStack(Material.ENCHANTMENT_TABLE);
            }

            return null;
        }
    }

    public static class Tier3 extends LootTier {
        public ItemStack getItem(LootType type) {
            double random = Math.random();

            switch (type) {
                case HELMET:
                    return new ItemStack(random > .5 ? Material.LEATHER_HELMET : Material.GOLD_HELMET);
                case CHESTPLATE:
                    return new ItemStack(random > .5 ? Material.LEATHER_CHESTPLATE : Material.GOLD_CHESTPLATE);
                case LEGGINGS:
                    return new ItemStack(random > .5 ? Material.LEATHER_LEGGINGS : Material.GOLD_LEGGINGS);
                case BOOTS:
                    return new ItemStack(random > .5 ? Material.LEATHER_BOOTS : Material.GOLD_BOOTS);
                case AXE:
                    return new ItemStack(random > .5 ? Material.WOOD_AXE : Material.STONE_AXE);
                case PICKAXE:
                    return new ItemStack(random > .5 ? Material.WOOD_PICKAXE : Material.STONE_AXE);
                case SWORD:
                    return new ItemStack(random > .5 ? Material.STONE_SWORD : Material.WOOD_SWORD);
                case FISHING_ROD:
                    return new ItemStack(Material.FISHING_ROD);
                case BOW:
                    return new ItemStack(Material.BOW);
                case ARROWS:
                    return new ItemStack(Material.ARROW, 8);
                case POTION:
                    return random > .5 ? POTION_FIRE_RESISTANCE : POTION_SPEED;
                case BLOCKS:
                    return new ItemStack(random > .5 ? Material.WOOD : Material.STONE, Math.random() > .5 ? 32 : 16);
                case FOOD:
                    return new ItemStack(random > .5 ? Material.COOKED_BEEF : Material.BREAD, Math.random() > .5 ? 16 : 8);
                case ENDER_PEARL:
                    return new ItemStack(Material.ENDER_PEARL, 1);
                case GOLDEN_APPLE:
                    return new ItemStack(Material.GOLDEN_APPLE, 2);
                case THROWABLE:
                    return new ItemStack(random > .5 ? Material.SNOW_BALL : Material.EGG, Math.random() > .5 ? 16 : 8);
                case WATER_BUCKET:
                    return new ItemStack(Material.WATER_BUCKET);
                case LAVA_BUCKET:
                    return new ItemStack(Material.LAVA_BUCKET);
                case FLINT_AND_STEEL:
                    return new ItemStack(Material.FLINT_AND_STEEL);
                case ENCHANTMENT_TABLE:
                    return new ItemStack(Material.ENCHANTMENT_TABLE);
            }

            return null;
        }
    }
}