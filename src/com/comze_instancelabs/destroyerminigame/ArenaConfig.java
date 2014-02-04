package com.comze_instancelabs.destroyerminigame;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaConfig {

    private FileConfiguration arenaConfig = null;
    private File arenaFile = null;
    private Main m = null;
    
    public ArenaConfig(Main m){
    	this.m = m;
    }
    
    public FileConfiguration getConfig() {
        if (arenaConfig == null) {
            reloadConfig();
        }
        return arenaConfig;
    }
    
    public void saveConfig() {
        if (arenaConfig == null || arenaFile == null) {
            return;
        }
        try {
            getConfig().save(arenaFile);
        } catch (IOException ex) {
            
        }
    }
    
    public void reloadConfig() {
        if (arenaFile == null) {
        	arenaFile = new File(m.getDataFolder(), "arena.yml");
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);

        InputStream defConfigStream = m.getResource("arena.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            arenaConfig.setDefaults(defConfig);
        }
    }
}

