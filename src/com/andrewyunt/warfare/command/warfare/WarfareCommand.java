
package com.andrewyunt.warfare.command.warfare;

import com.andrewyunt.warfare.command.warfare.arguments.*;
import com.faithfulmc.util.command.ArgumentExecutor;

public class WarfareCommand extends ArgumentExecutor {

	public WarfareCommand() {

		super("warfare");

		addArgument(new AddCoinsArgument());
		addArgument(new SetMapLocationArgument());
		addArgument(new RemoveCoinsArgument());
		addArgument(new AddCageArgument());
		addArgument(new RemoveCageArgument());
		addArgument(new AddChestArgument());
		addArgument(new EditArgument());
		addArgument(new StartArgument());
		addArgument(new RestartArgument());
	}
}