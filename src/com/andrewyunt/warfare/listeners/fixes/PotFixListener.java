package com.andrewyunt.warfare.listeners.fixes;

import com.andrewyunt.warfare.Warfare;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import net.minecraft.server.v1_8_R3.EntityProjectile;
import net.minecraft.server.v1_8_R3.MovingObjectPosition;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftThrownPotion;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PotFixListener implements Listener {

    public static final MethodAccessor methodAccessor = Accessors.getMethodAccessor(EntityProjectile.class, "a", MovingObjectPosition.class);

    @EventHandler(ignoreCancelled = true)
    private void onPlayerThrowPot(ProjectileLaunchEvent event) {
        if(Bukkit.getPluginManager().isPluginEnabled("FastPot")) {
            return;
        }

        if ((!(event.getEntity() instanceof ThrownPotion)) || (!(event.getEntity().getShooter() instanceof Player))) {
            return;
        }

        Player player = (Player)event.getEntity().getShooter();
        ThrownPotion potion = (ThrownPotion)event.getEntity();

        if ((!player.isDead()) && player.isSprinting() && (((CraftThrownPotion)potion).getHandle().motY < 0.3)) {
            for (PotionEffect potionEffect: potion.getEffects()) {
                if (potionEffect.getType().equals(PotionEffectType.HEAL)){
                    new BukkitRunnable(){
                        public void run() {
                            if(potion.isValid() && !player.isDead()) {
                                methodAccessor.invoke(((CraftThrownPotion)potion).getHandle(),new MovingObjectPosition(((CraftThrownPotion)potion).getHandle().shooter));
                            }
                        }
                    }.runTaskLater(Warfare.getInstance(), player.isOnGround() ? 4 : 8);
                    break;
                }
            }
        }
    }
}