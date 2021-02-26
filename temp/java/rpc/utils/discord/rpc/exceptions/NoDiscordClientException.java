package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.exceptions;

public class NoDiscordClientException extends Exception {
    /**
     * The serialized unique version identifier
     */
    private static final long serialVersionUID = 1L;

    public NoDiscordClientException() {
        super("No Valid Discord Client was found for this Instance");
    }
}
