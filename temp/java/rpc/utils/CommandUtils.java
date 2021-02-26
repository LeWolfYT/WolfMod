package com.lewolfyt.wolfmod.client.modules.rpc.utils;

import java.util.List;

import com.google.common.collect.Lists;
import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.Presence;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.assets.DiscordAssetUtils;

public class CommandUtils {
    /**
     * Whether you are on the Main Menu in Minecraft
     */
    public static boolean isInMainMenu = false;

    /**
     * Whether you are on the Loading Stage in Minecraft
     */
    public static boolean isLoadingGame = false;

    /**
     * Reloads and Synchronizes Data, as needed, and performs onTick Events
     *
     * @param forceUpdateRPC Whether to Force an Update to the RPC Data
     */
    public static void reloadData(final boolean forceUpdateRPC) {
        Reference.TRANSLATOR.onTick();
        Presence.SYSTEM.onTick();
        Presence.instance.addScheduledTask(() -> Presence.KEYBINDINGS.onTick());
        Presence.GUIS.onTick();

        if (Presence.SYSTEM.HAS_LOADED && Presence.SYSTEM.HAS_GAME_LOADED) {
        	Presence.BIOMES.onTick();
        	Presence.DIMENSIONS.onTick();
            Presence.TILE_ENTITIES.onTick();
            Presence.ENTITIES.onTick();
            Presence.SERVER.onTick();

            if (forceUpdateRPC) {
                if (Presence.DIMENSIONS.isInUse) {
                	Presence.DIMENSIONS.updateDimensionPresence();
                }
                if (Presence.GUIS.isInUse) {
                	Presence.GUIS.updateGUIPresence();
                }
                if (Presence.TILE_ENTITIES.isInUse) {
                	Presence.TILE_ENTITIES.updateEntityPresence();
                }
                if (Presence.ENTITIES.isInUse) {
                	Presence.ENTITIES.updateEntityPresence();
                }
                if (Presence.SERVER.isInUse) {
                	Presence.SERVER.updateServerPresence();
                }
                if (Presence.BIOMES.isInUse) {
                	Presence.BIOMES.updateBiomePresence();
                }
            }
        }
    }

    /**
     * Restarts and Initializes the RPC Data
     */
    public static void rebootRPC() {
    	Presence.CLIENT.shutDown();

        if (!Presence.CLIENT.CLIENT_ID.equals(Presence.CONFIG.clientId)) {
            DiscordAssetUtils.emptyData();
            Presence.CLIENT.CLIENT_ID = Presence.CONFIG.clientId;
        } else {
            DiscordAssetUtils.clearClientData();
        }
        DiscordAssetUtils.loadAssets(Presence.CONFIG.clientId, true);
        Presence.CLIENT.init(Presence.CONFIG.resetTimeOnInit);
    }

    /**
     * Initializes Essential Data<p>
     * (In this case, Pack Data and Available RPC Icons)
     */
    public static void init() {
        if (Presence.CONFIG.detectCurseManifest && !Presence.packFound) {
            CurseUtils.loadManifest();
        }
        if (Presence.CONFIG.detectMultiMCManifest && !Presence.packFound) {
            MultiMCUtils.loadInstance();
        }
        if (Presence.CONFIG.detectMCUpdaterInstance && !Presence.packFound) {
            MCUpdaterUtils.loadInstance();
        }
        if (Presence.CONFIG.detectTechnicPack && !Presence.packFound) {
            TechnicUtils.loadPack();
        }
        DiscordAssetUtils.loadAssets(Presence.CONFIG.clientId, true);
    }

    /**
     * Synchronizes RPC Data towards that of being in a Loading State
     */
    public static void setLoadingPresence() {
        // Form Argument Lists
        List<Pair<String, String>> loadingArgs = Lists.newArrayList();

        // Add All Generalized Arguments, if any
        if (!Presence.CLIENT.generalArgs.isEmpty()) {
            loadingArgs.addAll(Presence.CLIENT.generalArgs);
        }

        Presence.CLIENT.clearPartyData(true, false);

        Presence.CLIENT.syncArgument("&MAINMENU&", StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.loadingMessage, loadingArgs), false);
        Presence.CLIENT.syncArgument("&MAINMENU&", Presence.CLIENT.imageOf(Presence.CONFIG.defaultIcon, "", false), true);

        isLoadingGame = true;
    }

    /**
     * Synchronizes RPC Data towards that of being in the Main Menu
     */
    public static void setMainMenuPresence() {
        // Form Argument Lists
        List<Pair<String, String>> mainMenuArgs = Lists.newArrayList();

        // Add All Generalized Arguments, if any
        if (!Presence.CLIENT.generalArgs.isEmpty()) {
            mainMenuArgs.addAll(Presence.CLIENT.generalArgs);
        }

        // Clear Loading Game State, if applicable
        if (isLoadingGame) {
        	Presence.CLIENT.initArgument("&MAINMENU&");

            isLoadingGame = false;
        }

        Presence.CLIENT.syncArgument("&MAINMENU&", StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.mainMenuMessage, mainMenuArgs), false);
        Presence.CLIENT.syncArgument("&MAINMENU&", Presence.CLIENT.imageOf(Presence.CONFIG.defaultIcon, "", false), true);

        isInMainMenu = true;
    }
}

