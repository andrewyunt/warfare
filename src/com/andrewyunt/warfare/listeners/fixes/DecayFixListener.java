package com.andrewyunt.warfare.listeners.fixes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class DecayFixListener implements Listener {

    @EventHandler
    private void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
}