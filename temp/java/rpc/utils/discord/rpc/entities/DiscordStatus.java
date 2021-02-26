package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities;

public enum DiscordStatus {
    /**
     * Constant for the "disconnected" Discord Status.
     */
    Disconnected,

    /**
     * Constant for the "ready" Discord Status.
     */
    Ready,

    /**
     * Constant for the "joinGame" Discord Status
     * <p>Triggers when accepting and queuing a Join Request
     */
    JoinGame("Join Game"),

    /**
     * Constant for the "joinRequest" Discord Status
     * <p>Triggers when receiving a Join Request
     */
    JoinRequest("Join Request"),

    /**
     * Constant for the "spectateGame" Discord Status
     * <p>Triggers when queuing to spectate a game
     */
    SpectateGame("Spectate Game"),

    /**
     * 'Wildcard' build constant used to specify an errored or invalid status
     */
    Invalid;

    private final String displayName;

    DiscordStatus() {
        displayName = StringUtils.formatWord(name());
    }

    DiscordStatus(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets a {@link DiscordStatus} matching the specified display name.
     * <p>
     * This is only internally implemented.
     *
     * @param displayName The display name to get from.
     * @return The DiscordStatus corresponding to the display name, or
     * {@link DiscordStatus#Invalid} if none match.
     */
    public static DiscordStatus from(String displayName) {
        for (DiscordStatus value : values()) {
            if (value.getDisplayName() != null && value.getDisplayName().equals(displayName)) {
                return value;
            }
        }
        return Invalid;
    }

    public String getDisplayName() {
        return displayName;
    }
}
