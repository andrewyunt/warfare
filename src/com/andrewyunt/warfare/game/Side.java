package com.andrewyunt.warfare.game;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Side {

    @Getter private final int sideNum;

    @Getter @Setter private String name;

    public Side (int sideNum, String name) {
        this.sideNum = sideNum;
        this.name = name;
    }

    /**
     * @return A set of players currently in-game on the side.
     */
    public Set<GamePlayer> getPlayers() {
        Set<GamePlayer> players = new HashSet<>(Warfare.getInstance().getPlayerManager().getPlayers());
        Set<GamePlayer> toRemove = players.stream().filter(player -> player.getSide() != this)
                .collect(Collectors.toSet());

        toRemove.forEach(players::remove);

        return players;
    }
}