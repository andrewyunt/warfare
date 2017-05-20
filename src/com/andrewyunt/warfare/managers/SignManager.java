
package com.andrewyunt.warfare.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.objects.SignDisplay;
import com.andrewyunt.warfare.objects.SignDisplay.Type;

import java.util.HashSet;
import java.util.Set;

public class SignManager {
	
	public final Set<SignDisplay> signs = new HashSet<>();

	public void createSign(Location loc, Type type, int place, boolean load) throws SignException {
		
		if (place == 0 || loc == null) {
            throw new SignException();
        }
		
		SignDisplay sign = new SignDisplay(loc, type, place);
		signs.add(sign);

		if(!load) {
			Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getMySQLManager().saveSign(sign));
		}
	}
	
	public void deleteSign(SignDisplay sign) throws SignException {

		if (sign == null) {
            throw new SignException();
        }

		signs.remove(sign);

		Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getMySQLManager().deleteSign(sign));
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