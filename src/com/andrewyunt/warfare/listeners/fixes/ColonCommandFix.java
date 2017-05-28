package com.andrewyunt.warfare.listeners.fixes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ColonCommandFix implements Listener {

    @EventHandler
    public void onPlayerColonCommand(PlayerCommandPreprocessEvent e){
        String message = e.getMessage().toLowerCase();
        if (message.startsWith("/minecraft:") || message.startsWith("bukkit:") || message.startsWith("/me")) {
            e.setCancelled(true);
        } else if ((message.startsWith("/ver") || message.startsWith("/about") || message.contains(":")) && !e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }
}