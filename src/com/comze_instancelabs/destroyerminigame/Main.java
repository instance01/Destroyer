package com.comze_instancelabs.destroyerminigame;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;


public class Main extends JavaPlugin implements Listener {

	
	public static HashMap<String, String> arenap = new HashMap<String, String>(); // player -> arena
	public static HashMap<String, Integer> arenapcount = new HashMap<String, Integer>(); // arena -> current players in arena
	public static HashMap<String, String> arena_state = new HashMap<String, String>(); // arena -> [join], [ingame], [restarting]
	public static HashMap<String, Integer> pteam = new HashMap<String, Integer>(); // player -> team integer
	public static HashMap<String, Boolean> current_team_selection = new HashMap<String, Boolean>();

	Utils u = null;
	
	public int maxplayers_perteam = 0;
	
	/*
	 * Setup:
	 * dest createarena [name]
	 * dest setboundaries {count} [name]
	 * dest setspawn {count} [name]
	 * dest setlobby {count} [name]
	 * dest setbeacon {count} [name]
	 * 
	 */
	
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		
		getConfig().addDefault("config.maxplayers_per_team", 10);
		getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		this.saveConfig();
		
		maxplayers_perteam = getConfig().getInt("config.maxplayers_per_team");
		
		u = new Utils(this);
	}
	
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("destroyer") || cmd.getName().equalsIgnoreCase("dest")){
    		if(sender instanceof Player){
    			if(args.length > 0){
    				String action = args[0];
    				if(action.equalsIgnoreCase("createarena")){
    					// /dest createarena [name]
    					if(args.length > 1){
    						if(sender.hasPermission("destroyer.createarena")){
	    						String arena = args[1];
	    						getConfig().set("arenas." + arena + ".name", arena);
	    						this.saveConfig();
	    						sender.sendMessage("§2Successfully started creation of an arena.");	
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest createarena [name]");
    					}
    				}else if(action.equalsIgnoreCase("setboundaries")){
    					// /dest setboundaries {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("destroyer.createarena")){
	    						String count = args[1];
	    						String arena = args[2];
	    						getConfig().set("arenas." + arena + ".boundary" + count + ".world", p.getWorld().getName());
	    						getConfig().set("arenas." + arena + ".boundary" + count + ".location.x", p.getLocation().getBlockX());
	    						getConfig().set("arenas." + arena + ".boundary" + count + ".location.y", p.getLocation().getBlockY());
	    						getConfig().set("arenas." + arena + ".boundary" + count + ".location.z", p.getLocation().getBlockZ());
	    						this.saveConfig();
	    						sender.sendMessage("§2Boundary " + count + " registered.");	
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setlobby {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setlobby")){
    					// /dest setlobby {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("destroyer.createarena")){
	    						String count = args[1];
	    						String arena = args[2];
	    						getConfig().set("arenas." + arena + ".lobby" + count + ".world", p.getWorld().getName());
	    						getConfig().set("arenas." + arena + ".lobby" + count + ".location.x", p.getLocation().getBlockX());
	    						getConfig().set("arenas." + arena + ".lobby" + count + ".location.y", p.getLocation().getBlockY());
	    						getConfig().set("arenas." + arena + ".lobby" + count + ".location.z", p.getLocation().getBlockZ());
	    						this.saveConfig();
	    						sender.sendMessage("§2Lobby " + count + " registered.");	
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setlobby {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setspawn")){
    					// /dest setspawn {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("destroyer.createarena")){
	    						String count = args[1];
	    						String arena = args[2];
	    						getConfig().set("arenas." + arena + ".spawn" + count + ".world", p.getWorld().getName());
	    						getConfig().set("arenas." + arena + ".spawn" + count + ".location.x", p.getLocation().getBlockX());
	    						getConfig().set("arenas." + arena + ".spawn" + count + ".location.y", p.getLocation().getBlockY());
	    						getConfig().set("arenas." + arena + ".spawn" + count + ".location.z", p.getLocation().getBlockZ());
	    						this.saveConfig();
	    						sender.sendMessage("§2Spawn " + count + " registered.");	
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setspawn {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setbeacon")){
    					// /dest setbeacon {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("destroyer.createarena")){
	    						String count = args[1];
	    						String arena = args[2];
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".world", p.getWorld().getName());
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.x", p.getLocation().getBlockX());
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.y", p.getLocation().getBlockY());
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.z", p.getLocation().getBlockZ());
	    						sender.sendMessage("§2Beacon " + count + " registered. PLEASE DO NOT DESTROY THIS BEACON! Use /dest removebeacon {count} [name] !");
	    						this.saveConfig();
	    						Block b = p.getWorld().getBlockAt(p.getLocation());
	    						b.setType(Material.BEACON);
	    						Field field = null;
								try {
									field = net.minecraft.server.v1_6_R3.Block.class.getDeclaredField("strength");
								} catch (NoSuchFieldException | SecurityException e) {
									getLogger().severe("This version doesn't support your craftbukkit version!");
								}
	    						field.setAccessible(true);
	    						try {
									field.setFloat(b, 50.0F);
								} catch (IllegalArgumentException | IllegalAccessException e) {
									getLogger().severe("This version doesn't support your craftbukkit version!");
								}	
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setbeacon {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("removearena")){
    					// /dest removearena [name]
    					if(args.length > 1){
    						if(sender.hasPermission("destroyer.removearena")){
	    						String arena = args[1];
	    						getConfig().set("arenas." + arena, null);
	    						this.saveConfig();
	    						sender.sendMessage("§2Successfully removed arena '§3" + arena + "§2'.");
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest removearena [name]");
    					}
    				}else if(action.equalsIgnoreCase("removebeacon")){
    					// /dest removebeacon {count} [name]
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("destroyer.removearena")){
	    						String count = args[1];
	    						String arena = args[2];
	    						try{
	    					    	p.getWorld().getBlockAt(getComponentFromArena(arena, "beacon", count)).setType(Material.AIR);
	    						}catch(Exception e){
	    							sender.sendMessage("§2Could not find beacon for §3" + arena + "§2!");
	    							return true;
	    						}
	    						getConfig().set("arenas." + arena + ".beacon" + count, null);
	    						this.saveConfig();
	    						sender.sendMessage("§2Successfully removed beacon " + count + " for arena '§3" + arena + "§2'.");	
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/dest removearena [name]");
    					}
    				}else if(action.equalsIgnoreCase("leave")){
    					Player p = (Player)sender;
    					if(arenap.containsKey(p.getName())){
    						leaveArena(p.getName(), arenap.get(p.getName()));
    					}else{
    						sender.sendMessage("§4You're not in an arena right now!");
    					}
    				}else if(action.equalsIgnoreCase("list")){
    					// /dest list
    					if(sender.hasPermission("destroyer.listarenas")){
    						//TODO: list arenas
    					}
    				}else if(action.equalsIgnoreCase("stats")){
    					// /dest stats
    					Player p = (Player)sender;
    					String name = p.getName();
    					
    					p.sendMessage("§3Destroyer statistics: ");
    					p.sendMessage("§3Team Wins: §2" + this.getStatsComponent(name, "teamwins"));
    					p.sendMessage("§3Team Loses: §4" + this.getStatsComponent(name, "teamloses"));
    					p.sendMessage("§3Kills: §2" + this.getStatsComponent(name, "kills"));
    					p.sendMessage("§3Deaths: §4" + this.getStatsComponent(name, "deaths"));
    				}else if(action.equalsIgnoreCase("help")){
    					// /dest help
    					//TODO: display help
    					sender.sendMessage("§2Help:");
    				}
    			}else{
    				//TODO: display help
    				sender.sendMessage("§2Help: ");
    			}
    			return true;
    		}else{
    			sender.sendMessage("§2Please execute this command ingame!");
    			return true;
    		}
    	}else if(cmd.getName().equalsIgnoreCase("destadmin")){
    		if(sender.isOp()){
	    		if(args.length > 0){
	    			String action = args[0];
	    			if(action.equalsIgnoreCase("reset")){
						if(args.length > 1){
							resetArena(args[1]);
						}else{
							sender.sendMessage("§3Usage: §4/destadmin reset [name]");
						}
					}else if(action.equalsIgnoreCase("start")){
						if(args.length > 1){
							startArena(args[1]);
						}else{
							sender.sendMessage("§3Usage: §4/destadmin start [name]");
						}
					}else if(action.equalsIgnoreCase("end")){
						if(args.length > 1){
							resetArena(args[1]);
						}else{
							sender.sendMessage("§3Usage: §4/destadmin end [name]");
						}
					}else if(action.equalsIgnoreCase("savearena")){
						if(args.length > 1){
							File f = new File(args[1]);
							f.delete();
							saveArenaToFile(args[1]);
						}else{
							sender.sendMessage("§3Usage: §4/destadmin savearena [name]");
						}
					}
	    		}
    		}
    		
    		return true;
    	}
    	return false;
    }
    
	@EventHandler
	public void onSignUse(PlayerInteractEvent event)
	{	
	    if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();

                if (s.getLine(0).equalsIgnoreCase("§2[destroyer]")){
                	if(isValidArena(s.getLine(1).substring(2))){
                		joinArena(event.getPlayer().getName(), s.getLine(1).substring(2));	
                	}else{
                		event.getPlayer().sendMessage("§4This arena is set up wrong.");
                	}
                	
                }else if(s.getLine(0).equalsIgnoreCase("§2[d-class]")){
                	
                }
	        }
	    }
	}
	
	
	@EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).toLowerCase().contains("[destroyer]")){
        	if(event.getPlayer().hasPermission("destroyer.sign")){
	        	event.setLine(0, "§2[Destroyer]");
	        	if(!event.getLine(1).equalsIgnoreCase("")){
	        		String arena = event.getLine(1);
	        		if(isValidArena(arena)){
	        			getConfig().set("arenas." + arena + ".sign.world", p.getWorld().getName());
	        			getConfig().set("arenas." + arena + ".sign.location.x", p.getLocation().getBlockX());
						getConfig().set("arenas." + arena + ".sign.location.y", p.getLocation().getBlockY());
						getConfig().set("arenas." + arena + ".sign.location.z", p.getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("§2Successfully created arena sign.");
	        		}else{
	        			p.sendMessage("§2The arena appears to be invalid (missing components or misstyped arena)!");
	        			event.getBlock().breakNaturally();
	        		}
	        		event.setLine(1, "§5" +  arena);
	        	}
        	}
        }else if(event.getLine(0).toLowerCase().contains("[d-class]")){
        	if(event.getPlayer().hasPermission("destroyer.sign")){
	        	event.setLine(0, "§2[D-Class]");
	        	event.getPlayer().sendMessage("§2You have successfully created a class sign for Destroyer!");
        	}
        }
	}
	
	public boolean isValidArena(String arena){
		if(getConfig().isSet("arenas." + arena + ".name") && getConfig().isSet("arenas." + arena + ".boundary1") && getConfig().isSet("arenas." + arena + ".boundary2") && getConfig().isSet("arenas." + arena + ".lobby1") && getConfig().isSet("arenas." + arena + ".lobby2") && getConfig().isSet("arenas." + arena + ".spawn1") && getConfig().isSet("arenas." + arena + ".spawn2") && getConfig().isSet("arenas." + arena + ".beacon1") && getConfig().isSet("arenas." + arena + ".beacon2")){
			return true;
		}
		return false;
	}
	
	public boolean isOnline(String player){
		return Bukkit.getPlayer(player).isOnline();
	}
	
	public Location getComponentFromArena(String arena, String component, String count){
		if(isValidArena(arena)){
			String base = "arenas." + arena + "." + component + count;
			return new Location(Bukkit.getWorld(getConfig().getString(base + ".world")), getConfig().getInt(base + ".location.x"), getConfig().getInt(base + ".location.y"), getConfig().getInt(base + ".location.z"));
		}
		return null;
	}
	
	public void saveStatsComponent(String player, String component, String value){
		getConfig().set("stats." + player + "." + component, value);
		this.saveConfig();
	}
	
	public String getStatsComponent(String player, String component){
		if(getConfig().isSet("stats." + player + "." + component)){
			return getConfig().getString("stats." + player + "." + component);
		}else{
			return "0";
		}
	}

	
	public void joinArena(final String player, String arena){
		// prepare player
		arenap.put(player, arena);
		int count = 1;
		boolean currentteamselection = true;
		if(arenapcount.containsKey(arena)){
			count = arenapcount.get(arena) + 1;
			currentteamselection = !current_team_selection.get(arena);
		}
		
		current_team_selection.put(arena, currentteamselection);
		arenapcount.put(arena, count);
		setTeam(player, arena, currentteamselection);
		
		if(isOnline(player)){
			final Location t = getComponentFromArena(arena, "lobby", "2");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					getServer().getPlayer(player).teleport(t);
				}
			}, 10);
		}
		
		// start game if enough players
		if(count > (maxplayers_perteam * 2 - 1)){
			startArena(arena);
		}
		
	}
	
	public void setTeam(String player, String arena, boolean team){
		if(team){
			// team 1
			pteam.put(player, 1);
		}else if(!team){
			// team 2
			pteam.put(player, 2);
		}
	}
	
	public void startArena(String arena){
		for(final String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(isOnline(player)){
					getServer().getPlayer(player).sendMessage("§3The game has started!");
					
					final Location t = this.getComponentFromArena(arena, "spawn", Integer.toString(pteam.get(player)));
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							getServer().getPlayer(player).teleport(t);
						}
					}, 10);
					
					//TODO: give the player his class
				}
			}
		}
	}
	
	public void leaveArena(final String player, String arena){
		if(isOnline(player)){
			final Location t = getComponentFromArena(arena, "lobby", "1");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					getServer().getPlayer(player).teleport(t);
				}
			}, 10);
		}
		
		arenap.remove(player);
		int count = 1;
		if(arenapcount.containsKey(arena)){
			count = arenapcount.get(arena) - 1;
		}
		arenapcount.put(arena, count);
		/*if(count < 3){ // if count is less than 2 now
			// last man standing
		}*/
	}
	
	public void resetArena(String arena){
		for(final String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(isOnline(player)){
					final Location t = getComponentFromArena(arena, "lobby", "1");
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							getServer().getPlayer(player).teleport(t);
						}
					}, 10);
					pteam.remove(player);
					getServer().getPlayer(player).sendMessage("§4The game has ended!");
				}
			}
		}
		
		while (arenap.values().remove(arena));
		arenapcount.remove(arena);
		
		//TODO: reset map mechanism
		
		this.loadArenaFromFile(arena);
	}
	
	public void ArenaDraw(String arena){ // if time runs out -> draw
		resetArena(arena);
	}
	
	public void teamWin(String arena, int team){
		//TODO: save win stats, broadcast team that won
		for(String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(pteam.get(player) == team){
					int nbef = Integer.parseInt(this.getStatsComponent(player, "teamwin")) + 1;
					this.saveStatsComponent(player, "teamwin", Integer.toString(nbef));
					if(getServer().getPlayer(player).isOnline()){
						getServer().getPlayer(player).sendMessage("§4Congratulations, you won this game!");
					}
				}
			}
		}
		resetArena(arena);
	}
	
	public void teamLose(String arena, int team){
		//TODO: save lose stats
		for(String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(pteam.get(player) == team){
					int nbef = Integer.parseInt(this.getStatsComponent(player, "teamlose")) + 1;
					this.saveStatsComponent(player, "teamlose", Integer.toString(nbef));
					if(getServer().getPlayer(player).isOnline()){
						getServer().getPlayer(player).sendMessage("§4Congratulations, you won this game!");
					}
				}
			}
		}
		resetArena(arena);
	}
	
	public void die(String player){
		// save in stats if you die
		int nbef = Integer.parseInt(this.getStatsComponent(player, "deaths")) + 1;
		this.saveStatsComponent(player, "deaths", Integer.toString(nbef));
	}
	
	public void kill(String player){
		// save in stats if you kill someone
		int nbef = Integer.parseInt(this.getStatsComponent(player, "kills")) + 1;
		this.saveStatsComponent(player, "kills", Integer.toString(nbef));
	}
	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(arenap.containsKey(event.getPlayer().getName())){
			if(event.getBlock().getType().equals(Material.BEACON)){
				// TODO: arena boundaries
				Player p = event.getPlayer();
				// if it's not the own teams beacon
				
				if(!compareTwoLocations(event.getBlock().getLocation(), this.getComponentFromArena(arenap.get(p.getName()), "beacon", Integer.toString(pteam.get(p.getName()))))){
					int teamint = pteam.get(p.getName());
					teamWin(arenap.get(p.getName()), teamint);
				}else{
					event.setCancelled(true);
				}
			}
		}
	}
	
	public boolean compareTwoLocations(Location l1, Location l2){
		if(l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ()){
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(arenap.containsKey(event.getPlayer().getName())){
			// TODO: arena boundaries
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		if(arenap.containsKey(event.getPlayer().getName())){
			leaveArena(event.getPlayer().getName(), arenap.get(event.getPlayer().getName()));
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		// TODO: teleport player out of map, if left while a game was running
	}

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
    	if(event.getEntity().getKiller() != null){
            if(event.getEntity().getKiller() instanceof Player && event.getEntity() instanceof Player && arenap.containsKey(event.getEntity()) && arenap.containsKey(event.getEntity().getKiller())){
                event.getEntity().setHealth(20);
            	String killerName = event.getEntity().getKiller().getName();
                String entityKilled = event.getEntity().getName();
                //getLogger().info(killerName + " killed " + entityKilled);
                Player p1 = event.getEntity().getKiller();
                final Player p2 = event.getEntity();
                String arena = arenap.get(p1.getName());
                p2.playSound(p2.getLocation(), Sound.CAT_MEOW, 1F, 1);

                die(p2.getName());
                kill(p1.getName());
                
                final Location t = this.getComponentFromArena(arena, "spawn", Integer.toString(pteam.get(p2.getName())));
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						p2.teleport(t);
					}
				}, 5);
            }
        }	
    }
	
    
    // disable starvation
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.getEntity() instanceof Player){
    		Player p = (Player)event.getEntity();
    		if(event.getCause() == DamageCause.STARVATION){
    			if(arenap.containsKey(p.getName())){
    				event.setCancelled(true);
    			}
    		}
    	}
    }
    

    public void saveArenaToFile(String arena){
    	File f = new File(arena);
    	Cuboid c = new Cuboid(this.getComponentFromArena(arena, "boundary", "1"), this.getComponentFromArena(arena, "boundary", "2"));
    	Location start = c.getLowLoc();
    	Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();
		
		getLogger().info(Integer.toString(width) + " " + Integer.toString(height) +  " " + Integer.toString(length)); 
		
		
		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try{
			fos = new FileOutputStream(arena);	
			oos = new BukkitObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		for (int i = 0; i <= width; i++) {
			for (int j = 0; j <= height; j++) {
				for(int k = 0; k <= length; k++){
					Block change = c.getWorld().getBlockAt(start.getBlockX() + i, start.getBlockY() + j, start.getBlockZ() + k);
					
					//if(change.getType() != Material.AIR){
						ArenaBlock bl = new ArenaBlock(change);

						try {
							oos.writeObject(bl);
						} catch (IOException e) {
							e.printStackTrace();
						}	
					//}

				}
			}
		}
		
		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		getLogger().info("saved");
    }
    
    public void loadArenaFromFile(String arena){
    	/*Cuboid c = new Cuboid(this.getComponentFromArena(arena, "boundary", "1"), this.getComponentFromArena(arena, "boundary", "2"));
    	Location start = c.getLowLoc();
    	Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();
		*/
		
		FileInputStream fis = null;
		BukkitObjectInputStream ois = null;
		try {
			fis = new FileInputStream(arena);
			ois = new BukkitObjectInputStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}

        

		try {
			while(true)  
			{ 
				Object b = null;
				try{
					b = ois.readObject();
				}catch(EOFException e){
					getLogger().info("finished");
				}
				
				if(b != null){
					//TODO this doesnt work, continuing work tomorrow
					// when i initialize arenablock, the type turns to the new type in the world
					// means the new arena blocks state overwrites the saved one.
					
					//EDIT: WOW. I just fucking made that function getting to work after like 5 hours of work.
					ArenaBlock ablock = (ArenaBlock) b;
					//getLogger().info(ablock.getBlock().getLocation().toString());
					World w = ablock.getBlock().getWorld();
					String n1 = w.getBlockAt(ablock.getBlock().getLocation()).getType().toString();
					String n2 = ablock.getMaterial().toString();
					
					if(!n1.equalsIgnoreCase(n2)){
						//getLogger().info("something wrong here: " + ablock.getBlock().getLocation().toString());
						ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation()).setType(ablock.getMaterial());
					}
				}else{
					break;
				}
			} 

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
            

		try {
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		/*for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				for(int k = 0; k < length; k++){
					Block change = c.getWorld().getBlockAt(start.getBlockX() + width, start.getBlockY() + height, start.getBlockZ() + length);
				}
			}
		}*/
		
		
    }
    
    
    


}

