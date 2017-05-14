/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Andrew Yunt. The intellectual and technical concepts contained
 * herein are proprietary to Andrew Yunt and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Andrew Yunt. Access to the source code contained herein is hereby forbidden to anyone except current Andrew Yunt and those who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ANDREW YUNT IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.andrewyunt.warfare.managers;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.SignConfiguration;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.objects.SignDisplay;
import com.andrewyunt.warfare.objects.SignDisplay.Type;
import com.andrewyunt.warfare.utilities.Utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SignManager {
	
	public final Set<SignDisplay> signs = new HashSet<SignDisplay>();

	public void createSign(Location loc, Type type, int place) throws SignException {
		
		if (place == 0 || loc == null) {
            throw new SignException();
        }
		
		SignDisplay sign = new SignDisplay(
				Utils.getHighestEntry(Warfare.getInstance().getSignConfig().getConfig()
						.getConfigurationSection("signs")) + 1,
				loc, type, place, false);
		signs.add(sign);

    }
	
	public void deleteSign(SignDisplay sign) throws SignException {

		if (sign == null) {
            throw new SignException();
        }

		signs.remove(sign);
		
		SignConfiguration signConfig = Warfare.getInstance().getSignConfig();
		
		signConfig.getConfig().set("signs." + String.valueOf(sign.getConfigNumber()), null);
		signConfig.saveConfig();
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
	
	/**
	 * Iterates through all signs in the signs.yml file and loads them.
	 */
	public void loadSigns() {

		signs.clear(); // Clear the current signs list

		if (!Warfare.getInstance().getSignConfig().getConfig().contains("signs")) {
            return;
        }

		ConfigurationSection signs = Warfare.getInstance().getSignConfig().getConfig()
				.getConfigurationSection("signs");
		
		if (signs == null) {
            return;
        }

		Map<String, Object> cfgValues = signs.getValues(false);
		
		for (String name : cfgValues.keySet()) {
            loadSign(signs.getConfigurationSection(name));
        }
	}

	/**
	 * Loads the sign from the specified configuration section.
	 * 
	 * @param section
	 * 		The configuration section for the sign to be loaded.
	 * @return
	 * 		The loaded sign from the specified configuration section.
	 */
	public void loadSign(ConfigurationSection section) {

		SignDisplay sign = SignDisplay.loadFromConfig(section);

		Location loc = Utils.deserializeLocation(section.getConfigurationSection("location"));
		
		if (!signExists(loc)) {
			try {
				signs.remove(getSign(loc));
			} catch (SignException e) {
				e.printStackTrace();
			}
		}

		signs.add(sign);

	}
}