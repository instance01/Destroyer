package com.comze_instancelabs.destroyerminigame;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	
	public static HashMap<String, String> arenap = new HashMap<String, String>(); // player -> arena
	public static HashMap<String, Integer> arenapcount = new HashMap<String, Integer>(); // arena -> current players in arena
	public static HashMap<String, String> arena_state = new HashMap<String, String>(); // arena -> [join], [ingame], [restarting]
	public static HashMap<String, Integer> team = new HashMap<String, Integer>(); // player -> team integer
	public static HashMap<String, Boolean> current_team_selection = new HashMap<String, Boolean>();

	Utils u = null;
	
	public int maxplayers_perteam = 0;
	
	/*
	 * we got a startup lobby (lobby1) with the join signs
	 * we got a waiting lobby (lobby2) with the classes
	 * we got two spawn points for the teams
	 * we got two beacons for the teams
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
    						String arena = args[1];
    						getConfig().set("arenas." + arena + ".name", arena);
    						this.saveConfig();
    						sender.sendMessage("§2Successfully started creation of an arena.");
    					}else{
    						sender.sendMessage("§3Usage: §2/dest createarena [name]");
    					}
    				}else if(action.equalsIgnoreCase("setlobby")){
    					// /dest setlobby {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						String count = args[1];
    						String arena = args[2];
    						getConfig().set("arenas." + arena + ".lobby" + count + ".world", arena);
    						getConfig().set("arenas." + arena + ".lobby" + count + ".location.x", p.getLocation().getBlockX());
    						getConfig().set("arenas." + arena + ".lobby" + count + ".location.y", p.getLocation().getBlockY());
    						getConfig().set("arenas." + arena + ".lobby" + count + ".location.z", p.getLocation().getBlockZ());
    						this.saveConfig();
    						sender.sendMessage("§2Lobby " + count + " registered.");
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setlobby {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setspawn")){
    					// /dest setspawn {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						String count = args[1];
    						String arena = args[2];
    						getConfig().set("arenas." + arena + ".spawn" + count + ".world", arena);
    						getConfig().set("arenas." + arena + ".spawn" + count + ".location.x", p.getLocation().getBlockX());
    						getConfig().set("arenas." + arena + ".spawn" + count + ".location.y", p.getLocation().getBlockY());
    						getConfig().set("arenas." + arena + ".spawn" + count + ".location.z", p.getLocation().getBlockZ());
    						this.saveConfig();
    						sender.sendMessage("§2Spawn " + count + " registered.");
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setspawn {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setbeacon")){
    					// /dest setbeacon {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						String count = args[1];
    						String arena = args[2];
    						getConfig().set("arenas." + arena + ".beacon" + count + ".world", arena);
    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.x", p.getLocation().getBlockX());
    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.y", p.getLocation().getBlockY());
    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.z", p.getLocation().getBlockZ());
    						sender.sendMessage("§2Beacon " + count + " registered. PLEASE DO NOT DESTROY THIS BEACON! Use /dest removebeacon {count} [name] !");
    						this.saveConfig();
    						p.getWorld().getBlockAt(p.getLocation()).setType(Material.BEACON);
    					}else{
    						sender.sendMessage("§3Usage: §2/dest setbeacon {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("removearena")){
    					// /dest removearena [name]
    					if(args.length > 1){
    						String arena = args[1];
    						getConfig().set("arenas." + arena, null);
    						this.saveConfig();
    						sender.sendMessage("§2Successfully removed arena '§3" + arena + "§2'.");
    					}else{
    						sender.sendMessage("§3Usage: §2/dest removearena [name]");
    					}
    				}else if(action.equalsIgnoreCase("removebeacon")){
    					// /dest removebeacon {count} [name]
    					Player p = (Player)sender;
    					if(args.length > 2){
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
    						sender.sendMessage("§2Successfully removed arena '§3" + arena + "§2'.");
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
    				}else if(action.equalsIgnoreCase("help")){
    					// /dest help
    					sender.sendMessage("§2Help:");
    				}
    				//TODO: create all commands
    			}else{
    				//TODO: display help
    				sender.sendMessage("§2Help: ");
    			}
    			return true;
    		}else{
    			sender.sendMessage("§2Please execute this command ingame!");
    			return true;
    		}
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
		//TODO: finish
		if(getConfig().isSet("arenas." + arena + ".name") && getConfig().isSet("arenas." + arena + ".lobby1") && getConfig().isSet("arenas." + arena + ".lobby2") && getConfig().isSet("arenas." + arena + ".spawn1") && getConfig().isSet("arenas." + arena + ".spawn2") && getConfig().isSet("arenas." + arena + ".beacon1") && getConfig().isSet("arenas." + arena + ".beacon2")){
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

	
	public void joinArena(String player, String arena){
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
			getLogger().info(t.toString());
			getLogger().info(getServer().getPlayer(player).getName());
			getServer().getPlayer(player).teleport(t);
		}
		
		// start game if enough players
		if(count > (maxplayers_perteam * 2 - 1)){
			startArena();
		}
		
	}
	
	public void setTeam(String player, String arena, boolean team){
		if(team){
			// team 1
		}else if(!team){
			// team 2
		}
	}
	
	public void startArena(){
		
	}
	
	public void leaveArena(String player, String arena){
		
	}
	
	public void resetArena(){
		
	}
	
	public void ArenaDraw(){
		
	}
	
	public void teamWin(){
		
	}
	
	public void teamLoose(){
		
	}
	
	public void die(){
		// save in stats if you die
	}
	
	public void kill(){
		// save in stats if you kill someone
	}
}
