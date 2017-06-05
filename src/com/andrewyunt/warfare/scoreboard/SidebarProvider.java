package com.andrewyunt.warfare.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface SidebarProvider {
    String getTitle();

    List<SidebarEntry> getLines(Player paramPlayer, long now);
}