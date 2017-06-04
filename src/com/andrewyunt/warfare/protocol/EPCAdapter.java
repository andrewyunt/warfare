package com.andrewyunt.warfare.protocol;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.purchases.Powerup;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;

public class EPCAdapter extends PacketAdapter{

    public EPCAdapter() {
        super(Warfare.getInstance(), PacketType.Play.Client.USE_ENTITY);
    }

    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packetContainer = event.getPacket();
        if (packetContainer.getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.ATTACK) {
            Player player = event.getPlayer();
            if (player.isOnline()) {
                GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(player);
                if (gamePlayer.isInGame()) {
                    Powerup powerup = gamePlayer.getSelectedPowerup();
                    if (powerup != null && powerup != Powerup.MARKSMAN) {
                        gamePlayer.addEnergy(powerup.getEnergyPerClick());
                    }
                }
            }
        }
    }
}