/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Andrew Yunt. The intellectual and technical concepts contained
 * herein are proprietary to Andrew Yunt and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Andrew Yunt. Access to the source code contained herein is hereby forbidden to anyone except current Andrew Yunt and those who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ANDREW YUNT IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
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
		if (event.getEntityType() != EntityType.GHAST && event.getEntityType() != EntityType.PLAYER
				&& event.getEntityType() != EntityType.DROPPED_ITEM) {
            event.getEntity().remove();
        }
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){
        event.setCancelled(true);
	}
	
	@EventHandler
	private void onEntityTarget(EntityTargetEvent event) {
		if(event.getTarget() instanceof Player) {
			Player target = (Player) event.getTarget();
			if (event.getEntity() instanceof Ghast) {
				GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(target);

				if (gp.getGhasts().contains(event.getEntity().getUniqueId())) {
					event.setCancelled(true);
					event.setTarget(null);
				}
			}
		}
	}
}