
package com.andrewyunt.warfare.managers;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.objects.SignDisplay;
import com.andrewyunt.warfare.objects.SignDisplay.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SignManager {
    private final Warfare warfare;
	private final Set<SignDisplay> signs = new HashSet<>();

    public SignManager(Warfare warfare) {
        this.warfare = warfare;
        Bukkit.getScheduler().runTaskTimerAsynchronously(warfare, () -> {
            for(SignDisplay.Type type: SignDisplay.Type.values()){
                String name = type.name().toLowerCase();
                Map<Integer, Map.Entry<Object, Integer>> map = warfare.getStorageManager().getTopFiveColumn("Players", "name", name);
                Bukkit.getScheduler().runTask(warfare, () -> {
                    for(SignDisplay signDisplay: signs.stream().filter(sign -> sign.getType() == type).collect(Collectors.toSet())){
                        signDisplay.refresh(map);
                    }
                });
            }
        }, 20 * 5, 20 * 30);
    }

    public void createSign(Location loc, Type type, int place, boolean load) throws SignException {
		
		if (place == 0 || loc == null) {
            throw new SignException();
        }
		
		SignDisplay sign = new SignDisplay(loc, type, place);
		signs.add(sign);

		if(!load) {
			Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getStorageManager().saveSign(sign));
		}
	}
	
	public void deleteSign(SignDisplay sign) throws SignException {

		if (sign == null) {
            throw new SignException();
        }

		signs.remove(sign);

		Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getStorageManager().deleteSign(sign));
	}

	/**
	 * Gets all registered signs on the server.
	 * 
	 * @return
	 * 		A collection of all registered signs on the server.
	 */
	public Set<SignDisplay> getSigns() {

		return signs;
	}

	/**
	 * Gets a registered sign of the specified name.
	 * 
	 * @param loc
	 * 		The location of the specified sign.
	 * @return
	 * 		The sign fetched of the specified location.
	 * @throws SignException
	 * 		If a sign with the specified name does not exist.
	 */
	public SignDisplay getSign(Location loc) throws SignException {
		
		for (SignDisplay signDisplay : signs) {
            if (signDisplay.getBukkitSign() != null) {
                if (loc == signDisplay.getBukkitSign().getLocation()) {
                    return signDisplay;
                }
            }
        }
			
		throw new SignException("The specified sign does not exist.");
	}

	public boolean signExists(Location loc) {

		try {
			getSign(loc);
		} catch (SignException e) {
			return false;
		}
		
		return true;
	}
}