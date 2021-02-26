package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.pipe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.IPCClient;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.Callback;
import com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.rpc.entities.Packet;

public class UnixPipe extends Pipe {
    private final AFUNIXSocket socket;

    UnixPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, String location) throws IOException {
        super(ipcClient, callbacks);

        socket = AFUNIXSocket.newInstance();
        socket.connect(new AFUNIXSocketAddress(new File(location)));
    }

    @Override
    @SuppressWarnings("BusyWait")
    public Packet read() throws IOException, JsonParseException {
        InputStream is = socket.getInputStream();

        while ((status == PipeStatus.CONNECTED || status == PipeStatus.CLOSING) && is.available() == 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }

        if (status == PipeStatus.DISCONNECTED)
            throw new IOException("Disconnected!");

        if (status == PipeStatus.CLOSED)
            return new Packet(Packet.OpCode.CLOSE, null, ipcClient.getEncoding());

        // Read the op and length. Both are signed ints
        byte[] d = new byte[8];
        int readResult = is.read(d);
        ByteBuffer bb = ByteBuffer.wrap(d);

        if (ipcClient.isDebugMode() && ipcClient.isVerboseLogging()) {
        	Reference.LOG.debugInfo(String.format("Read Byte Data: %s with result %s", new String(d), readResult));
        }

        Packet.OpCode op = Packet.OpCode.values()[Integer.reverseBytes(bb.getInt())];
        d = new byte[Integer.reverseBytes(bb.getInt())];

        int reversedResult = is.read(d);

        if (ipcClient.isDebugMode() && ipcClient.isVerboseLogging()) {
            Reference.LOG.debugInfo(String.format("Read Reversed Byte Data: %s with result %s", new String(d), reversedResult));
        }

        return receive(op, d);
    }

    @Override
    public void write(byte[] b) throws IOException {
        socket.getOutputStream().write(b);
    }

    @Override
    public void close() throws IOException {
        if (ipcClient.isDebugMode()) {
        	Reference.LOG.debugInfo("Closing IPC pipe...");
        }

        status = PipeStatus.CLOSING;
        send(Packet.OpCode.CLOSE, new JsonObject(), null);
        status = PipeStatus.CLOSED;
        socket.close();
    }

    public boolean mkdir(String path) {
        File file = new File(path);

        return file.exists() && file.isDirectory() || file.mkdir();
    }

    @Override
    public void registerApp(String applicationId, String command) {
        String home = System.getenv("HOME");

        if (home == null)
            throw new RuntimeException("Unable to find user HOME directory");

        if (command == null) {
            try {
                command = Files.readSymbolicLink(Paths.get("/proc/self/exe")).toString();
            } catch (Exception ex) {
                throw new RuntimeException("Unable to get current exe path from /proc/self/exe", ex);
            }
        }

        String desktopFile =
                "[Desktop Entry]\n" +
                        "Name=Game " + applicationId + "\n" +
                        "Exec=" + command + " %%u\n" +
                        "Type=Application\n" +
                        "NoDisplay=true\n" +
                        "Categories=Discord;Games;\n" +
                        "MimeType=x-scheme-handler/discord-" + applicationId + ";\n";

        String desktopFileName = "/discord-" + applicationId + ".desktop";
        String desktopFilePath = home + "/.local";

        if (this.mkdir(desktopFilePath))
        	Reference.LOG.debugWarn("Failed to create directory '" + desktopFilePath + "', may already exist");

        desktopFilePath += "/share";

        if (this.mkdir(desktopFilePath))
        	Reference.LOG.debugWarn("Failed to create directory '" + desktopFilePath + "', may already exist");

        desktopFilePath += "/applications";

        if (this.mkdir(desktopFilePath))
        	Reference.LOG.debugWarn("Failed to create directory '" + desktopFilePath + "', may already exist");

        desktopFilePath += desktopFileName;

        try (FileWriter fileWriter = new FileWriter(desktopFilePath)) {
            fileWriter.write(desktopFile);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to write desktop info into '" + desktopFilePath + "'");
        }

        String xdgMimeCommand = "xdg-mime default discord-" + applicationId + ".desktop x-scheme-handler/discord-" + applicationId;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(xdgMimeCommand.split(" "));
            processBuilder.environment();
            int result = processBuilder.start().waitFor();
            if (result < 0)
                throw new Exception("xdg-mime returned " + result);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to register mime handler", ex);
        }
    }

    @Override
    public void registerSteamGame(String applicationId, String steamId) {
        this.registerApp(applicationId, "xdg-open steam://rungameid/" + steamId);
    }
}
