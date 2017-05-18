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

import com.andrewyunt.warfare.objects.Powerup;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.utilities.Utils;

/**
 * The listener class used for Powerups which holds methods to listen on events.
 *
 * @author Andrew Yunt
 */
public class PlayerPowerupListener implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {

        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        Material type = item.getType();
        Action action = event.getAction();
        Player player = event.getPlayer();
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {

            if (!gp.isInGame()) {
                return;
            }

            if (gp.getSelectedPowerup() == Powerup.EXPLOSIVE_ARROW) {
                return;
            }

            if (!type.toString().toLowerCase().contains("sword")) {
                return;
            }

            gp.getSelectedPowerup().use(gp);

        } else if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {

            if (!gp.isInGame()) {
                return;
            }

            if (gp.getSelectedPowerup() == Powerup.EXPLOSIVE_ARROW) {
                if (type == Material.BOW) {
                    gp.getSelectedPowerup().use(gp);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void EPC(EntityDamageByEntityEvent event) {

        if (event.getCause() == DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        final GamePlayer finalDamagedGP = damagedGP;

        if (damagedGP.isEPCCooldown()) {
            event.setCancelled(true);
        } else {
            damagedGP.setEPCCooldown(true);

            new BukkitRunnable() {
                @Override
                public void run() {

                    finalDamagedGP.setEPCCooldown(false);
                }
            }.runTaskLater(Warfare.getInstance(), 10L);
        }

        // Give energy
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            GamePlayer gpDamager = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
            GamePlayer gpDamaged = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

            if (!(gpDamaged.isInGame() && gpDamager.isInGame())) {
                return;
            }

            if (gpDamager.getSelectedPowerup() != Powerup.EXPLOSIVE_ARROW) {
                gpDamager.addEnergy(gpDamager.getSelectedPowerup().getEnergyPerClick());
            } else {
                gpDamager.addEnergy(3); // Since SKELETON enum only contains the player's bow hit energy.
            }

            Utils.playBloodEffect(damaged, 10);

            return;
        }

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }

            Player damager = (Player) arrow.getShooter();
            GamePlayer gpDamager = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
            GamePlayer gpDamaged = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

            // Don't give energy if the shot person is red
            if (event.isCancelled()) {
                return;
            }

            if (gpDamaged == gpDamager) {
                return;
            }

            if (gpDamager.getSelectedPowerup() != Powerup.EXPLOSIVE_ARROW) {
                return;
            }

            gpDamager.addEnergy(gpDamager.getSelectedPowerup().getEnergyPerClick());

            Utils.playBloodEffect(damaged, 10);

            return;
        }
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void EPC(EntityDamageEvent event) {

        if (event.getCause() == DamageCause.FALL) {
            return;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        final GamePlayer finalDamagedGP = damagedGP;

        if (damagedGP.isEPCCooldown()) {
            event.setCancelled(true);
        } else {
            damagedGP.setEPCCooldown(true);

            new BukkitRunnable() {
                @Override
                public void run() {

                    finalDamagedGP.setEPCCooldown(false);
                }
            }.runTaskLater(Warfare.getInstance(), 10L);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (event.getCause() == DamageCause.ENTITY_EXPLOSION && (event.getDamager().getType() != EntityType.PRIMED_TNT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {

        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.WITHER_SKULL) {
            return;
        }

        if (entity.hasMetadata("MegaArena")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        Entity entity = event.getEntity();

        if (!entity.hasMetadata("MegaArena")) {
            return;
        }

        Player shooter = (Player) ((Projectile) entity).getShooter();
        GamePlayer shooterGP = Warfare.getInstance().getPlayerManager().getPlayer(shooter.getName());

        Location loc = entity.getLocation().clone();

        loc.getWorld().spigot().playEffect(
                loc.add(0.0D, 0.8D, 0.0D),
                Effect.EXPLOSION_HUGE);

        for (Entity nearby : entity.getNearbyEntities(5D, 3D, 5D)) {
            if (!(nearby instanceof Player)) {
                continue;
            }

            if (nearby == shooter) {
                continue;
            }

            Player nearbyPlayer = (Player) nearby;

            GamePlayer nearbyGP = Warfare.getInstance().getPlayerManager().getPlayer(nearbyPlayer.getName());

            if (!nearbyGP.isInGame()) {
                continue;
            }

            double dmg = 1.5 + (shooterGP.getLevel(shooterGP.getSelectedPowerup()) * .5);
            Damageable dmgPlayer = nearbyPlayer;
            dmgPlayer.damage(0.00001D); // So the player will get the red damage

            if (dmgPlayer.getHealth() < dmg) {
                dmgPlayer.setHealth(0D);
                return;
            } else {
                nearbyPlayer.setHealth(nearbyPlayer.getHealth() - dmg);
            }
        }
    }
}