package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities;

public class Callback {
    private final DataConsumer<Packet> success;
    private final DataConsumer<String> failure;

    /**
     * Constructs an empty Callback.
     */
    public Callback() {
        this(null, null);
    }

    /**
     * Constructs a Callback with a success {@link DataConsumer} that
     * occurs when the process it is attached to executes without
     * error.
     *
     * @param success The Consumer to launch after a successful process.
     */
    public Callback(DataConsumer<Packet> success) {
        this(success, null);
    }

    /**
     * Constructs a Callback with a success {@link DataConsumer} <i>and</i>
     * a failure {@link DataConsumer} that occurs when the process it is
     * attached to executes without or with error (respectively).
     *
     * @param success The Consumer to launch after a successful process.
     * @param failure The Consumer to launch if the process has an error.
     */
    public Callback(DataConsumer<Packet> success, DataConsumer<String> failure) {
        this.success = success;
        this.failure = failure;
    }

    /**
     * Gets whether or not this Callback is "empty" which is more precisely
     * defined as not having a specified success {@link DataConsumer} and/or a
     * failure {@link DataConsumer}.<br>
     * This is only true if the Callback is constructed with the parameter-less
     * constructor ({@link #Callback()}) or another constructor that leaves
     * one or both parameters {@code null}.
     *
     * @return {@code true} if and only if the Callback is "empty"
     */
    public boolean isEmpty() {
        return success == null && failure == null;
    }

    /**
     * Launches the success {@link DataConsumer}.
     *
     * @param packet The packet to execute after success
     */
    public void succeed(Packet packet) {
        if (success != null)
            success.accept(packet);
    }

    /**
     * Launches the failure {@link DataConsumer} with the
     * provided message.
     *
     * @param message The message to launch the failure consumer with.
     */
    public void fail(String message) {
        if (failure != null)
            failure.accept(message);
    }
}
