package com.andrewyunt.warfare.scoreboard;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.scoreboard.provider.TimerSidebarProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ScoreboardHandler implements Listener, Runnable {

    private final Map<UUID, PlayerBoard> playerBoards;
    private final TimerSidebarProvider timerSidebarProvider;

    public ScoreboardHandler() {

        this.playerBoards = new HashMap<>();
        this.timerSidebarProvider = new TimerSidebarProvider();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerBoard playerBoard = new PlayerBoard(player);
            setPlayerBoard(player.getUniqueId(), playerBoard);
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(Warfare.getInstance(), this, 0, 2);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        setPlayerBoard(player.getUniqueId(), new PlayerBoard(player));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {

        synchronized (this.playerBoards) {
            (this.playerBoards.remove(event.getPlayer().getUniqueId())).remove();
        }
    }

    public PlayerBoard getPlayerBoard(UUID uuid) {

        synchronized (this.playerBoards) {
            return this.playerBoards.get(uuid);
        }
    }

    public void setPlayerBoard(UUID uuid, PlayerBoard board) {

        synchronized (this.playerBoards) {
            this.playerBoards.put(uuid, board);
        }
        board.setSidebarVisible(true);
        board.setDefaultSidebar(this.timerSidebarProvider);
    }

    public void run() {

        long now = System.currentTimeMillis();
        synchronized (this.playerBoards) {
            for (PlayerBoard board : this.playerBoards.values()) {
                if (board.getPlayer().isOnline() && !board.getPlayer().isDead() && !board.isRemoved()) {
                    try {
                        board.updateObjective(now);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    public void clearBoards() {

        synchronized (this.playerBoards) {
            Iterator<PlayerBoard> iterator = this.playerBoards.values().iterator();
            while (iterator.hasNext()) {
                iterator.next().remove();
                iterator.remove();
            }
        }
    }

    public Map<UUID, PlayerBoard> getPlayerBoards() {

        return playerBoards;
    }
}