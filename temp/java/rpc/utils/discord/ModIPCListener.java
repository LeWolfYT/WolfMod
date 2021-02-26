package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord;

import com.google.gson.JsonObject;
import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.Presence;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.StringUtils;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCClient;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCListener;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.DiscordStatus;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.Packet;

public class ModIPCListener implements IPCListener {
    @Override
    public void onActivityJoin(IPCClient client, String secret) {
        // On Accepting and Queuing a Join Request
        if (Presence.CLIENT.STATUS != DiscordStatus.JoinGame) {
        	Presence.CLIENT.STATUS = DiscordStatus.JoinGame;
        	Presence.SERVER.verifyAndJoin(secret);
        }
    }

    @Override
    public void onActivitySpectate(IPCClient client, String secret) {
        // Spectating Game, Unimplemented for now
        if (Presence.CLIENT.STATUS != DiscordStatus.SpectateGame) {
        	Presence.CLIENT.STATUS = DiscordStatus.SpectateGame;
        }
    }

    @Override
    public void onActivityJoinRequest(IPCClient client, String secret, User user) {
        // On Receiving a New Join Request
        if (Presence.CLIENT.STATUS != DiscordStatus.JoinRequest || !Presence.CLIENT.REQUESTER_USER.equals(user)) {
        	Presence.SYSTEM.TIMER = 30;
        	Presence.CLIENT.STATUS = DiscordStatus.JoinRequest;
        	Presence.CLIENT.REQUESTER_USER = user;

            if (!(Presence.instance.currentScreen instanceof CommandsGui)) {
            	Presence.GUIS.openScreen(new CommandsGui(Presence.instance.currentScreen));
            }
            CommandsGui.executeCommand("request");
        }
    }

    @Override
    public void onClose(IPCClient client, JsonObject json) {
        closeData(null);
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        closeData(t.getMessage());
    }

    @Override
    public void onPacketReceived(IPCClient client, Packet packet) {
        // N/A
    }

    @Override
    public void onPacketSent(IPCClient client, Packet packet) {
        // N/A
    }

    @Override
    public void onReady(IPCClient client) {
        if (Presence.CLIENT.STATUS != DiscordStatus.Ready) {
        	Presence.CLIENT.STATUS = DiscordStatus.Ready;
        	Presence.CLIENT.CURRENT_USER = client.getCurrentUser();
            Reference.LOG.info(Reference.TRANSLATOR.translate("wolfmod.modules.rpc.logger.info.load", Presence.CLIENT.CLIENT_ID, Presence.CLIENT.CURRENT_USER != null ? Presence.CLIENT.CURRENT_USER.getName() : "null"));
        }
    }

    private void closeData(final String disconnectMessage) {
        if (Presence.CLIENT.STATUS != DiscordStatus.Disconnected) {
            if (!StringUtils.isNullOrEmpty(disconnectMessage)) {
                Reference.LOG.error(Reference.TRANSLATOR.translate("wolfmod.modules.rpc.logger.error.rpc", disconnectMessage));
            }
            Presence.CLIENT.STATUS = DiscordStatus.Disconnected;
            Presence.CLIENT.shutDown();
        }
    }
}
