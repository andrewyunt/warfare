package com.andrewyunt.warfare.managers;

import com.andrewyunt.warfare.player.Party;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PartyManager {

    @Getter private Set<Party> parties = new HashSet<>();

    public Party createParty(UUID leader) {
        Party party = new Party(leader);
        parties.add(party);
        return party;
    }

    public void deleteParty(Party party) {
        parties.remove(party);
    }

    public Party getParty(UUID uuid) {
        for (Party party : parties) {
            if (party.getMembers().contains(uuid)) {
                return party;
            }
        }
        return null;
    }
}