package com.andrewyunt.warfare.configuration;

import java.io.*;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.andrewyunt.warfare.Warfare;

/**
 *
 * The class used for the server.yml configuration file.
 *
 * @author Andrew Yunt
 */
public class ServerConfiguration {

    private FileConfiguration config = null;
    private File configFile = null;

    public void reloadConfig() {

        if (configFile == null) {
            configFile = new File("server.yml");
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        Reader defConfigStream = null;

        try {
            defConfigStream = new InputStreamReader(new FileInputStream(new File("server.yml")), "UTF8");
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }

        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {

        if (config == null) {
            reloadConfig();
        }

        return config;
    }

    public void saveConfig() {

        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            Warfare.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {

        if (configFile == null) {
            configFile = new File("server.yml");
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            getConfig().set("server-name", "server name");
            saveConfig();
        }
    }
}