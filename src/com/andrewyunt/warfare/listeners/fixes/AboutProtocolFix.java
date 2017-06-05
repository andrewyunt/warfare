package com.andrewyunt.warfare.listeners.fixes;

import com.andrewyunt.warfare.Warfare;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class AboutProtocolFix extends PacketAdapter {

    public AboutProtocolFix() {
        super(Warfare.getInstance(), PacketType.Play.Client.TAB_COMPLETE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packetContainer = event.getPacket();
        String tab = packetContainer.getStrings().read(0);
        tab = tab.toLowerCase();

        if (tab.contains(":") || tab.startsWith("/minecraft:") || tab.startsWith("/bukkit:") || tab.startsWith("/about") || tab.startsWith("/ver") || tab.startsWith("/version")) {
            event.setCancelled(true);
        }
    }
}