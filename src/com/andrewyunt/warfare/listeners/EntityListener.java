
package com.andrewyunt.warfare.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

public class EntityListener implements Listener {
	@EventHandler
	private void onEntitySpawn(EntitySpawnEvent event) {
		if (event.getEntityType() != EntityType.PLAYER && event.getEntityType() != EntityType.DROPPED_ITEM) {
            event.getEntity().remove();
        }
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){
        event.setCancelled(true);
	}
}