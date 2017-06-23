package com.andrewyunt.warfare.game.loot;

/**
 * Type of loot in a chest, used for islands to guarantee loot over a group of chests.
 */
public enum LootType {

    HELMET(true),
    CHESTPLATE(true),
    LEGGINGS(true),
    BOOTS(true),
    AXE(true),
    PICKAXE(true),
    SWORD(true),
    FISHING_ROD(false),
    BOW(false),
    ARROWS(false),
    POTION(true),
    BLOCKS(true),
    FOOD(true),
    ENDER_PEARL(false),
    GOLDEN_APPLE(false),
    THROWABLE(false),
    WATER_BUCKET(false),
    LAVA_BUCKET(false),
    FLINT_AND_STEEL(false),
    ENCHANTMENT_TABLE(false);

    private boolean guaranteed;

    LootType(boolean guaranteed) {
        this.guaranteed = guaranteed;
    }

    public boolean isGuaranteed() {
        return guaranteed;
    }
}