package com.andrewyunt.warfare.listeners.fixes;

import com.andrewyunt.warfare.Warfare;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class MobFixer implements Listener {
    private final Warfare plugin;

    public MobFixer(Warfare plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onCreatureSpawn(CreatureSpawnEvent e){
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onCreatureSpawn(ExplosionPrimeEvent e){
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onCreatureSpawn(EntityExplodeEvent e){
        e.setCancelled(true);
    }
}
