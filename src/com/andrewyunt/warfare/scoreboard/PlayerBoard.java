package com.andrewyunt.warfare.scoreboard;

import com.andrewyunt.warfare.Warfare;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerBoard {

    public final BufferedObjective bufferedObjective;
    private final Scoreboard scoreboard;
    private final Player player;
    private boolean sidebarVisible;
    private boolean removed;
    private SidebarProvider defaultProvider;

    public PlayerBoard(final Player player) {

        this.sidebarVisible = false;
        this.removed = false;
        this.player = player;
        this.scoreboard = Warfare.getInstance().getServer().getScoreboardManager().getNewScoreboard();
        this.bufferedObjective = new BufferedObjective(this.scoreboard);
        player.setScoreboard(this.scoreboard);
    }

    public void remove() {

        this.removed = true;
        if (this.scoreboard != null) {
            synchronized (this.scoreboard) {
                for (final Team team : this.scoreboard.getTeams()) {
                    team.unregister();
                }
                for (final Objective objective : this.scoreboard.getObjectives()) {
                    objective.unregister();
                }
            }
        }
    }

    public Player getPlayer() {

        return this.player;
    }

    public Scoreboard getScoreboard() {

        return this.scoreboard;
    }

    public boolean isSidebarVisible() {

        return this.sidebarVisible;
    }

    public void setSidebarVisible(final boolean visible) {

        this.sidebarVisible = visible;
        this.bufferedObjective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    public void setDefaultSidebar(final SidebarProvider provider) {

        if (provider != null && provider.equals(this.defaultProvider)) {
            return;
        }
        this.defaultProvider = provider;
        if (provider == null) {
            synchronized (this.scoreboard) {
                this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            }
        }
    }

    protected void updateObjective(long now) {

        synchronized (this.scoreboard) {
            final SidebarProvider provider = this.defaultProvider;
            if (provider == null) {
                this.bufferedObjective.setVisible(false);
            } else {
                this.bufferedObjective.setTitle(provider.getTitle());
                this.bufferedObjective.setAllLines(provider.getLines(this.player, now));
                this.bufferedObjective.flip();
            }
        }
    }

    public boolean isRemoved() {

        return removed;
    }

    public SidebarProvider getDefaultProvider() {

        return defaultProvider;
    }
}