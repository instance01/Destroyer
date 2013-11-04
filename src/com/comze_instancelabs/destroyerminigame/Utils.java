package com.comze_instancelabs.destroyerminigame;

public class Utils {

	Main m = null;
	
	public Utils(Main p){
		m = p;
	}
	
	
	@Deprecated
	public boolean validArena(String arena){
		//TODO: finish
		if(m.getConfig().isSet("arenas." + arena) && m.getConfig().isSet("arenas." + arena + ".lobby1")){
			return true;
		}
		return false;
	}
	
}
