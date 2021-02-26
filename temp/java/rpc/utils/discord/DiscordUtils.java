package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord;

import java.util.List;

import com.google.common.collect.Lists;
import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.Presence;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.StringUtils;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCClient;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.DiscordStatus;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.PartyPrivacy;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.RichPresence;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.User;

public class DiscordUtils {
    /**
     * A Mapping of the Arguments available to use as RPC Message Placeholders
     */
    private final List<Pair<String, String>> messageData = Lists.newArrayList();
    /**
     * A Mapping of the Arguments available to use as Icon Key Placeholders
     */
    private final List<Pair<String, String>> iconData = Lists.newArrayList();
    /**
     * A Mapping of the Arguments attached to the &MODS& RPC Message placeholder
     */
    private final List<Pair<String, String>> modsArgs = Lists.newArrayList();
    /**
     * A Mapping of the Arguments attached to the &IGN& RPC Message Placeholder
     */
    private final List<Pair<String, String>> playerInfoArgs = Lists.newArrayList();
    /**
     * The Current User, tied to the Rich Presence
     */
    public User CURRENT_USER;
    /**
     * The Join Request User Data, if any
     */
    public User REQUESTER_USER;
    /**
     * The current RPC Status (Ex: ready, errored, disconnected)
     */
    public DiscordStatus STATUS = DiscordStatus.Disconnected;
    /**
     * The Current Message tied to the Party/Game Status Field of the RPC
     */
    public String GAME_STATE;
    /**
     * The Current Message tied to the current action / Details Field of the RPC
     */
    public String DETAILS;
    /**
     * The Current Small Image Icon being displayed in the RPC, if any
     */
    public String SMALL_IMAGE_KEY;
    /**
     * The Current Message tied to the Small Image, if any
     */
    public String SMALL_IMAGE_TEXT;
    /**
     * The Current Large Image Icon being displayed in the RPC, if any
     */
    public String LARGE_IMAGE_KEY;
    /**
     * The Current Message tied to the Large Image, if any
     */
    public String LARGE_IMAGE_TEXT;
    /**
     * The 18-character Client ID Number, tied to the game profile data attached to the RPC
     */
    public String CLIENT_ID;
    /**
     * Whether to register this application as an application with discord
     */
    public boolean AUTO_REGISTER;
    /**
     * The Current Starting Unix Timestamp from Epoch, used for Elapsed Time
     */
    public long START_TIMESTAMP;
    /**
     * The Party Session ID that's tied to the RPC, if any
     */
    public String PARTY_ID;
    /**
     * The Current Size of the Party Session, if in a Party
     */
    public int PARTY_SIZE;
    /**
     * The Maximum Size of the Party Session, if in a Party
     */
    public int PARTY_MAX;
    /**
     * The Privacy Level of the Party Session
     * <p>0 == Private; 1 == Public
     */
    public PartyPrivacy PARTY_PRIVACY = PartyPrivacy.Public;
    /**
     * The Current Party Join Secret Key, if in a Party
     */
    public String JOIN_SECRET;
    /**
     * The Current Ending Unix Timestamp from Epoch, used for Time Until if combined with {@link DiscordUtils#START_TIMESTAMP}
     */
    public long END_TIMESTAMP;
    /**
     * The Current Match Secret Key tied to the RPC, if any
     */
    public String MATCH_SECRET;
    /**
     * The Current Spectate Secret Key tied to the RPC, if any
     */
    public String SPECTATE_SECRET;
    /**
     * The Instance Code attached to the RPC, if any
     */
    public byte INSTANCE;
    /**
     * A Mapping of the General RPC Arguments allowed in adjusting Presence Messages
     */
    public List<Pair<String, String>> generalArgs = Lists.newArrayList();
    /**
     * An Instance of the {@link IPCClient}, responsible for sending and receiving RPC Events
     */
    public IPCClient ipcInstance;
    /**
     * Whether Discord is currently awaiting a response to a Ask to Join or Spectate Request, if any
     */
    public boolean awaitingReply = false;
    /**
     * A Mapping of the Last Requested Image Data
     * <p>Used to prevent sending duplicate packets and cache data for repeated images in other areas
     * <p>Format: lastAttemptedKey, lastResultingKey
     */
    private Pair<String, String> lastRequestedImageData = new Pair<>();
    /**
     * An Instance containing the Current Rich Presence Data
     * <p>Also used to prevent sending duplicate packets with the same presence data, if any
     */
    private RichPresence currentPresence;

    /**
     * Setup any Critical Methods needed for the RPC
     * <p>In this case, ensures a Thread is in place to shut down the RPC onExit
     */
    public synchronized void setup() {
        final Thread shutdownThread = new Thread("CraftPresence-ShutDown-Handler") {
            @Override
            public void run() {
                Presence.closing = true;
                Presence.timerObj.cancel();

                shutDown();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    /**
     * Initializes and Synchronizes Initial Rich Presence Data
     *
     * @param updateTimestamp Whether or not to update the starting timestamp
     */
    public synchronized void init(final boolean updateTimestamp) {
        try {
            // Update Start Timestamp onInit, if needed
            if (updateTimestamp) {
                updateTimestamp();
            }

            // Create IPC Instance and Listener and Make a Connection if possible
            ipcInstance = new IPCClient(Long.parseLong(CLIENT_ID), Reference.IS_DEV, Reference.IS_VERBOSE, AUTO_REGISTER, CLIENT_ID);
            ipcInstance.setListener(new ModIPCListener());
            ipcInstance.connect();

            // Subscribe to RPC Events after Connection
            ipcInstance.subscribe(IPCClient.Event.ACTIVITY_JOIN);
            ipcInstance.subscribe(IPCClient.Event.ACTIVITY_JOIN_REQUEST);
            ipcInstance.subscribe(IPCClient.Event.ACTIVITY_SPECTATE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Initialize and Sync any Pre-made Arguments (And Reset Related Data)
        initArgument(false, "&MAINMENU&", "&BRAND&", "&MCVERSION&", "&IGN&", "&MODS&", "&PACK&", "&DIMENSION&", "&BIOME&", "&SERVER&", "&SCREEN&", "&TILEENTITY&", "&TARGETENTITY&", "&ATTACKINGENTITY&", "&RIDINGENTITY&");
        initArgument(true, "&DEFAULT&", "&MAINMENU&", "&PACK&", "&DIMENSION&", "&SERVER&");

        // Ensure Main Menu RPC Resets properly
        CommandUtils.isInMainMenu = false;

        // Add Any Generalized Argument Data needed
        modsArgs.add(new Pair<>("&MODCOUNT&", Integer.toString(FileUtils.getModCount())));
        playerInfoArgs.add(new Pair<>("&NAME&", Reference.USERNAME));

        generalArgs.add(new Pair<>("&MCVERSION&", Reference.TRANSLATOR.translate("craftpresence.defaults.state.mc.version", Reference.MCVERSION)));
        generalArgs.add(new Pair<>("&BRAND&", Reference.BRAND));
        generalArgs.add(new Pair<>("&MODS&", StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.modsPlaceholderMessage, modsArgs)));
        generalArgs.add(new Pair<>("&IGN&", StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.outerPlayerPlaceholderMessage, playerInfoArgs)));

        for (Pair<String, String> generalArgument : generalArgs) {
            // For each General (Can be used Anywhere) Argument
            // Ensure they sync as Formatter Arguments too
            syncArgument(generalArgument.getFirst(), generalArgument.getSecond(), false);
        }

        // Sync the Default Icon Argument
        syncArgument("&DEFAULT&", Presence.CONFIG.defaultIcon, true);

        syncPackArguments();
    }

    /**
     * Updates the Starting Unix Timestamp, if allowed
     */
    public void updateTimestamp() {
        if (Presence.CONFIG.showTime) {
            START_TIMESTAMP = Presence.SYSTEM.CURRENT_TIMESTAMP / 1000L;
        }
    }

    /**
     * Synchronizes the Specified Argument as an RPC Message or an Icon Placeholder
     *
     * @param argumentName The Specified Argument to Synchronize for
     * @param insertString The String to attach to the Specified Argument
     * @param isIconData   Whether the Argument is an RPC Message or an Icon Placeholder
     */
    public void syncArgument(String argumentName, String insertString, boolean isIconData) {
        // Remove and Replace Placeholder Data, if the placeholder needs Updates
        if (!StringUtils.isNullOrEmpty(argumentName)) {
            if (isIconData) {
                synchronized (iconData) {
                    if (iconData.removeIf(e -> e.getFirst().equalsIgnoreCase(argumentName) && !e.getSecond().equalsIgnoreCase(insertString))) {
                        iconData.add(new Pair<>(argumentName, insertString));
                    }
                }
            } else {
                synchronized (messageData) {
                    if (messageData.removeIf(e -> e.getFirst().equalsIgnoreCase(argumentName) && !e.getSecond().equalsIgnoreCase(insertString))) {
                        messageData.add(new Pair<>(argumentName, insertString));
                    }
                }
            }
        }
    }

    /**
     * Initialize the Specified Arguments as Empty Data
     *
     * @param isIconData Whether the Argument is an RPC Message or an Icon Placeholder
     * @param args       The Arguments to Initialize
     */
    public void initArgument(boolean isIconData, String... args) {
        // Initialize Specified Arguments to Empty Data
        if (isIconData) {
            for (String argumentName : args) {
                synchronized (iconData) {
                    iconData.removeIf(e -> e.getFirst().equalsIgnoreCase(argumentName));
                    iconData.add(new Pair<>(argumentName, ""));
                }
            }
        } else {
            for (String argumentName : args) {
                synchronized (messageData) {
                    messageData.removeIf(e -> e.getFirst().equalsIgnoreCase(argumentName));
                    messageData.add(new Pair<>(argumentName, ""));
                }
            }
        }
    }

    /**
     * Initialize the Specified Arguments in all regards
     *
     * @param args The Arguments to Initialize as Empty Data for both Icons and RPC Messages
     */
    public void initArgument(String... args) {
        initArgument(false, args);
        initArgument(true, args);
    }

    /**
     * Synchronizes the &PACK& Argument, based on any found Launcher Pack/Instance Data
     */
    private void syncPackArguments() {
        // Add &PACK& Placeholder to ArgumentData
        String foundPackName = "", foundPackIcon = "";

        if (Reference.BRAND.contains("vivecraft")) {
            Presence.packFound = true;

            foundPackName = Presence.CONFIG.vivecraftMessage;
            foundPackIcon = "vivecraft";
        } else if (!StringUtils.isNullOrEmpty(CurseUtils.INSTANCE_NAME)) {
            foundPackName = CurseUtils.INSTANCE_NAME;
            foundPackIcon = foundPackName;
        } else if (!StringUtils.isNullOrEmpty(MultiMCUtils.INSTANCE_NAME)) {
            foundPackName = MultiMCUtils.INSTANCE_NAME;
            foundPackIcon = MultiMCUtils.ICON_KEY;
        } else if (MCUpdaterUtils.instance != null && !StringUtils.isNullOrEmpty(MCUpdaterUtils.instance.getPackName())) {
            foundPackName = MCUpdaterUtils.instance.getPackName();
            foundPackIcon = foundPackName;
        } else if (!StringUtils.isNullOrEmpty(TechnicUtils.PACK_NAME)) {
            foundPackName = TechnicUtils.PACK_NAME;
            foundPackIcon = TechnicUtils.ICON_NAME;
        } else if (!StringUtils.isNullOrEmpty(Presence.CONFIG.fallbackPackPlaceholderMessage)) {
            foundPackName = Presence.CONFIG.fallbackPackPlaceholderMessage;
            foundPackIcon = foundPackName;
        }

        syncArgument("&PACK&", StringUtils.formatWord(StringUtils.replaceAnyCase(Presence.CONFIG.packPlaceholderMessage, "&NAME&", !StringUtils.isNullOrEmpty(foundPackName) ? foundPackName : ""), !Presence.CONFIG.formatWords), false);
        syncArgument("&PACK&", !StringUtils.isNullOrEmpty(foundPackIcon) ? StringUtils.formatAsIcon(foundPackIcon) : "", true);
    }

    /**
     * Synchronizes and Updates the Rich Presence Data, if needed and connected
     *
     * @param presence The New Presence Data to apply
     */
    public void updatePresence(final RichPresence presence) {
        if (presence != null &&
                (currentPresence == null || !presence.toJson().toString().equals(currentPresence.toJson().toString())) &&
                ipcInstance.getStatus() == PipeStatus.CONNECTED) {
            ipcInstance.sendRichPresence(presence);
            currentPresence = presence;
        }
    }

    /**
     * Attempts to lookup the specified Image, and if not existent, use the alternative String, and null if allowed
     *
     * @param evalString        The Specified Icon Key to search for from the {@link DiscordUtils#CLIENT_ID} Assets
     * @param alternativeString The Alternative Icon Key to use if unable to locate the Original Icon Key
     * @param allowNull         If allowed to return null if unable to find any matches, otherwise uses the Default Icon in Config
     * @return The found or alternative matching Icon Key
     */
    public String imageOf(final String evalString, final String alternativeString, final boolean allowNull) {
        // Ensures Assets were fully synced from the Client ID before running
        if (DiscordAssetUtils.syncCompleted) {
            if (StringUtils.isNullOrEmpty(lastRequestedImageData.getFirst()) || !lastRequestedImageData.getFirst().equalsIgnoreCase(evalString)) {
                final String defaultIcon = DiscordAssetUtils.contains(Presence.CONFIG.defaultIcon) ? Presence.CONFIG.defaultIcon : DiscordAssetUtils.getRandomAsset();
                lastRequestedImageData.setFirst(evalString);

                String finalKey = evalString;

                if (!DiscordAssetUtils.contains(finalKey)) {
                    ModUtils.LOG.error(ModUtils.TRANSLATOR.translate(true, "wolfmod.modules.rpc.logger.error.discord.assets.fallback", evalString, alternativeString));
                    ModUtils.LOG.info(ModUtils.TRANSLATOR.translate(true, "wolfmod.modules.rpc.logger.info.discord.assets.request", evalString));
                    if (DiscordAssetUtils.contains(alternativeString)) {
                        ModUtils.LOG.info(ModUtils.TRANSLATOR.translate(true, "wolfmod.modules.rpc.logger.info.discord.assets.fallback", evalString, alternativeString));
                        finalKey = alternativeString;
                    } else {
                        if (allowNull) {
                            finalKey = "";
                        } else {
                            ModUtils.LOG.info(ModUtils.TRANSLATOR.translate(true, "wolfmod.modules.rpc.logger.error.discord.assets.default", evalString, defaultIcon));
                            finalKey = defaultIcon;
                        }
                    }
                }

                lastRequestedImageData.setSecond(finalKey);
                return finalKey;
            } else {
                return lastRequestedImageData.getSecond();
            }
        } else {
            return "";
        }
    }

    /**
     * Clears Related Party Session Information from the RPC, and updates if needed
     *
     * @param clearRequesterData Whether to clear Ask to Join / Spectate Request Data
     * @param updateRPC          Whether to immediately update the RPC following changes
     */
    public void clearPartyData(boolean clearRequesterData, boolean updateRPC) {
        if (clearRequesterData) {
            awaitingReply = false;
            REQUESTER_USER = null;
            Presence.SYSTEM.TIMER = 0;
        }
        JOIN_SECRET = null;
        PARTY_ID = null;
        PARTY_SIZE = 0;
        PARTY_MAX = 0;
        if (updateRPC) {
            updatePresence(buildRichPresence());
        }
    }

    /**
     * Clears Presence Data from the RPC, and updates if needed
     *
     * @param partyClearArgs Arguments for {@link DiscordUtils#clearPartyData(boolean, boolean)}
     */
    public void clearPresenceData(Tuple<Boolean, Boolean, Boolean> partyClearArgs) {
        GAME_STATE = "";
        DETAILS = "";
        LARGE_IMAGE_KEY = "";
        LARGE_IMAGE_TEXT = "";
        SMALL_IMAGE_KEY = "";
        SMALL_IMAGE_TEXT = "";

        if (partyClearArgs.getFirst()) {
            clearPartyData(partyClearArgs.getSecond(), partyClearArgs.getThird());
        }
    }

    /**
     * Shutdown the RPC and close related resources, as well as Clearing any remaining Runtime Client Data
     */
    public synchronized void shutDown() {
        if (Presence.SYSTEM.HAS_LOADED) {
            try {
                ipcInstance.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Clear User Data before final clear and shutdown
            STATUS = DiscordStatus.Disconnected;
            currentPresence = null;
            // Empty RPC Data
            clearPresenceData(new Tuple<>(true, true, false));

            CURRENT_USER = null;
            lastRequestedImageData = new Pair<>();

            Presence.DIMENSIONS.clearClientData();
            Presence.TILE_ENTITIES.clearClientData();
            Presence.ENTITIES.clearClientData();
            Presence.BIOMES.clearClientData();
            Presence.SERVER.clearClientData();
            Presence.GUIS.clearClientData();

            Presence.SYSTEM.HAS_LOADED = false;
            Reference.LOG.info(Reference.TRANSLATOR.translate("wolfmod.modules.rpc.logger.info.shutdown"));
        }
    }

    /**
     * Builds a New Instance of {@link RichPresence} based on Queued Data
     *
     * @return A New Instance of {@link RichPresence}
     */
    public RichPresence buildRichPresence() {
        // Format Presence based on Arguments available in argumentData
        DETAILS = StringUtils.formatWord(StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.detailsMessage, messageData), !Presence.CONFIG.formatWords, true, 1);
        GAME_STATE = StringUtils.formatWord(StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.gameStateMessage, messageData), !Presence.CONFIG.formatWords, true, 1);

        LARGE_IMAGE_KEY = StringUtils.formatAsIcon(StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.largeImageKey, iconData));
        SMALL_IMAGE_KEY = StringUtils.formatAsIcon(StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.smallImageKey, iconData));

        LARGE_IMAGE_TEXT = StringUtils.formatWord(StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.largeImageMessage, messageData), !Presence.CONFIG.formatWords, true, 1);
        SMALL_IMAGE_TEXT = StringUtils.formatWord(StringUtils.sequentialReplaceAnyCase(Presence.CONFIG.smallImageMessage, messageData), !Presence.CONFIG.formatWords, true, 1);

        final RichPresence newRPCData = new RichPresence.Builder()
                .setState(GAME_STATE)
                .setDetails(DETAILS)
                .setStartTimestamp(START_TIMESTAMP)
                .setEndTimestamp(END_TIMESTAMP)
                .setLargeImage(LARGE_IMAGE_KEY, LARGE_IMAGE_TEXT)
                .setSmallImage(SMALL_IMAGE_KEY, SMALL_IMAGE_TEXT)
                .setParty(PARTY_ID, PARTY_SIZE, PARTY_MAX, PARTY_PRIVACY.getPartyIndex())
                .setMatchSecret(MATCH_SECRET)
                .setJoinSecret(JOIN_SECRET)
                .setSpectateSecret(SPECTATE_SECRET)
                .build();

        // Format Data to UTF_8 after Sent to RPC (RPC has it's own Encoding)
        GAME_STATE = StringUtils.getConvertedString(GAME_STATE, "UTF-8", false);
        DETAILS = StringUtils.getConvertedString(DETAILS, "UTF-8", false);

        LARGE_IMAGE_KEY = StringUtils.getConvertedString(LARGE_IMAGE_KEY, "UTF-8", false);
        SMALL_IMAGE_KEY = StringUtils.getConvertedString(SMALL_IMAGE_KEY, "UTF-8", false);

        LARGE_IMAGE_TEXT = StringUtils.getConvertedString(LARGE_IMAGE_TEXT, "UTF-8", false);
        SMALL_IMAGE_TEXT = StringUtils.getConvertedString(SMALL_IMAGE_TEXT, "UTF-8", false);

        return newRPCData;
    }
}
