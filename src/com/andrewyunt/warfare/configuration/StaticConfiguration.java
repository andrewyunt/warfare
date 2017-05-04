package com.andrewyunt.warfare.configuration;

import com.andrewyunt.warfare.Warfare;

public class StaticConfiguration {
    public static boolean LOBBY = Warfare.getInstance().getConfig().getBoolean("is-lobby", false);
}
