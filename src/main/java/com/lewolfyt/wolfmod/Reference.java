package com.lewolfyt.wolfmod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
// import com.lewolfyt.wolfmod.client.modules.rpc.utils.FileUtils;
// import com.lewolfyt.wolfmod.client.modules.rpc.utils.StringUtils;
// import com.lewolfyt.wolfmod.client.modules.rpc.utils.TranslationUtils;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.realms.RealmsSharedConstants;

public class Reference {

	// Mod info
	public static final String MODID = "wolfmod";
	public static final String MODNAME = "WolfMod";
	public static final String VERSION = "1.0.1.0";
	public static final String VERSION_TYPE = "dev";
	//public static final String VERSION_ID;
	//public static final String VERSION_LABEL;
	public static final String MCVERSION = RealmsSharedConstants.VERSION_STRING;
	public static final int MC_PROTOCOL_ID = RealmsSharedConstants.NETWORK_PROTOCOL_VERSION;
	public static final int MOD_SCHEMA_VERSION = 1;
	public static final String BRAND = ClientBrandRetriever.getClientModName();
	
	// Classes
	public static final String CLIENT_PROXY_CLASS = "com.lewolfyt.wolfmod.server.proxy.ClientProxy";
	public static final String COMMON_PROXY_CLASS = "com.lewolfyt.wolfmod.server.proxy.CommonProxy";
	/*
	public static final TranslationUtils TRANSLATOR = new TranslationUtils(MODID, false);
	public static final ModLogger LOG = new ModLogger(MODID);
	public static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();
	public static final ModUpdaterUtils UPDATER;
	
	// Misc
	public static final String modsDir = WolfMod.SYSTEM.USER_DIR + File.separator + "mods";
	public static final String USERNAME = Minecraft.getMinecraft().getSession().getUsername();
    public static boolean IS_VERBOSE = (Launch.blackboard != null && !Launch.blackboard.isEmpty() && Launch.blackboard.containsKey("fml.deobfuscatedEnvironment")) && (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    public static boolean forceBlockTooltipRendering = false;
    public static boolean IS_DEV = false;
    
    // Web files
    public static final String UPDATE_JSON = "https://sidplayz.glitch.me/mods/wolfmod/update.json";
    
    // Code
	static {
        MODNAME = "@MOD_NAME@";
        VERSION_ID = "v@VERSION_ID@";
        VERSION_TYPE = "@VERSION_TYPE@";
        VERSION_LABEL = "@VERSION_LABEL@";
        UPDATER = new ModUpdaterUtils(MODID, UPDATE_JSON, VERSION_ID, MCVERSION);
    }
	
	public static void loadCharData(final boolean Update, final String encoding) {
        LOG.debugInfo(TRANSLATOR.translate("craftpresence.logger.info.chardata.init"));
        final String fileName = "chardata.properties", charDataPath = "/assets/" + MODID + "/" + fileName;
        final File charDataDir = new File(MODID + File.separator + fileName);
        boolean UpdateStatus = Update || !charDataDir.exists(), errored = false;
        InputStream inputData = null;
        InputStreamReader inputStream = null;
        OutputStream outputData = null;
        BufferedReader reader = null;

        if (UpdateStatus) {
            // Create Data Directory if non-existent
            if (!charDataDir.getParentFile().exists() && !charDataDir.getParentFile().mkdirs()) {
                errored = true;
            }

            LOG.debugInfo(TRANSLATOR.translate("craftpresence.logger.info.download.init", fileName, charDataDir.getAbsolutePath(), charDataPath));
            inputData = FileUtils.getResourceAsStream(Reference.class, charDataPath);

            // Write data from local charData to directory if an update is needed
            if (inputData != null) {
                try {
                    outputData = new FileOutputStream(charDataDir);

                    byte[] transferBuffer = new byte[inputData.available()];
                    for (int readBuffer = inputData.read(transferBuffer); readBuffer != -1; readBuffer = inputData.read(transferBuffer)) {
                        outputData.write(transferBuffer, 0, readBuffer);
                    }

                    LOG.debugInfo(TRANSLATOR.translate("wolfmod.logger.info.download.loaded", fileName, charDataDir.getAbsolutePath(), charDataPath));
                } catch (Exception ex) {
                    errored = true;
                }
            } else {
                errored = true;
            }
        }

        if (!errored) {
            try {
                inputData = new FileInputStream(charDataDir);
                inputStream = new InputStreamReader(inputData, Charset.forName(encoding));
                reader = new BufferedReader(inputStream);

                String currentString;
                while ((currentString = reader.readLine()) != null) {
                    String[] localWidths;
                    currentString = currentString.trim();

                    if (!currentString.startsWith("=") && currentString.contains("=")) {
                        String[] splitString = currentString.split("=", 2);

                        if (splitString[0].equalsIgnoreCase("charWidth")) {
                            localWidths = splitString[1].replaceAll("\\[", "").replaceAll("]", "").split(", ");

                            for (int i = 0; i < localWidths.length && i <= 256; i++) {
                                StringUtils.MC_CHAR_WIDTH[i] = Integer.parseInt(localWidths[i].trim());
                            }
                        } else if (splitString[0].equalsIgnoreCase("glyphWidth")) {
                            localWidths = splitString[1].replaceAll("\\[", "").replaceAll("]", "").split(", ");

                            for (int i = 0; i < localWidths.length && i <= 65536; i++) {
                                StringUtils.MC_GLYPH_WIDTH[i] = Byte.parseByte(localWidths[i].trim());
                            }
                        }
                    }
                }

                if (Arrays.equals(StringUtils.MC_CHAR_WIDTH, new int[256]) || Arrays.equals(StringUtils.MC_GLYPH_WIDTH, new byte[65536])) {
                    errored = true;
                }
            } catch (Exception ex) {
                loadCharData(true, "UTF-8");
            }
        }

        try {
            if (reader != null) {
                reader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (inputData != null) {
                inputData.close();
            }
            if (outputData != null) {
                outputData.close();
            }
        } catch (Exception ex) {
            LOG.debugError(TRANSLATOR.translate("craftpresence.logger.error.data.close"));
            if (IS_VERBOSE) {
                ex.printStackTrace();
            }
        } finally {
            if (errored) {
                LOG.debugError(TRANSLATOR.translate("craftpresence.logger.error.chardata"));
                forceBlockTooltipRendering = true;
            } else {
                LOG.debugInfo(TRANSLATOR.translate("craftpresence.logger.info.chardata.loaded"));
                forceBlockTooltipRendering = false;
            }
        }
    }

    public static void writeToCharData(final String encoding) {
        List<String> textData = Lists.newArrayList();
        InputStream inputData = null;
        InputStreamReader inputStream = null;
        OutputStream outputData = null;
        OutputStreamWriter outputStream = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        final File charDataDir = new File(MODID + File.separator + "chardata.properties");

        if (charDataDir.exists()) {
            try {
                // Read and Queue Character Data
                inputData = new FileInputStream(charDataDir);
                inputStream = new InputStreamReader(inputData, Charset.forName(encoding));
                br = new BufferedReader(inputStream);

                String currentString;
                while (!StringUtils.isNullOrEmpty((currentString = br.readLine()))) {
                    if (currentString.contains("=")) {
                        if (currentString.startsWith("charWidth")) {
                            textData.add("charWidth=" + Arrays.toString(StringUtils.MC_CHAR_WIDTH));
                        } else if (currentString.startsWith("glyphWidth")) {
                            textData.add("glyphWidth=" + Arrays.toString(StringUtils.MC_GLYPH_WIDTH));
                        }
                    }
                }

                // Write Queued Character Data
                outputData = new FileOutputStream(charDataDir);
                outputStream = new OutputStreamWriter(outputData, Charset.forName(encoding));
                bw = new BufferedWriter(outputStream);

                if (!textData.isEmpty()) {
                    for (String lineInput : textData) {
                        bw.write(lineInput);
                        bw.newLine();
                    }
                } else {
                    // If charWidth and glyphWidth don't exist, Reset Character Data
                    loadCharData(true, "UTF-8");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (bw != null) {
                        bw.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (inputData != null) {
                        inputData.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (outputData != null) {
                        outputData.close();
                    }
                } catch (Exception ex) {
                    LOG.debugError(TRANSLATOR.translate("wolfmod.logger.error.data.close"));
                    if (IS_VERBOSE) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            loadCharData(true, "UTF-8");
        }
    }*/


}
