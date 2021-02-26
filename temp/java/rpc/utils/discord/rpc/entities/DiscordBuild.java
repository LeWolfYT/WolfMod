package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities;

public enum DiscordBuild {
    /**
     * Constant for the current Discord Canary release.
     */
    CANARY("//canary.discord.com/api"),

    /**
     * Constant for the current Discord Public Test Build or PTB release.
     */
    PTB("//ptb.discord.com/api"),

    /**
     * Constant for the current stable Discord release.
     */
    STABLE("//discord.com/api"),

    /**
     * 'Wildcard' build constant used in {@link com.gitlab.cdagaming.craftpresence.utils.discord.rpc.IPCClient#connect(DiscordBuild...)
     * IPCClient#connect(DiscordBuild...)} to signify that the build to target is not important, and
     * that the first valid build will be used.
     * <p>
     * Other than this exact function, there is no use for this value.
     */
    ANY;

    private final String endpoint;

    DiscordBuild(String endpoint) {
        this.endpoint = endpoint;
    }

    DiscordBuild() {
        this(null);
    }

    /**
     * Gets a {@link DiscordBuild} matching the specified endpoint.
     * <p>
     * This is only internally implemented.
     *
     * @param endpoint The endpoint to get from.
     * @return The DiscordBuild corresponding to the endpoint, or
     * {@link DiscordBuild#ANY} if none match.
     */
    public static DiscordBuild from(String endpoint) {
        for (DiscordBuild value : values()) {
            if (value.endpoint != null && value.endpoint.equals(endpoint)) {
                return value;
            }
        }
        return ANY;
    }
}
