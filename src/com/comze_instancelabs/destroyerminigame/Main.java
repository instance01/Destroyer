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
    				}else if(action.equalsIgnoreCase("setlobby")){
    					// /dest setlobby {count} [name], where {count} is 1 or 2
    				}else if(action.equalsIgnoreCase("setspawn")){
    					// /dest setspawn {count} [name], where {count} is 1 or 2
    				}else if(action.equalsIgnoreCase("setbeacon")){
    					// /dest setbeacon {count} [name], where {count} is 1 or 2
    				}
    				//TODO: create all commands
    			}else{
    				//TODO: display help
    			}
    		}else{
    			sender.sendMessage("§4Please execute this command ingame!");
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
                	joinArena(event.getPlayer().getName(), s.getLine(1).substring(2));
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
	
	
	public boolean isOnline(String player){
		return Bukkit.getPlayer(player).isOnline();
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
			final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobby1.world")), getConfig().getDouble(arena + ".lobby1.x"), getConfig().getDouble(arena + ".lobby1.y"), getConfig().getDouble(arena + ".lobby1.z"));
			Bukkit.getPlayer(player).teleport(t);	
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
	
	public void leaveArena(){
		
	}
	
	public void resetArena(){
		
	}
	
	public void ArenaDraw(){
		
	}
	
	public void teamWin(){
		
	}
	
	public void teamLoose(){
		
	}
}
