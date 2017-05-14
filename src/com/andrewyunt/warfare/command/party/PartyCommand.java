package com.andrewyunt.warfare.command.party;

import com.andrewyunt.warfare.command.party.arguments.*;
import com.faithfulmc.util.command.ArgumentExecutor;

public class PartyCommand extends ArgumentExecutor {

    public PartyCommand() {

        super("party");

        addArgument(new PartyCreateArgument());
        addArgument(new PartyDisbandArgument());
        addArgument(new PartyInviteArgument());
        addArgument(new PartyDeinviteArgument());
        addArgument(new PartyJoinArgument());
        addArgument(new PartyKickArgument());
        addArgument(new PartyLeaveArgument());
        addArgument(new PartyOpenArgument());
    }
}