package com.lewolfyt.wolfmod.client.modules.rpc;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.CommandUtils;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.StringUtils;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.DiscordUtils;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCClient;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.DiscordStatus;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class Presence {
	public static boolean packFound = false;
	public static boolean closing = false;
	public static Timer timerObj = new Timer(Presence.class.getSimpleName());
	public static Minecraft instance = Minecraft.getMinecraft();
	public static EntityPlayer player = instance.thePlayer;
	public static ConfigUtils CONFIG;
	public static SystemUtils SYSTEM = new SystemUtils();
	public static KeyUtils KEYBINDINGS = new KeyUtils();
	public static DiscordUtils CLIENT = new DiscordUtils();
	public static ServerUtils SERVER = new ServerUtils();
	public static BiomeUtils BIOMES = new BiomeUtils();
	public static DimensionUtils DIMENSIONS = new DimensionUtils();
	public static EntityUtils ENTITIES = new EntityUtils();
	public static TileEntityUtils TILE_ENTITIES = new TileEntityUtils();
	public static GuiUtils GUIS = new GuiUtils();
	public static boolean isDevStatusOverridden = false;
	public static boolean isVerboseStatusOverridden = false;
	private boolean initialized = false;
	
	public Presence() {
		scheduleTick();
	}
	
	private void init() {
        // If running in Developer Mode, Warn of Possible Issues and Log OS Info
        Reference.LOG.debugWarn(Reference.TRANSLATOR.translate(true, "wolfmod.modules.rpc.logger.warning.debug_mode"));
        Reference.LOG.debugInfo(Reference.TRANSLATOR.translate(true, "wolfmod.modules.rpc.logger.info.os", SYSTEM.OS_NAME, SYSTEM.OS_ARCH, SYSTEM.IS_64_BIT));

        // Check for Updates before continuing
        Reference.UPDATER.checkForUpdates(() -> {
            if(Reference.UPDATER.isInvalidVersion) {
                // If the Updater found our version to be an invalid one
                // Then replace the Version ID, Name, and Type
                StringUtils.updateField(Reference.class, null, new Tuple<>("VERSION_ID", "v" + Reference.UPDATER.targetVersion, ~Modifier.FINAL));
                StringUtils.updateField(Reference.class, null, new Tuple<>("VERSION_TYPE", Reference.UPDATER.currentState.getDisplayName(), ~Modifier.FINAL));
                StringUtils.updateField(Reference.class, null, new Tuple<>("VERSION_LABEL", Reference.UPDATER.currentState.getDisplayName(), ~Modifier.FINAL));
                StringUtils.updateField(Reference.class, null, new Tuple<>("NAME", Reference.class.getSimpleName(), ~Modifier.FINAL));

                Reference.UPDATER.currentVersion = Reference.UPDATER.targetVersion;
                Reference.UPDATER.isInvalidVersion = false;
            }
        });

        SYSTEM = new SystemUtils();
        CONFIG = new ConfigUtils(Reference.configDir + File.separator + Reference.MODID + ".properties");
        CONFIG.initialize();

        final File CP_DIR = new File(ModUtils.MOD_ID);
        Reference.loadCharData(!CP_DIR.exists() || CP_DIR.listFiles() == null, "UTF-8");

        CommandUtils.init();

        // Synchronize Developer and Verbose Modes with Config Options, if they were not already true
        // If it is true (IE Modified from their Default Value), set the overridden flag to remember later
        if (!Reference.IS_DEV) {
        	Reference.IS_DEV = CONFIG.debugMode || Reference.IS_VERBOSE;
        } else {
            isDevStatusOverridden = true;
        }

        if (!Reference.IS_VERBOSE) {
        	Reference.IS_VERBOSE = CONFIG.verboseMode;
        } else {
            isVerboseStatusOverridden = true;
        }

        try {
            CLIENT.CLIENT_ID = CONFIG.clientId;
            CLIENT.AUTO_REGISTER = CONFIG.autoRegister;
            CLIENT.setup();
            CLIENT.init(true);
        } catch (Exception ex) {
        	Reference.LOG.error(Reference.TRANSLATOR.translate("wolfmod.modules.rpc.logger.error.load"));
            ex.printStackTrace();
        } finally {
            initialized = true;
        }
    }

    /**
     * Schedules the Next Tick to Occur if not currently closing
     */
    private void scheduleTick() {
        if (!closing) {
            timerObj.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            clientTick();
                        }
                    },
                    50
            );
        }
    }
    
    private void clientTick() {
        if (!closing) {
            if (initialized) {
                instance = Minecraft.getMinecraft();
                player = instance.thePlayer;

                // Synchronize Developer and Verbose Modes with Config Options, if they were not overridden pre-setup
                Reference.IS_DEV = !isDevStatusOverridden ? CONFIG.debugMode : Reference.IS_DEV;
                Reference.IS_VERBOSE = !isVerboseStatusOverridden ? CONFIG.verboseMode : Reference.IS_VERBOSE;

                CommandUtils.reloadData(false);

                if (!CONFIG.hasChanged) {
                    if (!SYSTEM.HAS_LOADED) {
                        // Ensure Loading Presence has already passed, before any other type of presence displays
                        CommandUtils.setLoadingPresence();
                    } else if (!CommandUtils.isInMainMenu && (!DIMENSIONS.isInUse && !BIOMES.isInUse && !TILE_ENTITIES.isInUse && !ENTITIES.isInUse && !SERVER.isInUse)) {
                        CommandUtils.setMainMenuPresence();
                    } else if (player != null && (CommandUtils.isLoadingGame || CommandUtils.isInMainMenu)) {
                        CommandUtils.isInMainMenu = false;
                        CommandUtils.isLoadingGame = false;
                        CLIENT.initArgument("&MAINMENU&");
                    }

                    if (SYSTEM.HAS_LOADED) {
                        if (CLIENT.awaitingReply && SYSTEM.TIMER == 0) {
                            StringUtils.sendMessageToPlayer(player, Reference.TRANSLATOR.translate("wolfmod.modules.rpc.command.request.ignored", CLIENT.REQUESTER_USER.getName()));
                            CLIENT.ipcInstance.respondToJoinRequest(CLIENT.REQUESTER_USER, IPCClient.ApprovalMode.DENY, null);
                            CLIENT.awaitingReply = false;
                            CLIENT.STATUS = DiscordStatus.Ready;
                        } else if (!CLIENT.awaitingReply && CLIENT.REQUESTER_USER != null) {
                            CLIENT.REQUESTER_USER = null;
                            CLIENT.STATUS = DiscordStatus.Ready;
                        }
                    }
                }
            } else {
                init();
            }

            scheduleTick();
        }
    }

}
