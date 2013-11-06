package com.comze_instancelabs.destroyerminigame;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

public class AClass {

	private Main m;
	public String name;
	public ArrayList<ItemStack> items = new ArrayList<ItemStack>();
	
	public AClass(Main m, String name, ArrayList<ItemStack> items){
		this.m = m;
		this.name = name;
		this.items = items;
	}
}
