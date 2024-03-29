package com.andrewyunt.warfare.scoreboard.provider;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.scoreboard.SidebarEntry;
import com.andrewyunt.warfare.scoreboard.SidebarProvider;
import com.andrewyunt.warfare.utilities.DateTimeFormats;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimerSidebarProvider implements SidebarProvider {

    protected static final String STRAIGHT_LINE = BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 13);

    private static String handleBardFormat(long millis, boolean trailingZero, boolean showMillis) {
        return ((showMillis ? trailingZero ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS
                : DateTimeFormats.SECONDS).get()).format(millis * 0.001D);
    }

    public String getTitle() {
        return ChatColor.GOLD + ChatColor.BOLD.toString() + "Warfare";
    }

    public String getColour(boolean b) {
        return b ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
    }

    public List<SidebarEntry> getLines(Player player, long now) {
        List<SidebarEntry> lines = new ArrayList<>();

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        if (StaticConfiguration.LOBBY) {
            lines.add(new SidebarEntry(ChatColor.GOLD + ChatColor.BOLD.toString() + "Statistics" + ChatColor.GRAY + ChatColor.BOLD.toString() + ":"));

            // Display player's level
            lines.add(new SidebarEntry(ChatColor.GOLD + "  » ", ChatColor.YELLOW + "Level: ", ChatColor.GRAY + String.valueOf(gp.getLevel())));

            // Display coins needed for next level
            int pointsNeeded = gp.getPoints() % 150 != 0 ? gp.getPoints() % 150 : 150;
            lines.add(new SidebarEntry(ChatColor.GOLD + "  » ", ChatColor.YELLOW + "Next level: ", ChatColor.GRAY + String.valueOf(pointsNeeded) + " points"));

            // Display player's wins
            lines.add(new SidebarEntry(ChatColor.GOLD + "  » ",ChatColor.YELLOW + "Total Wins: ", ChatColor.GRAY + String.valueOf(gp.getWins())));

            // Display player's kills
            lines.add(new SidebarEntry(ChatColor.GOLD + "  » ", ChatColor.YELLOW + "Total Kills: ",ChatColor.GRAY + String.valueOf(gp.getKills())));

            // Display player's coins
            lines.add(new SidebarEntry(ChatColor.GOLD + "  » ",ChatColor.YELLOW + "Coin Balance: ",ChatColor.GRAY + String.valueOf(gp.getCoins())));

            if (gp.isLoaded()) {
                lines.add(new SidebarEntry(" "));
                // Display player's chosen class
                lines.add(new SidebarEntry(ChatColor.GOLD + ChatColor.BOLD.toString() + "Selected Kit" + ChatColor.GRAY + ChatColor.BOLD.toString() + ":"));
                lines.add(new SidebarEntry(ChatColor.GOLD + "  » ", ChatColor.YELLOW + ChatColor.BOLD.toString(),
                        (gp.getSelectedKit()) == null ? "None" : gp.getSelectedKit().getName()));
            }
        } else {
            Game game = Warfare.getInstance().getGame();
            Game.Stage stage = game.getStage();

            if (stage == Game.Stage.WAITING || stage == Game.Stage.COUNTDOWN) {
                // Display players
                lines.add(new SidebarEntry(ChatColor.YELLOW, "Players: " + ChatColor.GRAY, game.getPlayers().size() + "/"
                        + (game.isTeams() ? game.getTeamSize() * 2 : game.getCages().size())));

                lines.add(new SidebarEntry(ChatColor.RESET + "  "));

                // Display seconds left
                if (stage == Game.Stage.WAITING) {
                    lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Waiting...", ""));
                } else {
                    lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Starting in ", game.getCountdownTime() + "s"));
                }

                lines.add(new SidebarEntry(" "));

                // Display server name
                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Map: ", ChatColor.GRAY + StaticConfiguration.MAP_NAME));
            } else {
                short gameTime = game.getGameTime();
                String timeRemaining = LocalTime.ofSecondOfDay(900 - gameTime).toString().substring(3);
                if (timeRemaining.length() == 1) {
                    timeRemaining = timeRemaining + ":00";
                }
                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Time remaining: ", ChatColor.GRAY + timeRemaining));

                lines.add(new SidebarEntry("    "));

                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Next event",":"));

                short subtractFrom = (short) (gameTime <= 300 ? 300 : gameTime <= 600 ? 600 : 900);
                String refillTime = LocalTime.ofSecondOfDay(subtractFrom - gameTime).toString().substring(4);
                if (refillTime.length() == 1) {
                    refillTime = refillTime + ":00";
                }

                lines.add(new SidebarEntry("  " + ChatColor.YELLOW.toString(), "Refill", ChatColor.GRAY + " " + refillTime));

                lines.add(new SidebarEntry(ChatColor.RESET + "   "));

                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Players Left: ", ChatColor.GRAY.toString() +  game.getPlayers().size()));

                lines.add(new SidebarEntry(ChatColor.RESET + "  " + ChatColor.RESET));

                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Killstreak: ", ChatColor.GRAY.toString() + gp.getKillStreak()));

                lines.add(new SidebarEntry("     " + ChatColor.RESET));

                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Coins: " + ChatColor.GRAY, String.valueOf(gp.getCoins())));

                lines.add(new SidebarEntry(ChatColor.YELLOW.toString(), "Level: " + ChatColor.GRAY, String.valueOf(gp.getLevel())));

                int pointsNeeded = gp.getPoints() % 150 != 0 ? gp.getPoints() % 150 : 150;
                lines.add(new SidebarEntry(ChatColor.YELLOW + "Next level: ", ChatColor.GRAY + String.valueOf(pointsNeeded), " points"));
            }
        }

        lines.add(0, new SidebarEntry(ChatColor.GRAY, ChatColor.STRIKETHROUGH + TimerSidebarProvider.STRAIGHT_LINE, TimerSidebarProvider.STRAIGHT_LINE));
        lines.add(lines.size(), new SidebarEntry(ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString(), TimerSidebarProvider.STRAIGHT_LINE, TimerSidebarProvider.STRAIGHT_LINE));

        return lines;
    }
}