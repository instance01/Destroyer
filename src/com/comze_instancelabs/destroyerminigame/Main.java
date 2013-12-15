package com.comze_instancelabs.destroyerminigame;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/**
 * 
 * @author InstanceLabs
 *
 */


public class Main extends JavaPlugin implements Listener {

	public static Economy econ = null;
	public boolean economy = true;
	
	public static HashMap<String, String> arenap = new HashMap<String, String>(); // player -> arena
	public static HashMap<String, Integer> arenapcount = new HashMap<String, Integer>(); // arena -> current players in arena
	public static HashMap<String, String> arena_state = new HashMap<String, String>(); // arena -> [join], [ingame], [restarting]
	public static HashMap<String, Integer> pteam = new HashMap<String, Integer>(); // player -> team integer
	public static HashMap<String, Boolean> current_team_selection = new HashMap<String, Boolean>();
	public static HashMap<String, AClass> pclass = new HashMap<String, AClass>(); // player -> class
	public static HashMap<String, Boolean> pspectate = new HashMap<String, Boolean>(); // player -> spectate boolean
	public static HashMap<String, AClass> aclasses = new HashMap<String, AClass>(); // classname -> class
	public static HashMap<String, Integer> taskid = new HashMap<String, Integer>(); // arena -> draw task
	
	public static HashMap<String, Boolean> pvpenabled = new HashMap<String, Boolean>(); // player -> pvp enabled or not

	//public static HashMap<String, ItemStack[]> pinv = new HashMap<String, ItemStack[]>(); // player -> inventory
	
	Utils u = null;
	
	public int maxplayers_perteam = 0;
	
	/*
	 * Setup:
	 * dest createarena [name]
	 * dest setboundaries {count} [name]
	 * dest setspawn {count} [name]
	 * dest setlobby {count} [name]
	 * dest setbeacon {count} [name]
	 * dest save [name]
	 * 
	 */
	
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		
		getConfig().addDefault("config.maxplayers_per_team", 10);
		getConfig().addDefault("config.minutes_per_game", 15);
		getConfig().addDefault("config.respawn_time_in_seconds", 5);
		getConfig().addDefault("config.auto_updating", true);
		
		getConfig().addDefault("config.money_rewards", true);
		//getConfig().addDefault("config.money_reward", 100);
		getConfig().addDefault("config.money_reward_per_kill", 10);
		getConfig().addDefault("config.money_reward_per_game", 50);
		getConfig().addDefault("config.item_reward_id", 264);
		getConfig().addDefault("config.item_reward_amount", 1);
		
		getConfig().addDefault("classes.default.name", "default");
		getConfig().addDefault("classes.default.items", "267#1;3#64;3#64");
		
		getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		this.saveConfig();
		
		maxplayers_perteam = getConfig().getInt("config.maxplayers_per_team");
		
		u = new Utils(this);
		
		this.setupEconomy();
		
		// extra test class
		//getConfig().set("classes.pro.name", "pro");
		//getConfig().set("classes.pro.items", "278#1;17#64;17#64;17#64");
		//this.saveConfig();
		
		this.loadClasses();
		
		/*if (getConfig().getBoolean("config.auto_updating")) {
			Updater updater = new Updater(this, 68563, this.getFile(), Updater.UpdateType.DEFAULT, false);
		}*/
		
		if(getConfig().getBoolean("config.use_economy")){
			economy = true;
			if (!setupEconomy()) {
	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
	            economy = false;
	        }
		}
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("thecore") || cmd.getName().equalsIgnoreCase("tc")){
    		if(sender instanceof Player){
    			if(args.length > 0){
    				String action = args[0];
    				if(action.equalsIgnoreCase("createarena")){
    					// /tc createarena [name]
    					if(args.length > 1){
    						if(sender.hasPermission("thecore.createarena")){
	    						String arena = args[1];
	    						getConfig().set("arenas." + arena + ".name", arena);
	    						getConfig().set("arenas." + arena + ".enabled", true);
	    						this.saveConfig();
	    						sender.sendMessage("§2Successfully started creation of an arena.");	
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/tc createarena [name]");
    					}
    				}else if(action.equalsIgnoreCase("setboundaries")){
    					// /tc setboundaries {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("thecore.createarena")){
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
    						sender.sendMessage("§3Usage: §2/tc setlobby {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setlobby")){
    					// /tc setlobby {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("thecore.createarena")){
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
    						sender.sendMessage("§3Usage: §2/tc setlobby {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setspawn")){
    					// /tc setspawn {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("thecore.createarena")){
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
    						sender.sendMessage("§3Usage: §2/tc setspawn {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("setbeacon")){
    					// /tc setbeacon {count} [name], where {count} is 1 or 2
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("thecore.createarena")){
	    						String count = args[1];
	    						String arena = args[2];
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".world", p.getWorld().getName());
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.x", p.getLocation().getBlockX());
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.y", p.getLocation().getBlockY());
	    						getConfig().set("arenas." + arena + ".beacon" + count + ".location.z", p.getLocation().getBlockZ());
	    						sender.sendMessage("§2Beacon " + count + " registered. PLEASE DO NOT DESTROY THIS BEACON! Use /tc removebeacon {count} [name] !");
	    						this.saveConfig();
	    						Block b = p.getWorld().getBlockAt(p.getLocation());
	    						b.setType(Material.BEACON);
	    						Field field = null;
								try {
									field = net.minecraft.server.v1_7_R1.Block.class.getDeclaredField("strength");
								} catch (NoSuchFieldException e1) {
									getLogger().severe("This version doesn't support your craftbukkit version!");
								} catch (SecurityException e){
									getLogger().severe("This version doesn't support your craftbukkit version!");
								}
	    						field.setAccessible(true);
	    						try {
									field.setFloat(b, 50.0F);
								} catch (IllegalArgumentException e1) {
									getLogger().severe("This version doesn't support your craftbukkit version!");
								} catch (IllegalAccessException e){
									getLogger().severe("This version doesn't support your craftbukkit version!");
								}
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/tc setbeacon {count} [name]");
    					}
    				}else if(action.equalsIgnoreCase("removearena")){
    					// /tc removearena [name]
    					if(args.length > 1){
    						if(sender.hasPermission("thecore.removearena")){
	    						String arena = args[1];
	    						getConfig().set("arenas." + arena, null);
	    						this.saveConfig();
	    						sender.sendMessage("§2Successfully removed arena '§3" + arena + "§2'.");
    						}else{
    							sender.sendMessage("§4You don't have permission.");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/tc removearena [name]");
    					}
    				}else if(action.equalsIgnoreCase("removebeacon")){
    					// /tc removebeacon {count} [name]
    					Player p = (Player)sender;
    					if(args.length > 2){
    						if(sender.hasPermission("thecore.removearena")){
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
    						sender.sendMessage("§3Usage: §2/tc removearena [name]");
    					}
    				}else if(action.equalsIgnoreCase("changeclass")){
    					// /tc changeclass [name]
    					Player p = (Player)sender;
    					if(args.length > 1){
    						if(p.hasPermission("thecore.changeclass")){
	    						if(arenap.containsKey(p.getName())){
	    							if(aclasses.containsKey(args[1])){
	    								if(p.hasPermission("tc.class." + args[1])){
	    									this.setClass(args[1], p.getName());
	    									sender.sendMessage("§2Class successfully set!");
	    								}
	    							}else{
	    								String all = "";
	    								for(String class_ : aclasses.keySet()){
	    									all += class_ + ", ";
	    								}
	    								sender.sendMessage("§4This is not a valid class. Possible ones: §3" + all.substring(0, all.length() - 1));
	    							}
	    						}else{
	    							sender.sendMessage("§4You are not in an arena right now.");
	    						}
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/tc changeclass [name].");
    					}
    				}else if(action.equalsIgnoreCase("join")){
    					// /tc join [name]
    					Player p = (Player)sender;
    					if(args.length > 1){
    						String arena = args[1];
    						if(isValidArena(arena)){
    							if(isEnabledArena(arena)){
	    							if(!arenap.containsKey(p.getName())){
	                    				joinArena(p.getName(), arena);	
	                    			}
    							}else{
    								sender.sendMessage("§4This arena is disabled!");
    							}
                        	}else{
                        		p.sendMessage("§4This arena is set up wrong.");
                        	}
    					}else{
    						sender.sendMessage("§3Usage: §2/tc changeclass [name].");
    					}
    				}else if(action.equalsIgnoreCase("spectate")){
    					// /tc spectate [name]
    					Player p = (Player)sender;
    					if(args.length > 1){
    						if(p.hasPermission("thecore.spectate")){
	    						spectateArena(p.getName(), args[1]);
	    						sender.sendMessage("§2Toggled spectator mode");
    						}
    					}else{
    						sender.sendMessage("§3Usage: §2/tc spectate [name].");
    					}
    				}else if(action.equalsIgnoreCase("leave")){
    					Player p = (Player)sender;
    					if(arenap.containsKey(p.getName())){
    						leaveArena(p.getName(), arenap.get(p.getName()));
    					}else{
    						sender.sendMessage("§4You're not in an arena right now!");
    					}
    				}else if(action.equalsIgnoreCase("list")){
    					// /tc list
    					if(sender.hasPermission("thecore.listarenas")){
    						if(getConfig().isSet("arenas")){
    							sender.sendMessage("§2Arenas: ");
	    						for(String arena : getConfig().getConfigurationSection("arenas.").getKeys(false)){
	    							sender.sendMessage("§3" + arena);
	    						}	
    						}else{
    							sender.sendMessage("§4Currently there are no registered arenas!");
    						}
    					}
    				}else if(action.equalsIgnoreCase("reload")){
    					// /tc list
    					if(sender.hasPermission("thecore.reload")){
    						this.reloadConfig();
    						sender.sendMessage("§2Successfully reloaded config!");
    					}
    				}else if(action.equalsIgnoreCase("stats")){
    					// /tc stats
    					Player p = (Player)sender;
    					String name = p.getName();
    					
    					p.sendMessage("§3TheCore statistics: ");
    					p.sendMessage("§3Team Wins: §2" + this.getStatsComponent(name, "teamwin"));
    					p.sendMessage("§3Team Loses: §4" + this.getStatsComponent(name, "teamlose"));
    					p.sendMessage("§3Kills: §2" + this.getStatsComponent(name, "kills"));
    					p.sendMessage("§3Deaths: §4" + this.getStatsComponent(name, "deaths"));
    				}else if(action.equalsIgnoreCase("help")){
    					// /tc help
    					this.sendHelp(sender);
    				}else if(action.equalsIgnoreCase("savearena")){
    					if(sender.hasPermission("thecore.savearena")){
    						Player p = (Player)sender;
	    					if(args.length > 1){
	    						if(isValidArena(args[1])){
		    						File f = new File(this.getDataFolder() + "/" + args[1]);
									f.delete();
									saveArenaToFile(p.getName(), args[1]);	
	    						}else{
	    							sender.sendMessage("§4The arena appears to be invalid (missing components)!");
	    						}
								
							}else{
								sender.sendMessage("§3Usage: §2/tc savearena [name]");
							}	
    					}else{
    						sender.sendMessage("§4You don't have permission.");
    					}
					}else{
						sender.sendMessage("§4Command action not found or parameters missing.");
						this.sendHelp(sender);
					}
    			}else{
    				this.sendHelp(sender);
    			}
    			return true;
    		}else{
    			sender.sendMessage("§2Please execute this command ingame!");
    			return true;
    		}
    	}else if(cmd.getName().equalsIgnoreCase("tcadmin")){
    		if(sender.isOp()){
	    		if(args.length > 0){
	    			String action = args[0];
	    			if(action.equalsIgnoreCase("reset")){
						if(args.length > 1){
							resetArena(args[1]);
						}else{
							sender.sendMessage("§3Usage: §2/tcadmin reset [name]");
						}
					}else if(action.equalsIgnoreCase("start")){
						if(args.length > 1){
							if(isValidArena(args[1])){
								startArena(args[1]);
							}else{
								sender.sendMessage("§4Arena could not be found.");
							}
						}else{
							sender.sendMessage("§3Usage: §2/tcadmin start [name]");
						}
					}else if(action.equalsIgnoreCase("end")){
						if(args.length > 1){
							resetArena(args[1]);
						}else{
							sender.sendMessage("§3Usage: §2/tcadmin end [name]");
						}
					}else if(action.equalsIgnoreCase("enable")){
						if(args.length > 1){
							if(isValidArena(args[1])){
								getConfig().set("arenas." + args[1] + ".enabled", true);
								Sign s = getSignFromArena(args[1]);
								s.setLine(2, "§2[Join]");
								s.update();
								this.saveConfig();
								sender.sendMessage("§2Successfully enabled §3" + args[1]);	
							}else{
								sender.sendMessage("§4Arena could not be found or is missing components.");
							}
						}else{
							sender.sendMessage("§3Usage: §2/tcadmin enable [name]");
						}
					}else if(action.equalsIgnoreCase("disable")){
						if(args.length > 1){
							if(isValidArena(args[1])){
								getConfig().set("arenas." + args[1] + ".enabled", false);
								Sign s = getSignFromArena(args[1]);
								s.setLine(2, "§4[Offline]");
								s.update();
								this.saveConfig();
								sender.sendMessage("§2Successfully disabled §3" + args[1]);	
							}else{
								sender.sendMessage("§4Arena could not be found or is missing components.");
							}
						}else{
							sender.sendMessage("§3Usage: §2/tcadmin disable [name]");
						}
					}else if(action.equalsIgnoreCase("savearena")){
						if(args.length > 1){
							File f = new File(this.getDataFolder() + "/" + args[1]);
							f.delete();
							saveArenaToFile(args[1]);
						}else{
							sender.sendMessage("§3Usage: §2/tcadmin savearena [name]");
						}
					}else{
						sender.sendMessage("§4Command action not found or parameters missing.");
						this.sendHelp(sender);
					}
	    		}
    		}
    		
    		return true;
    	}
    	return false;
    }
    
    
    public void sendHelp(CommandSender sender){
    	sender.sendMessage("§6----------------");
		sender.sendMessage("§2Do the following to set up an arena: ");
		sender.sendMessage("§3/tc createarena [name]");
		sender.sendMessage("§3/tc setboundaries 1 [name] §2and §3/tc setboundaries 2 [name]");
		sender.sendMessage("§3/tc setspawn 1 [name] §2and §3/tc setspawn 2 [name]");
		sender.sendMessage("§3/tc setlobby 1 [name] §2and §3/tc setlobby 2 [name]");
		sender.sendMessage("§3/tc setbeacon 1 [name] §2and §3/tc setbeacon 2 [name]");
		sender.sendMessage("§2Important: §3/tc savearena [name]");
		sender.sendMessage("§6----------------");
    }
    
    
	@EventHandler
	public void onSignUse(PlayerInteractEvent event)
	{	
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
	    {
	    	if(event.getPlayer().getItemInHand().getTypeId() == new ItemStack(Material.WRITTEN_BOOK).getTypeId()){
        		if(arenap.containsKey(event.getPlayer().getName())){

        			IconMenu iconm = new IconMenu("TheCore Classes", 18, new IconMenu.OptionClickEventHandler() {
            			@Override
                        public void onOptionClick(IconMenu.OptionClickEvent event) {
                            String d = event.getName();
                            if(aclasses.containsKey(d)){
								if(event.getPlayer().hasPermission("tc.class." + d)){
									setClass(d, event.getPlayer().getName());
									event.getPlayer().sendMessage("§2Class successfully set!");
								}
							}
                            event.setWillClose(true);
                        }
                    }, this);
        			
        			int count = 0;
        			for(String class_ : aclasses.keySet()){
						iconm.setOption(count, new ItemStack(Material.IRON_SWORD, 1), class_, "TheCore " + class_ + " class");
						count += 1;
					}
        			
                	iconm.open(event.getPlayer());
                	
                	event.setCancelled(true);
        		}
	        }
	    	
	    	if(event.hasBlock()){
		    	if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
		        {
		            final Sign s = (Sign) event.getClickedBlock().getState();
	
	                if (s.getLine(0).equalsIgnoreCase("§1[the core]")){
	                	if(isValidArena(s.getLine(1).substring(2))){
	                		if(isEnabledArena(s.getLine(1).substring(2))){
		                		if(s.getLine(2).equalsIgnoreCase("§2[join]")){
		                			if(!arenap.containsKey(event.getPlayer().getName())){
		                				joinArena(event.getPlayer().getName(), s.getLine(1).substring(2));	
		                			}
		                		}	
	                		}else{
	                			event.getPlayer().sendMessage("§4This arena is disabled!");
	                		}
	                	}else{
	                		event.getPlayer().sendMessage("§4This arena is set up wrong.");
	                	}
	                }else if(s.getLine(0).equalsIgnoreCase("§2[d-class]")){
	                	if(arenap.containsKey(event.getPlayer().getName())){
	                		this.giveClassesBook(event.getPlayer().getName());
	                	}
	                }
		        }	
	    	}
	    }
	}
	
	
	@EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).toLowerCase().contains("[thecore]")){
        	if(event.getPlayer().hasPermission("thecore.sign")){
	        	event.setLine(0, "§1[The Core]");
	        	if(!event.getLine(1).equalsIgnoreCase("")){
	        		String arena = event.getLine(1);
	        		if(isValidArena(arena)){
	        			getConfig().set("arenas." + arena + ".sign.world", p.getWorld().getName());
	        			getConfig().set("arenas." + arena + ".sign.location.x", event.getBlock().getLocation().getBlockX());
						getConfig().set("arenas." + arena + ".sign.location.y", event.getBlock().getLocation().getBlockY());
						getConfig().set("arenas." + arena + ".sign.location.z", event.getBlock().getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage("§2Successfully created arena sign.");
	        		}else{
	        			p.sendMessage("§2The arena appears to be invalid (missing components or misstyped arena)!");
	        			event.getBlock().breakNaturally();
	        		}
	        		event.setLine(1, "§5" +  arena);
	        		event.setLine(2, "§2[join]");
	        		event.setLine(3, "0/" + Integer.toString(this.maxplayers_perteam * 2));
	        	}
        	}
        }else if(event.getLine(0).toLowerCase().contains("[d-class]")){
        	if(event.getPlayer().hasPermission("thecore.sign")){
	        	event.setLine(0, "§2[D-Class]");
	        	event.getPlayer().sendMessage("§2You have successfully created a class sign for TheCore!");
        	}
        }
	}
	
	
	public boolean isEnabledArena(String arena){
		if(isValidArena(arena)){
			if(getConfig().getBoolean("arenas." + arena + ".enabled")){
				return true;
			}
		}
		return false;
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
	
	public Location getComponentFromArena(String arena, String component){
		if(isValidArena(arena)){
			String base = "arenas." + arena + "." + component;
			return new Location(Bukkit.getWorld(getConfig().getString(base + ".world")), getConfig().getInt(base + ".location.x"), getConfig().getInt(base + ".location.y"), getConfig().getInt(base + ".location.z"));
		}
		return null;
	}
	
	public Sign getSignFromArena(String arena){
		Location b_ = this.getComponentFromArena(arena, "sign");
    	BlockState bs = b_.getBlock().getState();
    	Sign s_ = null;
    	if(bs instanceof Sign){
    		s_ = (Sign)bs;
    	}else{
    		getLogger().info("Could not find sign: " + bs.getBlock().toString());
    	}
		return s_;
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

	
	
	public void spectateArena(final String player, String arena){
		if(isOnline(player)){
			Player p = getServer().getPlayer(player);
			
			if(pspectate.containsKey(p.getName())){
				pspectate.remove(p.getName());
				p.setFlying(false);
				p.setAllowFlight(false);
				final Location t = getComponentFromArena(arena, "lobby", "1");
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						getServer().getPlayer(player).teleport(t);
					}
				}, 10);
				
				for(final String player_ : arenap.keySet()){
					if(arenap.get(player_).equalsIgnoreCase(arena)){
						if(isOnline(player_)){
							getServer().getPlayer(player_).showPlayer(getServer().getPlayer(player));
						}
					}
				}
			}else{
				pspectate.put(p.getName(), true);
				p.setFlying(true);
				p.setAllowFlight(true);
				final Location t = getComponentFromArena(arena, "spawn", "1");
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						getServer().getPlayer(player).teleport(t);
					}
				}, 10);
				
				for(final String player_ : arenap.keySet()){
					if(arenap.get(player_).equalsIgnoreCase(arena)){
						if(isOnline(player_)){
							getServer().getPlayer(player_).hidePlayer(getServer().getPlayer(player));
						}
					}
				}
			}
		}
		
	}
	
	
	public void joinArena(final String player, final String arena){
		// disable pvp
		pvpenabled.put(player, false);
		
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
					Player p = getServer().getPlayer(player);
					p.teleport(t);
					p.setGameMode(GameMode.SURVIVAL);
					p.sendMessage("§3You are playing on map §2" + arena + "§3.");
				}
			}, 10);
		}
		
		this.setClass("default", player);
		this.giveClassesBook(player);
		
		Sign s = this.getSignFromArena(arena);
		s.setLine(3, Integer.toString(arenapcount.get(arena)) + "/" + Integer.toString(this.maxplayers_perteam * 2));
		s.update();
		
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
	
	public void giveClassesBook(String player){
		ItemStack b = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) b.getItemMeta();
		bm.setAuthor("TheCore");
		bm.setTitle("Classes");
		b.setItemMeta(bm);
		
		if(isOnline(player)){
			getServer().getPlayer(player).getInventory().addItem(b);
			getServer().getPlayer(player).updateInventory();
		}
	}
	
	public void startArena(final String arena){
		Sign s = this.getSignFromArena(arena);
		s.setLine(2, "§4[Ingame]");
		s.setLine(3, Integer.toString(arenapcount.get(arena)) + "/" + Integer.toString(this.maxplayers_perteam * 2));
		s.update();
		
		for(final String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(isOnline(player)){
					getServer().getPlayer(player).sendMessage("§3The game has started!");
					
					// enable pvp again
					pvpenabled.put(player, true);
					
					final Location t = this.getComponentFromArena(arena, "spawn", Integer.toString(pteam.get(player)));
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							getServer().getPlayer(player).teleport(t);
						}
					}, 10);
					
					getClass(player);
				}
			}
		}
		
		int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				ArenaDraw(arena);
			}
		}, getConfig().getInt("config.minutes_per_game") * 1200);
		
		taskid.put(arena, id);
		
		this.updateScoreboardTEAMS();
	}
	
	public void leaveArena(final String player, String arena){
		if(isOnline(player)){
			getServer().getPlayer(player).getInventory().clear();
			getServer().getPlayer(player).updateInventory();
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
		
		
		Sign s = this.getSignFromArena(arena);
		s.setLine(3, Integer.toString(arenapcount.get(arena)) + "/" + Integer.toString(this.maxplayers_perteam * 2));
		s.update();
		
		if(count < 2){
			resetArena(arena);
		}
	}
	
	public void resetArena(final String arena){
		boolean itemsreset = false;
		for(final String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(isOnline(player)){
					if(!itemsreset){
						for(Entity tt : getServer().getPlayer(player).getNearbyEntities(50, 50, 50)){
		    				if(tt.getType() != EntityType.ITEM_FRAME && tt.getType() != EntityType.PAINTING && tt.getType() != EntityType.PLAYER && tt.getType() != EntityType.BOAT && tt.getType() != EntityType.HORSE){
		    					if(tt.getPassenger() == null){
		    						tt.remove();
		    					}
		    				}
				    	}
						itemsreset = true;
					}
					final Location t = getComponentFromArena(arena, "lobby", "1");
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							getServer().getPlayer(player).teleport(t);
							getServer().getPlayer(player).playSound(getServer().getPlayer(player).getLocation(), Sound.LEVEL_UP, 1F, 1);
						}
					}, 10);
					pteam.remove(player);
					
					// reset class:
					pclass.remove(player);
					getServer().getPlayer(player).getInventory().clear();
					getServer().getPlayer(player).updateInventory();
					
					getServer().getPlayer(player).sendMessage("§4The game has ended!");
				}
			}
		}
		
		while (arenap.values().remove(arena));
		arenapcount.remove(arena);
		
		final ArrayList<ArenaBlock> ablocklist = new ArrayList<ArenaBlock>();
		
		Runnable r = new Runnable() {
	        public void run() {
	        	//loadArenaFromFileASYNC(arena);
	        	loadArenaFromFileSYNC(arena);
	        }
	    };
	    new Thread(r).start();
		
	    
		Sign s = this.getSignFromArena(arena);
		s.setLine(2, "§6[Restarting]");
		s.setLine(3, "0/" + Integer.toString(this.maxplayers_perteam * 2));
		s.update();
		
		// cancel draw task
		try{
			Bukkit.getServer().getScheduler().cancelTask(taskid.get(arena));
			taskid.remove(arena);
		}catch(Exception e){
			
		}
		
		this.updateScoreboardTEAMS();
	}
	
	public void ArenaDraw(String arena){ // if time runs out -> draw
		for(final String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(isOnline(player)){
					getServer().getPlayer(player).sendMessage("§4It's a draw, noone wins!");
				}
			}
		}
		
		resetArena(arena);
		getLogger().info("TEST3" + arena);
	}
	
	public void teamWin(String arena, int team){
		for(String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(pteam.get(player) == team){
					int nbef = Integer.parseInt(this.getStatsComponent(player, "teamwin")) + 1;
					this.saveStatsComponent(player, "teamwin", Integer.toString(nbef));
					if(getServer().getPlayer(player).isOnline()){
						getServer().getPlayer(player).sendMessage("§2Congratulations, you won this game!");
						if(getConfig().getBoolean("config.money_rewards")){
							EconomyResponse r = econ.depositPlayer(player, getConfig().getDouble("config.money_reward_per_game"));
	            			if(!r.transactionSuccess()) {
	            				getServer().getPlayer(player).sendMessage(String.format("An error occured: %s", r.errorMessage));
	                        }
						}else{
							//TODO: handle exception if material id is invalid
							Player p = getServer().getPlayer(player);
							try{
								p.getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.item_reward_id")), getConfig().getInt("config.item_reward_amount")));
							}catch(Exception e){
								getLogger().severe("Error while giving out the item reward! Is the item id valid?");
							}
							p.updateInventory();
						}
					}
				}
			}
		}
		//resetArena(arena);
	}
	
	public void teamLose(String arena, int team){
		for(String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(pteam.get(player) == team){
					int nbef = Integer.parseInt(this.getStatsComponent(player, "teamlose")) + 1;
					this.saveStatsComponent(player, "teamlose", Integer.toString(nbef));
					if(getServer().getPlayer(player).isOnline()){
						getServer().getPlayer(player).sendMessage("§2You lost this game!");
					}
				}
			}
		}
		// TODO: check that
		// arena already gets reset at teamWin function
		//resetArena(arena);
	}
	
	public void die(String player){
		// save in stats if you die
		int nbef = Integer.parseInt(this.getStatsComponent(player, "deaths")) + 1;
		this.saveStatsComponent(player, "deaths", Integer.toString(nbef));
		
		getClass(player);
	}
	
	public void kill(String player){
		// save in stats if you kill someone
		int nbef = Integer.parseInt(this.getStatsComponent(player, "kills")) + 1;
		this.saveStatsComponent(player, "kills", Integer.toString(nbef));
		
		if(getConfig().getBoolean("config.money_rewards")){
			EconomyResponse r = econ.depositPlayer(player, getConfig().getDouble("config.money_reward_per_kill"));
			if(!r.transactionSuccess()) {
				getServer().getPlayer(player).sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
		}
	}
	
	
	@Deprecated
	public void updateScoreboardBELOW_NAME(){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Scoreboard board2 = manager.getNewScoreboard();
		
		Objective objective = board.registerNewObjective("teamred", "dummy");
		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		objective.setDisplayName("§4TEAM");
		
		Objective objective2 = board2.registerNewObjective("teamblue", "dummy");
		objective2.setDisplaySlot(DisplaySlot.BELOW_NAME);
		objective2.setDisplayName("§1TEAM");
		 
		//for (Player online : Bukkit.getOnlinePlayers()) {
			//Score score = objective.getScore(online);
			//score.setScore(0);
		//}
		
		for(String player : arenap.keySet()){
			if(isOnline(player)){
				if(pteam.get(player) == 1){
					getServer().getPlayer(player).setScoreboard(board);
				}else{
					getServer().getPlayer(player).setScoreboard(board2);
				}
			}
		}
	}
	
	public void updateScoreboardTEAMS(){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		
		Team teamred = board.registerNewTeam("teamred");
		Team teamblue = board.registerNewTeam("teamblue");
		
		//teamred.setCanSeeFriendlyInvisibles(true);
		teamred.setAllowFriendlyFire(false);
		teamred.setPrefix("§3[TC] §4");
		teamred.setDisplayName("§3[TC] §4");
		//teamblue.setCanSeeFriendlyInvisibles(true);
		teamblue.setAllowFriendlyFire(false);
		teamblue.setPrefix("§3[TC] §1");
		teamblue.setDisplayName("§3[TC] §1");
		
		for(Player p : Bukkit.getOnlinePlayers()){
			if(arenap.containsKey(p.getName())){
				if(pteam.get(p.getName()) == 1){
					teamred.addPlayer(p);
				}else{
					teamblue.addPlayer(p);
				}
			}else{
				if(teamred.hasPlayer(p)){
					teamred.removePlayer(p);
				}
				if(teamblue.hasPlayer(p)){
					teamblue.removePlayer(p);
				}
			}
		}
	}
	
	public void updateScoreboardSIDEBAR(String arena, int time){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		
		Objective objective = board.registerNewObjective(arena, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("§3TheCore");
		
		
		Score score = objective.getScore(Bukkit.getOfflinePlayer("§aTime left: "));
		score.setScore(time);
		Score score2 = objective.getScore(Bukkit.getOfflinePlayer("§aArena: " + arena));
		
		//for (Player online : Bukkit.getOnlinePlayers()) {
			//Score score = objective.getScore(online);
			//score.setScore(0);
		//}
		
		for(String player : arenap.keySet()){
			if(arenap.get(player).equalsIgnoreCase(arena)){
				if(isOnline(player)){
					getServer().getPlayer(player).setScoreboard(board);
				}	
			}
		}
	}
	
	
	public void getClass(String player){
		AClass c = pclass.get(player);
		if(isOnline(player)){
			getServer().getPlayer(player).getInventory().clear();
			getServer().getPlayer(player).getInventory().setArmorContents(null);
			getServer().getPlayer(player).updateInventory();
			for(ItemStack i : c.items){
				getServer().getPlayer(player).getInventory().addItem(i);
			}
			getServer().getPlayer(player).updateInventory();
		}
	}
	
	public void setClass(String classname, String player){
		pclass.put(player, aclasses.get(classname));
	}
	
	public void loadClasses(){
		if(getConfig().isSet("classes")){
			for(String aclass : getConfig().getConfigurationSection("classes.").getKeys(false)){
				AClass n = new AClass(this, aclass, parseItems(getConfig().getString("classes." + aclass + ".items")));
				aclasses.put(aclass, n);
			}
		}
	}
	
	// example items: 267#1;3#64;3#64
	@SuppressWarnings("unused")
	public ArrayList<ItemStack> parseItems(String rawitems){
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		
		String[] a = rawitems.split(";");
		for(String b : a){
			String[] c = b.split("#");
			String itemid = c[0];
			String itemamount = c[1];
			ItemStack nitem = new ItemStack(Integer.parseInt(itemid), Integer.parseInt(itemamount));
			ret.add(nitem);
		}
		if(ret == null){
			getLogger().severe("Found invalid class in config!");
		}
		return ret;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(arenap.containsKey(event.getPlayer().getName())){
			Player p = event.getPlayer();
			
			// players can only build in the arena:
	    	Cuboid c = new Cuboid(this.getComponentFromArena(arenap.get(p.getName()), "boundary", "1"), this.getComponentFromArena(arenap.get(p.getName()), "boundary", "2"));
	    	if(!c.containsLoc(event.getBlock().getLocation())){
	    		event.setCancelled(true);
	    	}
	    	
			if(event.getBlock().getType().equals(Material.BEACON)){
				// if it's not the own team's beacon
				if(!compareTwoLocations(event.getBlock().getLocation(), this.getComponentFromArena(arenap.get(p.getName()), "beacon", Integer.toString(pteam.get(p.getName()))))){
					int teamint = pteam.get(p.getName());
					teamWin(arenap.get(p.getName()), teamint);
					if(teamint == 1){
						teamLose(arenap.get(p.getName()), 2);
					}else{
						teamLose(arenap.get(p.getName()), 1);
					}
					event.setCancelled(true);
				}else{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if(arenap.containsKey(event.getPlayer().getName())){
			Player p = event.getPlayer();
			
			// players can only build in the arena:
	    	Cuboid c = new Cuboid(this.getComponentFromArena(arenap.get(p.getName()), "boundary", "1"), this.getComponentFromArena(arenap.get(p.getName()), "boundary", "2"));
	    	if(!c.containsLoc(event.getBlock().getLocation())){
	    		event.setCancelled(true);
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
			Player p = event.getPlayer();
			Cuboid c = new Cuboid(this.getComponentFromArena(arenap.get(p.getName()), "boundary", "1"), this.getComponentFromArena(arenap.get(p.getName()), "boundary", "2"));
	    	
			// if y is still over lower level
			if(event.getTo().getBlockY() > c.getLowLoc().getBlockY()){
				// still not in arena?
		    	if(!c.containsLoc(event.getTo())){
		    		// player is out of the arena
		    		//TODO: arena boundaries
		    	}	
	    	}
			
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
    	if(event.getEntity() instanceof Player){
    		final Player p2 = event.getEntity();
    		if(arenap.containsKey(p2.getName())){
	    		if(event.getEntity().getKiller() != null){
	    			if(event.getEntity().getKiller() instanceof Player){
	    				final Player p1 = event.getEntity().getKiller();
	    				if(arenap.containsKey(p1.getName())){
		    				event.getEntity().setHealth(20);
							String arena = arenap.get(p1.getName());
							p2.playSound(p2.getLocation(), Sound.CAT_MEOW, 1F, 1);
			
							final Location t = this.getComponentFromArena(arena, "lobby", "2");
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								@Override
								public void run() {
									p2.teleport(t);
									//die(p2.getName());
									//kill(p1.getName());
								}
							}, 5);
							
							//TODO respawn timer: try out
							final Location t_ = this.getComponentFromArena(arena, "spawn", Integer.toString(pteam.get(p2.getName())));
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								@Override
								public void run() {
									p2.teleport(t_);
									die(p2.getName());
									kill(p1.getName());
								}
							}, getConfig().getLong("config.respawn_time_in_seconds") * 20);
							
							
							p1.sendMessage("§2You killed " + p2.getName() + "!");
							p2.sendMessage("§4You got killed by " + p1.getName() + "!");
							p2.sendMessage("§2Respawning in 5 seconds . . .");
							
							// global message in arena:
							for(String player : arenap.keySet()){
								if(arenap.get(player).equalsIgnoreCase(arena)){
									if(isOnline(player)){
										getServer().getPlayer(player).sendMessage("§4" + p2.getName() + "§3 was killed!");
									}
								}
							}
	    				}
	    			}
	    		}else{
	    			event.getEntity().setHealth(20);
					String arena = arenap.get(p2.getName());
					p2.playSound(p2.getLocation(), Sound.CAT_MEOW, 1F, 1);
	
					final Location t = this.getComponentFromArena(arena, "spawn", Integer.toString(pteam.get(p2.getName())));
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							p2.teleport(t);
							die(p2.getName());
						}
					}, 5);
	    		}	
    		}
    		
    	}
    }
	
    
    // disable starvation
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	if(event.getEntity() instanceof Player){
    		Player p = (Player)event.getEntity();
    		if(arenap.containsKey(p.getName())){
    			if(!pvpenabled.get(p.getName())){
    				event.setCancelled(true);
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onHunger(FoodLevelChangeEvent event){
    	if(event.getEntity() instanceof Player){
    		Player p = (Player)event.getEntity();
    		if(arenap.containsKey(p.getName())){
    			event.setCancelled(true);
    		}
    	}
    }
    
    
    @EventHandler
   	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
       	
    	if(event.getMessage().equalsIgnoreCase("/leave")){
    		if(arenap.containsKey(event.getPlayer().getName())){
    			Player p = event.getPlayer();
				if(arenap.containsKey(p.getName())){
					leaveArena(p.getName(), arenap.get(p.getName()));
				}else{
					p.sendMessage("§4You're not in an arena right now!");
				}
    			event.setCancelled(true);
    		}
    	}else if(event.getMessage().equalsIgnoreCase("/stats")){
    		Player p = event.getPlayer();
			String name = p.getName();
			
			p.sendMessage("§3TheCore statistics: ");
			p.sendMessage("§3Team Wins: §2" + this.getStatsComponent(name, "teamwin"));
			p.sendMessage("§3Team Loses: §4" + this.getStatsComponent(name, "teamlose"));
			p.sendMessage("§3Kills: §2" + this.getStatsComponent(name, "kills"));
			p.sendMessage("§3Deaths: §4" + this.getStatsComponent(name, "deaths"));
			event.setCancelled(true);
    	}
       	
    }
    
    
    public void saveArenaToFile(String player, String arena){
    	File f = new File(this.getDataFolder() + "/" + arena);
    	Cuboid c = new Cuboid(this.getComponentFromArena(arena, "boundary", "1"), this.getComponentFromArena(arena, "boundary", "2"));
    	Location start = c.getLowLoc();
    	Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();
		
		getLogger().info("BOUNDS: " + Integer.toString(width) + " " + Integer.toString(height) +  " " + Integer.toString(length)); 
		getLogger().info("BLOCKS TO SAVE: " + Integer.toString(width * height * length));
		
		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try{
			fos = new FileOutputStream(f);
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
		
		if(isOnline(player)){
			getServer().getPlayer(player).sendMessage("§2Successfully saved arena.");
		}
    }
    
    public void saveArenaToFile(String arena){
    	File f = new File(this.getDataFolder() + "/" + arena);
    	Cuboid c = new Cuboid(this.getComponentFromArena(arena, "boundary", "1"), this.getComponentFromArena(arena, "boundary", "2"));
    	Location start = c.getLowLoc();
    	Location end = c.getHighLoc();

		int width = end.getBlockX() - start.getBlockX();
		int length = end.getBlockZ() - start.getBlockZ();
		int height = end.getBlockY() - start.getBlockY();
		
		getLogger().info("BOUNDS: " + Integer.toString(width) + " " + Integer.toString(height) +  " " + Integer.toString(length)); 
		getLogger().info("BLOCKS TO SAVE: " + Integer.toString(width * height * length));
		
		FileOutputStream fos;
		ObjectOutputStream oos = null;
		try{
			fos = new FileOutputStream(f);
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
    
    public void loadArenaFromFileASYNC(String arena){
    	File f = new File(this.getDataFolder() + "/" + arena);
		FileInputStream fis = null;
		BukkitObjectInputStream ois = null;
		try {
			fis = new FileInputStream(f);
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
					getLogger().info("Finished restoring map for " + arena + ".");
					
					Sign s = this.getSignFromArena(arena);
					s.setLine(2, "§2[Join]");
					s.setLine(3, "0/" + Integer.toString(this.maxplayers_perteam * 2));
					s.update();
				}
				
				if(b != null){
					ArenaBlock ablock = (ArenaBlock) b;
					World w = ablock.getBlock().getWorld();

					if(!w.getBlockAt(ablock.getBlock().getLocation()).getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())){
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
		
    }
    
    
    public void loadArenaFromFileSYNC(String arena){
    	int failcount = 0;
    	final ArrayList<ArenaBlock> failedblocks = new ArrayList<ArenaBlock>();
    	
    	File f = new File(this.getDataFolder() + "/" + arena);
		FileInputStream fis = null;
		BukkitObjectInputStream ois = null;
		try {
			fis = new FileInputStream(f);
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
					getLogger().info("Finished restoring map for " + arena + ".");
					
					Sign s = this.getSignFromArena(arena);
					s.setLine(2, "§2[Join]");
					s.setLine(3, "0/" + Integer.toString(this.maxplayers_perteam * 2));
					s.update();
				}
				
				if(b != null){
					ArenaBlock ablock = (ArenaBlock) b;
					try{
						if(!ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation()).getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())){
							ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation()).setType(ablock.getMaterial());
						}
					}catch(IllegalStateException e){
						failcount += 1;
						failedblocks.add(ablock);
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

		getLogger().warning("Failed to update " + Integer.toString(failcount) + " blocks due to spigots async exception.");
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				// restore spigot blocks!
				getLogger().info("Trying to restore blocks affected by spigot exception..");
				for(ArenaBlock ablock : failedblocks){
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).setType(Material.WOOL);
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).getTypeId();
					getServer().getWorld(ablock.world).getBlockAt(new Location(getServer().getWorld(ablock.world), ablock.x, ablock.y, ablock.z)).setType(ablock.getMaterial());
				}
				getLogger().info("Successfully finished!");
			}
		}, 40L);
		
		return;
    }

}

