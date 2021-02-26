package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.pipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCClient;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.Packet;

public class WindowsPipe extends Pipe {
    public RandomAccessFile file;

    WindowsPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, String location) {
        super(ipcClient, callbacks);
        try {
            this.file = new RandomAccessFile(location, "rw");
        } catch (FileNotFoundException e) {
            this.file = null;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b);
    }

    @Override
    @SuppressWarnings("BusyWait")
    public Packet read() throws IOException, JsonParseException {
        while ((status == PipeStatus.CONNECTED || status == PipeStatus.CLOSING) && file.length() == 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }

        if (status == PipeStatus.DISCONNECTED)
            throw new IOException("Disconnected!");

        if (status == PipeStatus.CLOSED)
            return new Packet(Packet.OpCode.CLOSE, null, ipcClient.getEncoding());

        Packet.OpCode op = Packet.OpCode.values()[Integer.reverseBytes(file.readInt())];
        int len = Integer.reverseBytes(file.readInt());
        byte[] d = new byte[len];

        file.readFully(d);

        return receive(op, d);
    }

    @Override
    public void close() throws IOException {
        if (ipcClient.isDebugMode()) {
            Reference.LOG.debugInfo("Closing IPC pipe...");
        }

        status = PipeStatus.CLOSING;
        send(Packet.OpCode.CLOSE, new JsonObject(), null);
        status = PipeStatus.CLOSED;
        file.close();
    }

    @Override
    public void registerApp(String applicationId, String command) {
        String javaLibraryPath = System.getProperty("java.home");
        File javaExeFile = new File(javaLibraryPath.split(";")[0] + "/bin/java.exe");
        File javawExeFile = new File(javaLibraryPath.split(";")[0] + "/bin/javaw.exe");
        String javaExePath = javaExeFile.exists() ? javaExeFile.getAbsolutePath() : javawExeFile.exists() ? javawExeFile.getAbsolutePath() : null;

        if (javaExePath == null)
            throw new RuntimeException("Unable to find java path");

        String openCommand;

        if (command != null)
            openCommand = command;
        else
            openCommand = javaExePath;

        String protocolName = "discord-" + applicationId;
        String protocolDescription = "URL:Run game " + applicationId + " protocol";
        String keyName = "Software\\Classes\\" + protocolName;
        String iconKeyName = keyName + "\\DefaultIcon";
        String commandKeyName = keyName + "\\DefaultIcon";

        try {
            WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, keyName);
            WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, keyName, "", protocolDescription);
            WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, keyName, "URL Protocol", "\0");

            WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, iconKeyName);
            WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, iconKeyName, "", javaExePath);

            WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, commandKeyName);
            WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, commandKeyName, "", openCommand);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to modify Discord registry keys", ex);
        }
    }

    @Override
    public void registerSteamGame(String applicationId, String steamId) {
        try {
            String steamPath = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Software\\\\Valve\\\\Steam", "SteamExe");
            if (steamPath == null)
                throw new RuntimeException("Steam exe path not found");

            steamPath = steamPath.replaceAll("/", "\\");

            String command = "\"" + steamPath + "\" steam://rungameid/" + steamId;

            this.registerApp(applicationId, command);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to register Steam game", ex);
        }
    }

}
