package com.andrewyunt.warfare.player;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Party {

    @Setter private UUID leader;
    @Setter private boolean open;

    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> invites = new HashSet<>();

    public Party(UUID leader) {
        this.leader = leader;
        members.add(leader);
    }
}