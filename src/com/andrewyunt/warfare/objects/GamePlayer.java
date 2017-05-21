
package com.andrewyunt.warfare.objects;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

public class GamePlayer {
	
	private UUID uuid;
	private String name;
	private int points, coins, earnedCoins, wins, kills, killStreak, energy;
	private boolean epcCooldown, loaded, spectating, sentActivate, hasFallen, hasBloodEffect, explosiveWeaknessCooldown;
	private GamePlayer lastDamager;
	private Kit selectedKit;
	private Powerup selectedPowerup;
	
	private final Map<Purchasable, Integer> purchases = new HashMap<>();
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;

		for (Powerup powerup : Powerup.values()) {
			if (!purchases.containsKey(powerup)) {
				purchases.put(powerup, -1);
			}
		}

		// Register health objective for game servers
		if (!StaticConfiguration.LOBBY) {
			Objective healthObjective = Warfare.getInstance().getScoreboardHandler().getPlayerBoard(uuid).getScoreboard()
					.registerNewObjective(ChatColor.RED + "❤", "health");
			healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
	}
	
	public UUID getUUID() {
		
		return uuid;
	}
	
	public Player getBukkitPlayer() {
		
		return Bukkit.getServer().getPlayer(uuid);
	}

	public int getPoints() {

		return points;
	}

	public void setPoints(int points) {

		this.points = points;
	}

	public int getLevel() {

		return (int) Math.floor(points / 150);
	}
	
	public void setCoins(int coins) {
		
		this.coins = coins;
	}
	
	public int getCoins() {
		
		return coins;
	}
	
	public void setEarnedCoins(int earnedCoins) {
		if (!Objects.equals(earnedCoins, this.earnedCoins)) {
            this.earnedCoins = earnedCoins;
            update();
        }
	}
	
	public int getEarnedCoins() {
		
		return earnedCoins;
	}
	
	public void setWins(int wins) {
	    if (!Objects.equals(wins, this.wins)) {
            this.wins = wins;
            update();
        }
	}
	
	public int getWins() {
		return wins;
	}

	
	public void addKill() {
		
		this.killStreak = killStreak + 1;
		
		setKills(kills + 1);
	}
	
	public void setKills(int kills) {
	    if (!Objects.equals(kills, this.kills)) {
            this.kills = kills;
            update();
        }
	}
	
	public int getKills() {
		
		return kills;
	}

	public int getKillStreak() {

		return killStreak;
	}

	public void addEnergy(int energy) {

		setEnergy(this.energy + energy);
	}

	public void removeEnergy(int energy) {

		setEnergy(this.energy - energy);
	}

	public void setEnergy(int energy) {

		this.energy = energy;

		if (this.energy > 100) {
			this.energy = 100;
		} else {
			sentActivate = false;
		}

		if (this.energy == 100) {
			if (!sentActivate) {
				sentActivate = true;
				if (selectedPowerup == Powerup.MARKSMAN) {
					getBukkitPlayer().sendMessage(ChatColor.GOLD + "Left click" + ChatColor.YELLOW + " using your bow to activate your ability!");
				} else {
					getBukkitPlayer().sendMessage(ChatColor.GOLD + "Right click" + ChatColor.YELLOW + " using your sword to activate your ability!");
				}
			}
		}

		getBukkitPlayer().setLevel(this.energy);
		getBukkitPlayer().setExp(this.energy / 100.0F);
	}

	public int getEnergy() {

		return this.energy;
	}
	
	public void setEPCCooldown(boolean cooldown) {
		
		this.epcCooldown = cooldown;
	}
	
	public boolean isEPCCooldown() {
		
		return epcCooldown;
	}
	
	public void setLoaded(boolean loaded) {
		
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		
		return loaded;
	}
	
	public void setHasFallen(boolean hasFallen) {
		
		this.hasFallen = hasFallen;
	}
	
	public boolean hasFallen() {
		
		return hasFallen;
	}

	public void setHasBloodEffect(boolean hasBloodEffect) {

		this.hasBloodEffect = hasBloodEffect;
	}

	public boolean getHasBloodEffect() {

		return hasBloodEffect;
	}

	public void setExplosiveWeaknessCooldown(boolean explosiveWeaknessCooldown) {

		this.explosiveWeaknessCooldown = explosiveWeaknessCooldown;
	}

	public boolean isExplosiveWeaknessCooldown() {

		return explosiveWeaknessCooldown;
	}
	
	public boolean isInGame() {
		
		return Warfare.getInstance().getGame().getPlayers().contains(this);
	}
	
	public void setLastDamager(GamePlayer lastDamager) {
		
		this.lastDamager = lastDamager;
	}
	
	public GamePlayer getLastDamager() {
		
		return lastDamager;
	}
	
	public void setSelectedKit(Kit selectedKit) {
	    if(!Objects.equals(selectedKit, this.selectedKit)) {
            this.selectedKit = selectedKit;
            update();
        }
	}
	
	public Kit getSelectedKit() {
		return selectedKit;
	}

	public Kit getSelectedKitOrPot(){
        return selectedKit == null ? Kit.POT : selectedKit;
    }

	public void setSelectedPowerup(Powerup selectedPowerup) {
		if(!Objects.equals(selectedPowerup, this.selectedPowerup)) {
			this.selectedPowerup = selectedPowerup;
			update();
		}
	}

	public Powerup getSelectedPowerup() {
		return selectedPowerup;
	}

	public Location setSpectating(boolean spectating, boolean respawn) {
		this.spectating = spectating;
		
		if (spectating) {
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
                Player player = getBukkitPlayer();

                player.setAllowFlight(true);
                player.setFireTicks(0);

                for (GamePlayer toShow : Warfare.getInstance().getGame().getSpectators()) {
                    player.showPlayer(toShow.getBukkitPlayer());
                }

                for (GamePlayer toHide : Warfare.getInstance().getGame().getPlayers()) {
                    toHide.getBukkitPlayer().hidePlayer(player);
                }

                updateHotbar();
                player.spigot().setCollidesWithEntities(false);
            }, 5L);

			
			Location loc = Warfare.getInstance().getArena().getMapLocation();
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded()) {
                chunk.load();
            }
			
			loc.setY(loc.getY() + 1);
			
			if (respawn) {
                return loc;
            } else {
                getBukkitPlayer().teleport(loc, TeleportCause.COMMAND);
            }
		}
		
		return null;
	}
	
	public boolean isSpectating() {
		
		return spectating;
	}
	
	public Map<Purchasable, Integer> getPurchases() {
		
		return purchases;
	}

	public int getLevel(Purchasable purchasable) {

		return purchases.get(purchasable);
	}
	
	public boolean isCaged() {
		
		return getCage() != null;
	}
	
	public Cage getCage() {
		
		if (StaticConfiguration.LOBBY) {
            return null;
        }
		
		for (Cage cage : Warfare.getInstance().getGame().getCages()) {
            if (cage.hasPlayer() && cage.getPlayer() == this) {
                return cage;
            }
        }
		
		return null;
	}

	public void updateHotbar() {

		PlayerInventory inv = getBukkitPlayer().getInventory();
		inv.clear();

        if (StaticConfiguration.LOBBY) {
			ItemStack shop = new ItemStack(Material.CHEST, 1);
			ItemMeta shopMeta = shop.getItemMeta();
			shopMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_SHOP_TITLE));
			shop.setItemMeta(shopMeta);
			inv.setItem(StaticConfiguration.LOBBY_SHOP_SLOT - 1, shop);

			ItemStack play = new ItemStack(Material.COMPASS, 1);
			ItemMeta playMeta = play.getItemMeta();
			playMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_PLAY_TITLE));
			play.setItemMeta(playMeta);
			inv.setItem(StaticConfiguration.LOBBY_PLAY_SLOT - 1, play);

			ItemStack classSelector = new ItemStack(Material.ENDER_CHEST, 1);
			ItemMeta classSelectorMeta = classSelector.getItemMeta();
			classSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_KIT_SELECTOR_TITLE));
			classSelector.setItemMeta(classSelectorMeta);
			inv.setItem(StaticConfiguration.LOBBY_KIT_SELECTOR_SLOT - 1, classSelector);
		} else if (spectating) {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
				ItemStack teleporter = new ItemStack(Material.COMPASS, 1);
				ItemMeta teleporterMeta = teleporter.getItemMeta();
				teleporterMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.SPECTATOR_TELEPORTER_TITLE));
				teleporter.setItemMeta(teleporterMeta);
				inv.setItem(StaticConfiguration.SPECTATOR_TELEPORTER_SLOT - 1, teleporter);

				ItemStack bed = new ItemStack(Material.BED, 1);
				ItemMeta bedMeta = bed.getItemMeta();
				bedMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_TITLE));
				bed.setItemMeta(bedMeta);
				inv.setItem(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_SLOT - 1, bed);
			}, 5);
		} else {
			ItemStack kitSelector = new ItemStack(Material.ENDER_CHEST, 1);
			ItemMeta kitSelectorMeta = kitSelector.getItemMeta();
			kitSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.CAGE_KIT_SELECTOR_TITLE));
			kitSelector.setItemMeta(kitSelectorMeta);
			inv.setItem(StaticConfiguration.CAGE_KIT_SELECTOR_SLOT - 1, kitSelector);

			ItemStack powerupSelector = new ItemStack(Material.ENDER_CHEST, 1);
			ItemMeta powerupSelectorMeta = powerupSelector.getItemMeta();
			powerupSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.CAGE_POWERUP_SELECTOR_TITLE));
			powerupSelector.setItemMeta(powerupSelectorMeta);
			inv.setItem(StaticConfiguration.CAGE_POWERUP_SELECTOR_SLOT - 1, powerupSelector);

			ItemStack bed = new ItemStack(Material.BED, 1);
			ItemMeta bedMeta = bed.getItemMeta();
			bedMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.CAGE_RETURN_TO_LOBBY_TITLE));
			bed.setItemMeta(bedMeta);
			inv.setItem(StaticConfiguration.CAGE_RETURN_TO_LOBBY_SLOT - 1, bed);
		}

		getBukkitPlayer().updateInventory();
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
	    if(!Objects.equals(name, this.name)) {
            this.name = name;
            update();
        }
    }

    public void update(){
	    if(isLoaded()) {
            Warfare.getInstance().getStorageManager().savePlayerAsync(this);
        }
    }
}