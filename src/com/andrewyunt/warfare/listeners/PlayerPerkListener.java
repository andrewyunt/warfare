package com.andrewyunt.warfare.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.Perk;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * The listener class used for Perks which holds methods to listen on events.
 *
 * @author Andrew Yunt
 * @author MaccariTA
 */
public class PlayerPerkListener implements Listener {

    private HashMap<UUID, Player> creeperTNT = new HashMap<>();
    private HashMap<UUID, GamePlayer> explosiveWeaknessTNT = new HashMap<>();

    @EventHandler
    public void removeEffects(PlayerDeathEvent e) {

        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> e.getEntity().getActivePotionEffects().clear(), 20L);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void boomerangSkill(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking for a bow hit from a player to a player
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        final Arrow arrow = (Arrow) event.getDamager();

        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player shooter = (Player) arrow.getShooter();
        Player damaged = (Player) event.getEntity();

        if (shooter == damaged) {
            return;
        }

        GamePlayer shooterGP = Warfare.getInstance().getPlayerManager().getPlayer(shooter.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());


        // Check if players are in-game
        if (!shooterGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        // Checking that the damaged player has the perk
        if (shooterGP.getSelectedPerk() != Perk.BOOMERANG) {
            return;
        }

        if (Math.random() > 0.2) {
            return;
        }

        shooter.getInventory().addItem(new ItemStack(Material.ARROW));

        shooter.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.BOOMERANG.getName() + ChatColor.YELLOW));
    }

    @EventHandler
    public void weakeningArrow(EntityDamageByEntityEvent event) {

        // Checking for a bow hit from a player to a player
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getDamager();

        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player shooter = (Player) arrow.getShooter();
        Player damaged = (Player) event.getEntity();

        if (shooter == damaged) {
            return;
        }

        GamePlayer shooterGP = Warfare.getInstance().getPlayerManager().getPlayer(shooter.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!shooterGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (shooterGP.getSelectedPerk() != Perk.WEAKENING_ARROW) {
            return;
        }

        // Apply effects
        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, true);
        PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true);

        shooter.addPotionEffect(regen, true);
        damaged.addPotionEffect(weakness, true);

        shooter.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.WEAKENING_ARROW.getName() + ChatColor.YELLOW));
        damaged.sendMessage(String.format(
                "%s's arrow inflicted you with Weakness II for 2 seconds.",
                ChatColor.GOLD + shooter.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void resist(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Check if damager and damaged entities are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        // Checking that the damaged player has the perk
        if (damagedGP.getSelectedPerk() != Perk.RESIST) {
            return;
        }

        if (Math.random() > 0.11) {
            return;
        }

        PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 0, true);

        damaged.addPotionEffect(resistance, true);

        damaged.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.RESIST.getName() + ChatColor.YELLOW));
    }

    @EventHandler
    public void swiftness(EntityDamageByEntityEvent event) {

        // Checking for a bow hit from a player to a player
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        final Arrow arrow = (Arrow) event.getDamager();

        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player shooter = (Player) arrow.getShooter();
        Player damaged = (Player) event.getEntity();

        GamePlayer shooterGP = Warfare.getInstance().getPlayerManager().getPlayer(shooter.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!shooterGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagedGP.getSelectedPerk() != Perk.SWIFTNESS) {
            return;
        }

        if (Math.random() > 0.1) {
            return;
        }

        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 60, 1, true);

        Collection<PotionEffect> effects = damaged.getActivePotionEffects();
        for (PotionEffect e : effects) {
            if (e.getType() == PotionEffectType.SPEED) {
                if (e.getAmplifier() >= 2) {
                    return;
                } else if (e.getDuration() >= 60) {
                    return;
                }
            }
        }

        damaged.addPotionEffect(speed, true);

        damaged.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.SWIFTNESS.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void recharge(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damaged is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damaged = (Player) event.getEntity();
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());
        GamePlayer damagerGP = damagedGP.getLastDamager();

        if (damagerGP == null) {
            return;
        }

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagerGP.getSelectedPerk() != Perk.RECHARGE) {
            return;
        }

        // Checking if killed
        boolean dead = false;

        if (event.getDamage() < 0.0001D && damaged.getHealth() - 1.0 < 0) {
            dead = true;
        }

        if (damaged.getHealth() - event.getFinalDamage() > 0 && !dead) {
            return;
        }

        PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true);
        PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, true);

        Player damager = damagerGP.getBukkitPlayer();

        damager.addPotionEffect(regen, true);
        damager.addPotionEffect(resistance, true);

        damager.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.RECHARGE.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void flurry(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damager and damaged are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getDamage() < 0.001D) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagerGP.getSelectedPerk() != Perk.FLURRY) {
            return;
        }

        if (Math.random() > .01) {
            return;
        }

        Collection<PotionEffect> effects = damager.getActivePotionEffects();
        for (PotionEffect e : effects) {
            if (e.getType() == PotionEffectType.SPEED) {
                if (e.getAmplifier() >= 1) {
                    return;
                } else if (e.getDuration() >= 40) {
                    return;
                }
            }
        }

        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 40, 0, true);
        damager.addPotionEffect(speed, true);

        damager.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.FLURRY.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void explosiveWeakness(EntityDamageEvent event) {

        if (event.isCancelled()) {
            return;
        }

        explosiveWeakness(event.getEntity());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void explosiveWeakness(EntityRegainHealthEvent event) {

        if (event.isCancelled()) {
            return;
        }

        explosiveWeakness(event.getEntity());
    }

    public void explosiveWeakness(Entity entity) {

        // Check if the entity is player
        if (!(entity instanceof Player)) {
            return;
        }

        // Casting to players
        Player player = (Player) entity;
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

        // Check if players are in-game
        if (!gp.isInGame()) {
            return;
        }

        if (gp.getSelectedPerk() != Perk.EXPLOSIVE_WEAKNESS) {
            return;
        }

        if (gp.isExplosiveWeaknessCooldown()) {
            return;
        }

        if (((Damageable) player).getHealth() <= 7) {
            return;
        }

        Location loc = entity.getLocation().clone();

        loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4, false, false);

        gp.setExplosiveWeaknessCooldown(true);

        final GamePlayer finalGP = gp;

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> finalGP.setExplosiveWeaknessCooldown(false), 600L);

        player.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.EXPLOSIVE_WEAKNESS.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void support(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damager and damaged are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagedGP.getSelectedPerk() != Perk.SUPPORT) {
            return;
        }

        if (Math.random() > 0.06) {
            return;
        }

        TNTPrimed tnt = (TNTPrimed) damaged.getWorld().spawnEntity(damaged.getEyeLocation(), EntityType.PRIMED_TNT);

        tnt.setFuseTicks(60); // 3 second delay before explosion
        creeperTNT.put(tnt.getUniqueId(), damaged);

        damaged.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.SUPPORT.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void disableTNT(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        if (!(event.getDamager() instanceof TNTPrimed)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        TNTPrimed tnt = (TNTPrimed) event.getDamager();
        event.setCancelled(true);

        if (!creeperTNT.containsKey(tnt.getUniqueId())) {
            return;
        }

        Player creeper = creeperTNT.get(tnt.getUniqueId());
        Player damaged = (Player) event.getEntity();

        if (creeper.getName().equals(damaged.getName())) {
            return;
        }

        GamePlayer creeperAP = Warfare.getInstance().getPlayerManager().getPlayer(creeper.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!creeperAP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damaged.getHealth() <= 2.0) {
            damaged.setHealth(0.0D);
        } else {
            damaged.setHealth(damaged.getHealth() - 2.0D);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void weakeningSwing(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damager and damaged are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagerGP.getSelectedPerk() != Perk.WEAKENING_SWING) {
            return;
        }

        if (Math.random() > 0.15D) {
            return;
        }

        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, true);

        damaged.addPotionEffect(weakness, false);

        damager.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.WEAKENING_SWING.getName() + ChatColor.YELLOW));
        damaged.sendMessage(String.format(
                "%s's hit inflicted you with Weakness for 2 seconds.",
                ChatColor.GOLD + damager.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void swiftBackup(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damager and damaged are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagedGP.getSelectedPerk() != Perk.SWIFT_BACKUP) {
            return;
        }

        if (Math.random() > 0.1D) {
            return;
        }

        damager.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.SWIFT_BACKUP.getName() + ChatColor.YELLOW));

        Wolf wolf = (Wolf) damaged.getWorld().spawnEntity(damaged.getLocation(), EntityType.WOLF);
        wolf.setOwner((AnimalTamer) damaged);
        wolf.setMaxHealth(1.0D);
        wolf.setHealth(1.0D);
        ((LivingEntity) wolf).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
        ((LivingEntity) wolf).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1), true);

        new BukkitRunnable() {
            @Override
            public void run() {

                wolf.remove();
            }
        }.runTaskLater(Warfare.getInstance(), 80L);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void soulSucker(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damager and damaged are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        if (damagerGP.getSelectedPerk() != Perk.SOUL_SUCKER) {
            return;
        }

        if (Math.random() > 0.12) {
            return;
        }

        if (damager.getHealth() > damager.getMaxHealth() - 1.0) {
            damager.setHealth(40.0);
        } else {
            damager.setHealth(damager.getHealth() + 1.0);
        }

        damager.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.SOUL_SUCKER.getName() + ChatColor.YELLOW));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void undead(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        // Checking if damager and damaged are players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Casting to players
        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();
        GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
        GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());

        // Check if players are in-game
        if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
            return;
        }

        // Checking that the damaged player has the perk
        if (damagedGP.getSelectedPerk() != Perk.UNDEAD) {
            return;
        }

        if (Math.random() > 0.07) {
            return;
        }

        if (damaged.getHealth() > ((Damageable) damaged).getMaxHealth() - 1.0) {
            damaged.setHealth(40.0);
        } else {
            damaged.setHealth(((Damageable) damaged).getHealth() + 1.0);
        }

        damaged.sendMessage(ChatColor.YELLOW + String.format(
                "Your %s perk has been activated!",
                ChatColor.GOLD + Perk.UNDEAD.getName() + ChatColor.YELLOW));
    }
}