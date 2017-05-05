package com.andrewyunt.warfare.objects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {

    private UUID leader;
    private boolean open;

    private final Set<UUID> members = new HashSet<UUID>();
    private final Set<UUID> invites = new HashSet<UUID>();

    public Party(UUID leader) {

        this.leader = leader;
        members.add(leader);
    }

    public UUID getLeader() {

        return leader;
    }

    public void setOpen(boolean open) {

        this.open = open;
    }

    public boolean isOpen() {

        return open;
    }

    public Set<UUID> getMembers() {

        return members;
    }

    public Set<UUID> getInvites() {

        return invites;
    }
}