package com.andrewyunt.warfare.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntityListener implements Listener {

	@EventHandler
	private void onEntitySpawn(EntitySpawnEvent event) {
		if (event.getEntityType() != EntityType.PLAYER && event.getEntityType() != EntityType.DROPPED_ITEM) {
            event.getEntity().remove();
        }
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
	}
}