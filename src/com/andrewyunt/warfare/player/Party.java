package com.andrewyunt.warfare.player;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {

    @Getter  private UUID leader;
    @Getter @Setter private boolean open;

    @Getter private final Set<UUID> members = new HashSet<>();
    @Getter private final Set<UUID> invites = new HashSet<>();

    public Party(UUID leader) {

        this.leader = leader;
        members.add(leader);
    }
}