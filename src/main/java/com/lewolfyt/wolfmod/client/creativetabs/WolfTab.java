package com.lewolfyt.wolfmod.client.creativetabs;

import com.lewolfyt.wolfmod.init.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class WolfTab extends CreativeTabs {

	public WolfTab(String label) {
		super(label);
		this.setBackgroundImageName("wolftab.png");
	}

	@Override
	public Item getTabIconItem() {
		return ModItems.test_item;
	}
	
}
