package com.comze_instancelabs.destroyerminigame;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StatsConfig {

    private FileConfiguration statsConfig = null;
    private File statsFile = null;
    private Main m = null;
    
    public StatsConfig(Main m){
    	this.m = m;
    }
    
    public FileConfiguration getConfig() {
        if (statsConfig == null) {
            reloadConfig();
        }
        return statsConfig;
    }
    
    public void saveConfig() {
        if (statsConfig == null || statsFile == null) {
            return;
        }
        try {
            getConfig().save(statsFile);
        } catch (IOException ex) {
            
        }
    }
    
    public void reloadConfig() {
        if (statsFile == null) {
        	statsFile = new File(m.getDataFolder(), "stats.yml");
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        InputStream defConfigStream = m.getResource("stats.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            statsConfig.setDefaults(defConfig);
        }
    }
}

