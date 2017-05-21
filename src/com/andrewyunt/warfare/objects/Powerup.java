
package com.andrewyunt.warfare.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.utilities.Utils;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

/**
 * The enumeration for Powerups, their names, and the method to use them.
 *
 * @author Andrew Yunt
 */
public enum Powerup implements Purchasable {

    MEDIC("Medic", 4),
    MARKSMAN("Marksman", 20),
    WIZARD("Wizard", 10),
    BOMBER("Bomber", 10),
    SPECTRE("Spectre", 5),
    NINJA("Ninja", 5);

    private final String name;
    private final int energyPerClick;

    private int particleTaskID;
    private int damageTaskID;

    Powerup(String name, int energyPerClick) {

        this.name = name;
        this.energyPerClick = energyPerClick;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public int getPrice(int level) {

        switch(this) {
            case MEDIC:
                return level * 5000;
            case MARKSMAN:
                return level * 7500;
            case WIZARD:
                return level * 10000;
            case BOMBER:
                return level * 12500;
            case SPECTRE:
                return level * 15000;
            case NINJA:
                if (level == 1) {
                    return 17500;
                } else if (level == 2) {
                    return 55000;
                } else if (level == 3) {
                    return 72500;
                }
        }

        return 0;
    }

    public int getPlayerLvlNeeded(int level) {

        return (new ArrayList<Powerup>(Arrays.asList(values())).indexOf(this) * 4 + level + 1) * 5;
    }

    @Override
    public ItemStack getDisplayItem() {

        switch(this) {
            case MEDIC:
                Potion healPotion = new Potion(PotionType.INSTANT_HEAL, 2);
                healPotion.setSplash(true);
                return healPotion.toItemStack(1);
            case MARKSMAN:
                return new ItemStack(Material.ARROW);
            case WIZARD:
                return new ItemStack(Material.BLAZE_ROD);
            case BOMBER:
                return new ItemStack(Material.TNT);
            case SPECTRE:
                return new ItemStack(Material.NETHER_STAR);
            case NINJA:
                return new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
        }

        return null;
    }

    public int getEnergyPerClick() {

        return energyPerClick;
    }

    /**
     * Uses the specified ability for the given player.
     *
     * @param player
     * 		The player to use the specified ability for.
     */
    public void use(GamePlayer player) {

        if (player.getEnergy() < 100) {
            return;
        }

        Player bp = player.getBukkitPlayer();
        int level = player.getLevel(this);

        if (this == MEDIC) {

            double hearts = 2.0 + 0.5 * (level - 1);

            if (bp.getHealth() + hearts < bp.getMaxHealth()) {
                bp.setHealth(bp.getHealth() + hearts);
            } else {
                bp.setHealth(bp.getMaxHealth());
            }

            Vector vector = new Vector();

            for (int i = 0; i < 50; i++) {
                float alpha = ((3.1415927F / 2F) / 50) * i;
                double phi = Math.pow(Math.abs(Math.sin(2 * 2F * alpha)) + 0.8
                        * Math.abs(Math.sin(2F * alpha)), 1 / 2D);

                vector.setY(phi * (Math.sin(alpha) + Math.cos(alpha)) * 1);
                vector.setZ(phi * (Math.cos(alpha) - Math.sin(alpha)) * 1);

                Location newLoc = bp.getEyeLocation().clone();
                Utils.rotateYAxis(vector, 50);
                newLoc.add(vector);
                newLoc.getWorld().playEffect(newLoc, Effect.HEART, 1);
            }

            bp.sendMessage(ChatColor.YELLOW + String.format(
                    "You have used the %s ability and have restored %s hearts.",
                    ChatColor.GOLD + "Heal" + ChatColor.YELLOW,
                    ChatColor.GOLD + String.valueOf(hearts / 2) + ChatColor.YELLOW));

        } else if (this == MARKSMAN) {

            Projectile arrow = bp.launchProjectile(Arrow.class);
            arrow.setVelocity(arrow.getVelocity().multiply(2.0));
            arrow.setMetadata("Warfare", new FixedMetadataValue(Warfare.getInstance(), true));
            arrow.setShooter(bp);

        } else if (this == WIZARD) {

            int count = 0;

            for (Entity entity : bp.getNearbyEntities(3, 3, 3)){
                if (!(entity instanceof Player)) {
                    continue;
                }

                Player entityPlayer = (Player) entity;
                GamePlayer entityAP = Warfare.getInstance().getPlayerManager().getPlayer(entityPlayer.getName());

                if (!entityAP.isInGame()) {
                    continue;
                }

                double dmg = 1.0 + 0.5 * (level - 1);

                entityPlayer.getWorld().strikeLightningEffect(entityPlayer.getLocation());
                Damageable dmgVictim = entityPlayer;
                dmgVictim.damage(0.00001D); // So the player will get the red damage

                if (dmgVictim.getHealth() <= dmg) {
                    dmgVictim.setHealth(0D);
                } else {
                    dmgVictim.setHealth(dmgVictim.getHealth() - dmg);
                }

                count++;
            }

            if (count == 0) {
                bp.sendMessage(ChatColor.YELLOW + "No targets within range found!");
                return;
            }

            bp.sendMessage(ChatColor.YELLOW + String.format(
                    "You have used the %s ability.",
                    ChatColor.GOLD + this.getName() + ChatColor.YELLOW));

        } else if (this == BOMBER) {

            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
                Location loc = bp.getLocation().clone();

                loc.getWorld().spigot().playEffect(
                        loc.add(0.0D, 0.8D, 0.0D),
                        Effect.EXPLOSION_HUGE);

                for (Entity entity : bp.getNearbyEntities(5, 3, 5)) {
                    if (!(entity instanceof Player)) {
                        continue;
                    }

                    Player entityPlayer = (Player) entity;
                    GamePlayer entityAP = Warfare.getInstance().getPlayerManager().getPlayer(entityPlayer.getName());

                    if (!entityAP.isInGame()) {
                        continue;
                    }

                    Damageable dmgVictim = (Damageable) entity;
                    double dmg = 3.0 + 0.5 * (level - 1);

                    ((Damageable) entity).damage(0.00001D); // So the player will get the red damage

                    if (dmgVictim.getHealth() <= dmg) {
                        dmgVictim.setHealth(0D);
                    } else {
                        dmgVictim.setHealth(dmgVictim.getHealth() - dmg);
                    }
                }
            }, 60L);

        } else if (this == SPECTRE) {

            Location location = bp.getLocation();

            float duration = (float) (1.5 + (0.5 * level));
            double radius = 2;
            double maxHeight = 5;

            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            particleTaskID = scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), new Runnable() {
                float elapsedTime = 0;

                @Override
                public void run() {

                    if (elapsedTime >= duration) {
                        scheduler.cancelTask(particleTaskID);
                    }

                    for (double y = 0; y < maxHeight; y+= 0.05) {
                        double x = Math.sin(y * radius);
                        double z = Math.cos(y * radius);

                        Location newLoc = new Location(location.getWorld(), location.getX() + x,
                                location.getY() + y, location.getZ() + z);

                        for (Entity entity : Utils.getNearbyEntities(bp.getLocation(), 50)) {
                            if (!(entity instanceof Player)) {
                                continue;
                            }

                            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                                    "snowshovel",
                                    (float) newLoc.getX(),
                                    (float) newLoc.getY(),
                                    (float) newLoc.getZ(),
                                    0, 0, 0, 1, 0);

                            ((CraftPlayer) entity).getHandle().playerConnection.sendPacket(packet);
                        }
                    }

                    for (int i = 0; i < 5; i++) {
                        float xRand = new Random().nextInt(2) - 1;
                        float zRand = new Random().nextInt(2) - 1;

                        Location newLoc = new Location(location.getWorld(), location.getX() + xRand,
                                location.getY(), location.getZ() + zRand);

                        for (Entity entity : Utils.getNearbyEntities(bp.getLocation(), 50)) {
                            if (!(entity instanceof Player)) {
                                continue;
                            }

                            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                                    "largesmoke",
                                    (float) newLoc.getX(),
                                    (float) newLoc.getY(),
                                    (float) newLoc.getZ(),
                                    0, 0, 0, 1, 0);

                            ((CraftPlayer) entity).getHandle().playerConnection.sendPacket(packet);
                        }
                    }

                    elapsedTime = elapsedTime + 0.5F;
                }
            }, 0L, (long) 10L);

            damageTaskID = scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), new Runnable() {
                int elapsedTime = 0;

                @Override
                public void run() {

                    if (elapsedTime >= duration) {
                        scheduler.cancelTask(damageTaskID);
                    }

                    for (Entity entity : Utils.getNearbyEntities(location, 5)) {
                        if (!(entity instanceof Player)) {
                            continue;
                        }

                        Player entityPlayer = (Player) entity;

                        if (entityPlayer.getLocation().getY() - location.getY() > 3) {
                            continue;
                        }

                        GamePlayer entityGP = Warfare.getInstance().getPlayerManager().getPlayer(entityPlayer.getName());

                        if (!entityGP.isInGame()) {
                            continue;
                        }

                        if (entity == bp) {
                            continue;
                        }

                        // So the player will get the red damage
                        ((Damageable) entity).damage(0.00001D);

                        double health = ((Damageable) entity).getHealth() - 2.0D;

                        if (health > 0) {
                            ((Damageable) entity).setHealth(health);
                        } else {
                            ((Damageable) entity).setHealth(0D);
                        }
                    }

                    elapsedTime++;
                }
            }, 0L, 20L);

        } else if (this == NINJA) {

            Vector originalVector = bp.getEyeLocation().getDirection();
            Vector rightVector = originalVector.clone();
            Vector leftVector =originalVector.clone();

            Utils.rotateYAxis(rightVector, 25);
            Utils.rotateYAxis(leftVector, -25);

            WitherSkull originalSkull = bp.launchProjectile(WitherSkull.class, originalVector);
            originalSkull.setMetadata("Warfare", new FixedMetadataValue(Warfare.getInstance(), true));

            WitherSkull rightSkull = bp.launchProjectile(WitherSkull.class, rightVector);
            rightSkull.setMetadata("Warfare", new FixedMetadataValue(Warfare.getInstance(), true));

            WitherSkull leftSkull = bp.launchProjectile(WitherSkull.class, leftVector);
            leftSkull.setMetadata("Warfare", new FixedMetadataValue(Warfare.getInstance(), true));
        }

        player.setEnergy(0);
    }
}