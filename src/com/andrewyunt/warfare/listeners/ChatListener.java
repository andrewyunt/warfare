package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import com.google.common.collect.HashMultimap;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    public static final List<String> blocked = Arrays.asList("ikari","cheatbreaker","cheat breaker","nigga", "kill yourself", "nigger", "prime", "pulsepvp", "para",
            "plugins", "plugin", "p4ra", "rip off", "shit staff", "shit server", "gay server", "kys", "leaked", "ddos", "velt", "hcteams", "minehq", "kill yourself",
            "viper", "oxpvp", "togglinq", "hydrahcf", "purgepots", "shit owner", "fuck this", "crap server", "crap staff", "bad staff", "gay staff", "etb", "exploitsquad",
            "arson", "lag", "botted", "fake players", "bot players", "my server", "join my server", "hcgames", "hcsquads", "server is shit", "server is crap", "server is so",
            "anticheat", "anti cheat", "bad server", "faggot", "anti-cheat", "hydra", "endyou", "whatspuberty", "kult", "this is shit", "staff", "mods", "arsonhcf", "spoofing",
            "playercount", "minehq", "customkkk", "restart", "#keyall", "#another","rollback","admins","stuck");
    private HashMultimap<UUID, Long> messages = HashMultimap.create();
    private final ConcurrentMap<Object, Object> messageHistory = new ConcurrentHashMap<>();
    private static final Pattern PATTERN = Pattern.compile("\\W");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e){
        e.setCancelled(true);
        Player player = e.getPlayer();
        BaseUser baseUser = BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId());
        if (baseUser.isInStaffChat() && player.hasPermission("base.command.staffchat")) {
            return;
        }
        String message = e.getMessage();
        String lastMessage = (String) this.messageHistory.get(player.getUniqueId());
        String cleanedMessage = PATTERN.matcher(message).replaceAll("");
        if ((lastMessage != null) && ((message.equals(lastMessage)) || (StringUtils.getLevenshteinDistance(cleanedMessage, lastMessage) <= 1))
                && (!player.hasPermission("hcf.doublepost.bypass"))) {
            player.sendMessage(ChatColor.RED + "Double posting is prohibited.");
            e.setCancelled(true);
            return;
        }
        String prefixpex = Warfare.getChat().getGroupPrefix(player.getWorld(), Warfare.getPermission().getPrimaryGroup(player));
        String prefix = ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', prefixpex) + player.getName() + " "
                + (player.hasPermission("practice.silverarrow") ? ChatColor.DARK_GRAY : ChatColor.GOLD) + "» " + ChatColor.GRAY;
        String lower = message.toLowerCase();
        boolean block = false;
        for (String word: blocked) {
            if (lower.contains(word)) {
                block = true;
                break;
            }
        }
        if (message.length() == 1 && !(message.equalsIgnoreCase("o") || message.equalsIgnoreCase("k")
                || message.equalsIgnoreCase("?") || message.equalsIgnoreCase("l"))) {
            e.getRecipients().clear();
            e.getRecipients().add(player);
        } else {
            long now = System.currentTimeMillis();
            messages.put(player.getUniqueId(), System.currentTimeMillis());
            int amount = 0;
            for (long timestamp : new HashSet<>(messages.get(player.getUniqueId()))) {
                if (now - 5000 > timestamp) {
                    messages.remove(player.getUniqueId(), timestamp);
                } else {
                    amount++;
                }
            }
            if (amount >= 5) {
                if (amount == 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("base.command.staffchat")) {
                            p.sendMessage(org.bukkit.ChatColor.DARK_RED + "[!] " + ChatColor.RED + player.getName() + ChatColor.GRAY + " was stopped from spamming");
                        }
                    }
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + player.getName() + " -s 5m Spam");
                e.getRecipients().clear();
                e.getRecipients().add(player);
            } else if (amount >= 3) {
                block = true;
            }
        }
        GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(player);
        String msg = gamePlayer.isSpectating() ? ChatColor.GRAY + "[Spectator Chat] " : "" + prefix + message;
        if (gamePlayer.isSpectating()) {
            Iterator<Player> iterator = e.getRecipients().iterator();
            while (iterator.hasNext()) {
                Player recipient = iterator.next();
                GamePlayer otherPlayer = Warfare.getInstance().getPlayerManager().getPlayer(recipient);
                if (!otherPlayer.isSpectating()) {
                    iterator.remove();
                }
            }
        }
        player.sendMessage(msg);
        if (block) {
            msg = msg.replace(ChatColor.GOLD + "»", ChatColor.RED + "»");
        }
        for (Player other: e.getRecipients()) {
            if (other != player) {
                if (block) {
                    if (other.hasPermission("staffchat.use")) {
                        other.sendMessage(msg);
                    }
                } else {
                    other.sendMessage(msg);
                }
            }
        }
    }
}