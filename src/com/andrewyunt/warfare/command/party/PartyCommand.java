package com.andrewyunt.warfare.command.party;

import com.andrewyunt.warfare.command.party.arguments.PartyJoinArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyKickArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyLeaveArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyOpenArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyDeinviteArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyInviteArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyDisbandArgument;
import com.andrewyunt.warfare.command.party.arguments.PartyCreateArgument;
import com.andrewyunt.warfare.utilities.command.ArgumentExecutor;

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