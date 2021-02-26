package com.lewolfyt.wolfmod.server.proxy;

import com.lewolfyt.wolfmod.init.ModItems;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerRenders() {
		ModItems.registerRenders();
	}
}
