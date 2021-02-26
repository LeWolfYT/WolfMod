package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.pipe;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCClient;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.Callback;

public class MacPipe extends UnixPipe {

    MacPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, String location) throws IOException {
        super(ipcClient, callbacks, location);
    }

    private void registerCommand(String applicationId, String command) {
        String home = System.getenv("HOME");
        if (home == null)
            throw new RuntimeException("Unable to find user HOME directory");

        String path = home + "/Library/Application Support/discord";

        if (!this.mkdir(path))
            throw new RuntimeException("Failed to create directory '" + path + "'");

        path += "/games";

        if (!this.mkdir(path))
            throw new RuntimeException("Failed to create directory '" + path + "'");

        path += "/" + applicationId + ".json";

        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write("{\"command\": \"" + command + "\"}");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to write fame info into '" + path + "'");
        }
    }

    private void registerUrl(String applicationId) {
        throw new UnsupportedOperationException("MacOS URL registration is not supported at this time.");
    }

    @Override
    public void registerApp(String applicationId, String command) {
        try {
            if (command != null)
                this.registerCommand(applicationId, command);
            else
                this.registerUrl(applicationId);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to register " + (command == null ? "url" : "command"), ex);
        }
    }

    @Override
    public void registerSteamGame(String applicationId, String steamId) {
        this.registerApp(applicationId, "steam://rungameid/" + steamId);
    }
}
