package com.andrewyunt.skywarfare.objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.andrewyunt.skywarfare.SkyWarfare;

public class ClassNameConversation implements ConversationAbandonedListener {
	
	private final ConversationFactory conversationFactory;
	private final GamePlayer player;
	
	public ClassNameConversation(GamePlayer player, CustomClass customClass) {
		
		this.player = player;
		
		conversationFactory = new ConversationFactory(SkyWarfare.getInstance())
				.withModality(true)
				.withFirstPrompt(new NamePrompt(player, customClass))
				.withEscapeSequence("quit")
				.withTimeout(30)
				.thatExcludesNonPlayersWithMessage("Unable to access from the console.")
				.addConversationAbandonedListener(this);
	}
	
	public void beginConversation() {
		
		conversationFactory.buildConversation((Conversable) player.getBukkitPlayer()).begin();
	}

	private class NamePrompt extends StringPrompt {
		
		private GamePlayer player;
		private CustomClass customClass;
		
		NamePrompt(GamePlayer player, CustomClass customClass) {
			
			this.player = player;
			this.customClass = customClass;
		}
		
		@Override
		public String getPromptText(ConversationContext context) {
			
			return ChatColor.GOLD + "Please enter a name for your class in the chat.";
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			
			customClass.setName(input);
			player.getCustomClasses().add(customClass);
			
			player.getBukkitPlayer().sendMessage(ChatColor.GOLD + String.format(
					"You set the name of your class to %s", input));
			
			return Prompt.END_OF_CONVERSATION;
		}
	}
	
	@Override
	public void conversationAbandoned(ConversationAbandonedEvent event) {
		
		Player player = (Player) event.getContext().getForWhom();
		
		if(!event.gracefulExit())
			player.sendMessage("Converastion abandoned by " + event.getCanceller().getClass().getName());
	}
}