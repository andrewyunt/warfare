package com.andrewyunt.warfare.objects;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;

public class ClassNameConversation implements ConversationAbandonedListener {
	
	private final ConversationFactory conversationFactory;
	private final GamePlayer player;
	
	public ClassNameConversation(GamePlayer player, CustomClass customClass, CustomClass replacingClass) {
		
		this.player = player;
		
		for (int i = 0; i < 100; i++)
			player.getBukkitPlayer().sendMessage(" ");
		
		conversationFactory = new ConversationFactory(Warfare.getInstance())
				.withModality(true)
				.withFirstPrompt(new NamePrompt(player, customClass, replacingClass))
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
		private CustomClass replacingClass;
		
		NamePrompt(GamePlayer player, CustomClass customClass, CustomClass replacingClass) {
			
			this.player = player;
			this.customClass = customClass;
			this.replacingClass = replacingClass;
		}
		
		@Override
		public String getPromptText(ConversationContext context) {
			
			return ChatColor.GOLD + ChatColor.BOLD.toString() + "Please enter a name for your class in the chat.";
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					player.getCustomClasses().remove(replacingClass);
					
					customClass.setName(input);
					player.getCustomClasses().add(customClass);
					
					player.getBukkitPlayer().sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString()
						+ String.format("You set the name of your class to %s", input));
					
					if (player.getCustomClass() == null)
						player.setCustomClass(player.getCustomClasses().get(0));
				}
			}, 20L);
			
			return Prompt.END_OF_CONVERSATION;
		}
	}
	
	@Override
	public void conversationAbandoned(ConversationAbandonedEvent event) {
		
		Player player = (Player) event.getContext().getForWhom();
		
		if (!event.gracefulExit())
			player.sendMessage("Converastion abandoned by " + event.getCanceller().getClass().getName());
	}
}