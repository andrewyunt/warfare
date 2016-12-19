package com.andrewyunt.skywarfare.objects;
 
import com.google.common.base.Splitter;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
 
import java.util.*;
 
/**
 * Class used for player-specific scoreboards and scoreboards with entries over 16 chars.
 * 
 * @author Antalax
 * @author bobacadodl
 * @author Andrew Yunt
 */
public class DynamicScoreboard {
	
	private Scoreboard scoreboard;
	private Objective objective;
	private Map<Integer, NameData> scoreMap = new HashMap<>();
	private Map<String, Integer> nameMap = new HashMap<>();
 
	public DynamicScoreboard(String title) {
		
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		objective = scoreboard.registerNewObjective((title.length() > 16 ? title.substring(0, 15) : title), "dummy");
		objective.setDisplayName(title);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public void add(int score, String text) {
		
		if(!scoreMap.containsKey(score)) {
			String fix = fixDuplicates(text);
			nameMap.put(fix, score);
			
			NameData nameData = new NameData(fix);
			scoreMap.put(score, nameData);
			
			Team team = scoreboard.registerNewTeam("teams-" + nameData.idx);
			
			if(nameData.prefix != null)
				team.setPrefix(nameData.prefix);
			
			if(nameData.suffix != null)
				team.setSuffix(nameData.suffix);
			
			team.addEntry(nameData.name);
			
			objective.getScore(nameData.name).setScore(score);
		}
	}
 
	public void blankLine(int score) {
		
		add(score, ChatColor.RESET.toString());
	}
 
	public void remove(int score) {
		
		if(scoreMap.containsKey(score)) {
			NameData nameData = scoreMap.get(score);
			
			scoreboard.getTeam("teams-" + nameData.idx).unregister();
			scoreboard.resetScores(nameData.name);
			
			scoreMap.remove(score);
			
			String text = nameData.name;
			
			if(nameData.prefix != null){
				text = nameData.prefix + text;
				
				if(nameData.suffix != null)
					text += nameData.suffix;
			}
			
			nameMap.remove(text);
		}
	}
	
	public void update(int score, String text) {
		
		remove(score);
		add(score, text);
	}
	
	public void destroy() {
		
		List<Team> teams = new ArrayList<>(scoreboard.getTeams());
		
		for(Team team : teams)
			team.unregister();
		
		List<String> players = new ArrayList<>(scoreboard.getEntries());
		
		for(String player : players)
			scoreboard.resetScores(player);
		
		objective.unregister();
		scoreMap.clear();
	}
 
	public Scoreboard getScoreboard() {
		
		return scoreboard;
	}
	
	public Objective getObjective() {
		
		return objective;
	}
	
	private String fixDuplicates(String text) {
		
		while (nameMap.containsKey(text))
			text += "§r";
		
		if (text.length() > 48)
			text = text.substring(0, 47);
		
		return text;
	}
	
	static int netIdx = 0;
    
	class NameData {
		
		private String prefix, name, suffix;
		private int idx;
 
		public NameData(String text) {
			name = text;
			
			if (text.length() > 16) {
				Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
				
				prefix = iterator.next();
				name = iterator.next();
				
				if(text.length() > 32)
					suffix = iterator.next();
			}
			
			idx = netIdx++;
		}
	}
}