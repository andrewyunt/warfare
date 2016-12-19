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
package com.andrewyunt.skywarfare.db;

import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import com.andrewyunt.skywarfare.objects.CustomClass;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Upgradable;

public abstract class DataSource {
	
	public abstract boolean connect();
	
	public abstract void disconnect();
	
	public abstract void savePlayer(GamePlayer player);
	
	public abstract void loadPlayer(GamePlayer player);
	
	public abstract void saveLayout(GamePlayer player, CustomClass customClass, Inventory inv);
	
	public abstract Inventory loadLayout(GamePlayer player, CustomClass customClass);

	public abstract void setLevel(GamePlayer player, Upgradable upgradable, int level);
	
	public abstract int getLevel(GamePlayer player, Upgradable upgradable);
	
	public abstract Map<Integer, Map.Entry<OfflinePlayer, Integer>> getMostKills(boolean weekly,
			boolean finalKill, CustomClass classType);
	
	public void updateDB() {
		
		createPlayersTable();
		createLayoutsTable();
		createUpgradesTable();
	}
	
	public abstract void createPlayersTable();

	public abstract void createLayoutsTable();
	
	public abstract void createUpgradesTable();
}