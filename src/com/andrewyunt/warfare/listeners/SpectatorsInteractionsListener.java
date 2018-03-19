/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.andrewyunt.warfare.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.material.Gate;
import org.bukkit.material.TrapDoor;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.managers.PlayerManager;
import com.andrewyunt.warfare.player.GamePlayer;

/**
 * Modified class sourced from the Bukkit plugin SpectatorPlus.
 * 
 * @see {@link https://github.com/pgmann/SpectatorPlus}
 * 
 * @author pgmann
 * @author Andrew Yunt
 */
public class SpectatorsInteractionsListener implements Listener {
	
	private final PlayerManager pm = Warfare.getInstance().getPlayerManager();

	/* ** Blocks-related ** */

	/**
	 * Used to prevent spectators from blocking players from placing blocks.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockCanBuild(final BlockCanBuildEvent ev) {
		
		if (!ev.isBuildable()) {
			// Get location of the block that is going to be placed
			Location blockLocation = ev.getBlock().getLocation();
			
			boolean allowed = false; // If there are any actual players there, the event should not be over-wrote.

			for (Player target : Bukkit.getServer().getOnlinePlayers()) {
				if (target.getWorld().equals(ev.getBlock().getWorld())) {
					Location playerLocation = target.getLocation();

					// If the player is at this location
					if (playerLocation.getX() > blockLocation.getBlockX() - 1
							&& playerLocation.getX() < blockLocation.getBlockX() + 1
							&& playerLocation.getZ() > blockLocation.getBlockZ() - 1
							&& playerLocation.getZ() < blockLocation.getBlockZ() + 1
							&& playerLocation.getY() > blockLocation.getBlockY() - 2
							&& playerLocation.getY() < blockLocation.getBlockY() + 1) {
						if (pm.getPlayer(target).isSpectating()) {
							allowed = true;
						} else {
							allowed = false;
							break;
						}
					}
				}
			}
			
			ev.setBuildable(allowed);
		}
	}

	/**
	 * Used to prevent spectators from placing blocks, and to teleport
	 * spectators blocking players from placing blocks.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(final BlockPlaceEvent ev) {
		if (pm.getPlayer(ev.getPlayer()).isSpectating()) {
			ev.setCancelled(true);
			return;
		}

		// Get location of the block that is going to be placed
		Location blockL = ev.getBlock().getLocation();

		for (Player target : Bukkit.getServer().getOnlinePlayers()) {
			// Player spectating & in same world?
			if (pm.getPlayer(target).isSpectating() && target.getWorld().equals(ev.getBlock().getWorld())) {
				Location playerL = target.getLocation();

				// Is this player at the location of the block being placed?
				if (playerL.getX() > blockL.getBlockX() - 1 && playerL.getX() < blockL.getBlockX() + 1
						&& playerL.getZ() > blockL.getBlockZ() - 1 && playerL.getZ() < blockL.getBlockZ() + 1
						&& playerL.getY() > blockL.getBlockY() - 2 && playerL.getY() < blockL.getBlockY() + 1) {
					// The location of the player placing the block is a safe location
					target.teleport(ev.getPlayer(), PlayerTeleportEvent.TeleportCause.PLUGIN);
					target.sendMessage("You were teleported away from a placed block.");
				}
			}
		}
	}

	/**
	 * Used to prevent spectators from breaking blocks.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(final BlockBreakEvent ev) {
		if (pm.getPlayer(ev.getPlayer()).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/* ** Entities-related ** */

	/**
	 * Cancels any damage taken or caused by a spectator.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {

			GamePlayer damagerGP = pm.getPlayer((Player) event.getDamager());

			if (damagerGP != null) {
				if (damagerGP.isSpectating()) {
					event.setCancelled(true);
					return;
				}
			}

			GamePlayer damagedGP = pm.getPlayer((event.getEntity()).getUniqueId());

			if (damagedGP != null) {
				if (damagedGP.isSpectating()) {
					event.setCancelled(true);
				}
			}
		}
	}

	/**
	 * Used to prevent the mobs to be interested by (and aggressive against)
	 * spectators.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityTarget(final EntityTargetEvent ev) {
		
		// Check to make sure it isn't an NPC
		if (ev.getTarget() instanceof Player && !ev.getTarget().hasMetadata("NPC")
				&& pm.getPlayer(((Player) ev.getTarget())).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/**
	 * Used to prevent players & mobs from damaging spectators, and stop the
	 * fire display when a spectator comes out of a fire/lava block.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(final EntityDamageEvent ev) {
		
		// Check to make sure it isn't an NPC
		if (ev.getEntity() instanceof Player && !ev.getEntity().hasMetadata("NPC")
				&& pm.getPlayer((Player) ev.getEntity()).isSpectating()) {
			ev.setCancelled(true);
			ev.getEntity().setFireTicks(0);
		}
	}

	/**
	 * Used to prevent any interaction on entities
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent ev) {
		if (!ev.getPlayer().hasMetadata("NPC") && pm.getPlayer(ev.getPlayer()).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/**
	 * Used to prevent any interaction on blocks (item frames, buttons, levers,
	 * pressure plates...).
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(final PlayerInteractEvent ev) {
		if (pm.getPlayer(ev.getPlayer()).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/**
	 * Used to prevent spectators breaking hangings (ItemFrame, LeashHitch or
	 * Painting)
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHangingBreakByEntity(final HangingBreakByEntityEvent ev) {

		if (ev.getRemover() instanceof Player && pm.getPlayer((Player) ev.getRemover()).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/* ** Drop- & pickup-related ** */

	/**
	 * Used to prevent spectators from dropping items on ground.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(final PlayerDropItemEvent ev) {
		if (pm.getPlayer(ev.getPlayer()).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/**
	 * Used to prevent spectators from picking up items.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(final PlayerPickupItemEvent ev) {
		if (pm.getPlayer(ev.getPlayer()).isSpectating()) {
			ev.setCancelled(true);
		}
	}

	/* ** Doors-related ** */

	/**
	 * Cancel the use of the doors, trapdoors, etc.
	 *
	 * This event is not directly cancelled as the cancellation is part of the
	 * {@link #onPlayerInteract(PlayerInteractEvent)} event handler.
	 */
	@SuppressWarnings("incomplete-switch")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerUseDoor(final PlayerInteractEvent ev) {

		if (pm.getPlayer(ev.getPlayer()).isSpectating() && ev.hasBlock()) {
			final Material clickedType = ev.getClickedBlock().getType();

			// Allows spectators to pass through doors.
			if (clickedType == Material.WOODEN_DOOR || clickedType == Material.IRON_DOOR_BLOCK
					|| clickedType == Material.FENCE_GATE) {
				Player spectator = ev.getPlayer();
				Location doorLocation = ev.getClickedBlock().getLocation()
						.setDirection(spectator.getLocation().getDirection());

				int relativeHeight = 0;

				if (clickedType == Material.WOODEN_DOOR || clickedType == Material.IRON_DOOR_BLOCK) {
					Material belowBlockType = ev.getClickedBlock().getLocation().add(0, -1, 0).getBlock().getType();

					if (belowBlockType == Material.WOODEN_DOOR || belowBlockType == Material.IRON_DOOR_BLOCK) {
						// The spectator clicked the top part of the door.
						relativeHeight = -1;
					}
				}

					/*
					 * North: small Z South: big Z East: big X West: small X
					 */
				switch (ev.getBlockFace()) {
					case EAST:
						spectator.teleport(doorLocation.add(-0.5, relativeHeight, 0.5),
								PlayerTeleportEvent.TeleportCause.PLUGIN);
						break;

					case NORTH:
						spectator.teleport(doorLocation.add(0.5, relativeHeight, 1.5),
								PlayerTeleportEvent.TeleportCause.PLUGIN);
						break;

					case SOUTH:
						spectator.teleport(doorLocation.add(0.5, relativeHeight, -0.5),
								PlayerTeleportEvent.TeleportCause.PLUGIN);
						break;

					case WEST:
						spectator.teleport(doorLocation.add(1.5, relativeHeight, 0.5),
								PlayerTeleportEvent.TeleportCause.PLUGIN);
						break;

					case UP:
						/*
						 * If it's a fence gate, we uses the relative position of
						 * the player and the gate.
						 */
						if (ev.getClickedBlock().getState().getData() instanceof Gate) {
							Gate fenceGate = (Gate) ev.getClickedBlock().getState().getData();
							/*
							 * The BlockFace represents the block in the direction
							 * of the "line" of the gate. So we needs to invert the relative
							 * teleportation.
							 */
							switch (fenceGate.getFacing()) {
								case NORTH:
								case SOUTH:
									if (spectator.getLocation().getX() > doorLocation.getX()) {
										spectator.teleport(doorLocation.add(-0.5, relativeHeight, 0.5),
												PlayerTeleportEvent.TeleportCause.PLUGIN);
									} else {
										spectator.teleport(doorLocation.add(1.5, relativeHeight, 0.5),
												PlayerTeleportEvent.TeleportCause.PLUGIN);
									}
									break;

								case EAST:
								case WEST:
									if (spectator.getLocation().getZ() > doorLocation.getZ()) {
										spectator.teleport(doorLocation.add(0.5, relativeHeight, -0.5),
												PlayerTeleportEvent.TeleportCause.PLUGIN);
									} else {
										spectator.teleport(doorLocation.add(0.5, relativeHeight, 1.5),
												PlayerTeleportEvent.TeleportCause.PLUGIN);
									}
									break;
							}
						}

						break;
				}
			}

			// Allows spectators to pass through trap doors
			else if (clickedType == Material.TRAP_DOOR) {
				if (!((TrapDoor) ev.getClickedBlock().getState().getData()).isOpen()) {
					Player spectator = ev.getPlayer();
					Location doorLocation = ev.getClickedBlock().getLocation()
							.setDirection(spectator.getLocation().getDirection());

					switch (ev.getBlockFace()) {
						case UP:
							spectator.teleport(doorLocation.add(0.5, -1, 0.5), PlayerTeleportEvent.TeleportCause.PLUGIN);
							break;

						case DOWN:
							spectator.teleport(doorLocation.add(0.5, 1, 0.5), PlayerTeleportEvent.TeleportCause.PLUGIN);
							break;

						default:
							break;
					}
				}
			}
		}
	}

	/* ** Riding-related ** */

	/**
	 * Stops spectators riding horses, Minecarts, etc.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleEnter(final VehicleEnterEvent e) {
		if (e.getEntered() instanceof Player && pm.getPlayer((Player) e.getEntered()).isSpectating()) {
			e.setCancelled(true);
		}
	}

	/**
	 * Stops players trying to break entities such as Minecarts, Boats, etc.
	 */
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onVehicleDamage(final VehicleDamageEvent e) {
		if (e.getAttacker() instanceof Player && pm.getPlayer((Player) e.getAttacker()).isSpectating()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		
		GamePlayer gp = pm.getPlayer((Player) event.getWhoClicked());
		
		if (gp.isSpectating()) {
            event.setCancelled(true);
        }
	}
}