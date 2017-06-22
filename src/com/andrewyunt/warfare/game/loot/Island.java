package com.andrewyunt.warfare.game.loot;

import lombok.Getter;
import java.util.*;

/**
 * Used to represent a group of chests on an island. Only use islands to group tier 2 and 3
 * chests on islands where players spawn.
 */
public class Island {

    @Getter private final String name;
    @Getter private final List<LootChest> lootChests = new ArrayList<>();
    @Getter private final Map<LootChest, Set<LootType>> chestItems = new HashMap<>();

    public Island(String name) {
        this.name = name;
    }

    public void randomizeItems() {
        for (LootType lootType : LootType.values()) {
            Collections.shuffle(lootChests);
            chestItems.get(lootChests.get(0)).add(lootType);
        }
    }

    public void addChest(LootChest chest) {
        lootChests.add(chest);
        chestItems.put(chest, new HashSet<>());
    }
}