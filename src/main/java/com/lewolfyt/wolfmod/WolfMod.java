package com.lewolfyt.wolfmod;

import java.util.Timer;

import com.lewolfyt.wolfmod.client.creativetabs.WolfTab;
import com.lewolfyt.wolfmod.init.ModItems;
import com.lewolfyt.wolfmod.server.proxy.CommonProxy;
import com.sidplayz.wolfmodutils.WolfModVars;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION + "-" + Reference.VERSION_TYPE)
public class WolfMod {

	public static boolean packFound = false;
	public static boolean closing = false;
	public static Timer timerObj = new Timer(WolfMod.class.getSimpleName());
	public static Minecraft instance = Minecraft.getMinecraft();
	// public static EntityPlayer player = instance.player;
	public static boolean isDevStatusOverridden = false;
    public static boolean isVerboseStatusOverridden = false;
    private boolean initialized = false;
    // public static ConfigUtils CONFIG;
    public static final WolfTab tabWolf = new WolfTab("tabWolf");
    public static final String REFERENCE_PATH = WolfModVars.REFERENCE_CLASS_PATH;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModItems.init();
		ModItems.register();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenders();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
}
