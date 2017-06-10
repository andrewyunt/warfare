package com.andrewyunt.warfare.managers;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.lobby.SignDisplay;
import com.andrewyunt.warfare.lobby.SignDisplay.Type;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SignManager {

    @Getter private final Set<SignDisplay> signs = new HashSet<>();

    public SignManager() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Warfare.getInstance(), () -> {
            for (SignDisplay.Type type: SignDisplay.Type.values()) {
                String id = type.getId();
                Map<Integer, Map.Entry<Object, Double>> map = Warfare.getInstance().getStorageManager()
						.getTopFiveColumn("Players", "name", id);
                Bukkit.getScheduler().runTask(Warfare.getInstance(), () -> {
                    for (SignDisplay signDisplay: signs.stream().filter(sign -> sign.getType() == type).collect(Collectors.toSet())) {
                        signDisplay.refresh(map);
                    }
                });
            }
        }, 20 * 5, 20 * 30);
    }

    public void createSign(Location loc, Type type, int place, boolean load) {
		SignDisplay sign = new SignDisplay(loc, type, place);
		signs.add(sign);

		if (!load) {
			Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getStorageManager().saveSign(sign));
		}
	}
	
	public void deleteSign(SignDisplay sign) {
		signs.remove(sign);

		Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getStorageManager().deleteSign(sign));
	}

	/**
	 * Gets a registered sign of the specified name.
	 * 
	 * @param loc
	 * 		The location of the specified sign.
	 * @return
	 * 		The sign fetched of the specified location.
	 */
	public SignDisplay getSign(Location loc) {
		for (SignDisplay signDisplay : signs) {
            if (signDisplay.getBukkitSign() != null) {
            	Location displayLoc = signDisplay.getBukkitSign().getLocation();
                if (Objects.equals(loc.getWorld().getBlockAt(loc), displayLoc.getWorld().getBlockAt(displayLoc))) {
                    return signDisplay;
                }
            }
        }

        return null;
	}
}