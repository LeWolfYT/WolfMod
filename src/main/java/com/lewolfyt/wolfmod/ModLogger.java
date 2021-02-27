package com.lewolfyt.wolfmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// import com.lewolfyt.wolfmod.client.modules.rpc.utils.StringUtils;
import com.lewolfyt.wolfmod.WolfMod;

public class ModLogger {
    private final String loggerName;
    private final Logger logInstance;

    ModLogger(final String loggerName) {
        this.loggerName = loggerName;
        this.logInstance = LogManager.getLogger(loggerName);
    }

    /**
     * Sends a Message with an ERROR Level to either Chat or Logs
     *
     * @param logMessage   The Log Message to Send
     * @param logArguments Additional Formatting Arguments
     */
    /*public void error(final String logMessage, Object... logArguments) {
        if (WolfMod.player != null && !WolfMod.CONFIG.hasChanged && !WolfMod.closing && WolfMod.CONFIG.showLoggingInChat) {
            StringUtils.sendMessageToPlayer(WolfMod.player, "§6§l[§f§l" + loggerName + "§6]§r§c " + logMessage);
        } else {
            logInstance.error(logMessage, logArguments);
        }
    }

    /**
     * Sends a Message with an WARNING Level to either Chat or Logs
     *
     * @param logMessage   The Log Message to Send
     * @param logArguments Additional Formatting Arguments
     */
    /*public void warn(final String logMessage, Object... logArguments) {
        if (WolfMod.player != null && !WolfMod.CONFIG.hasChanged && !WolfMod.closing && WolfMod.CONFIG.showLoggingInChat) {
            StringUtils.sendMessageToPlayer(WolfMod.player, "§6§l[§f§l" + loggerName + "§6]§r§e " + logMessage);
        } else {
            logInstance.warn(logMessage, logArguments);
        }
    }*/

    /**
     * Sends a Message with an INFO Level to either Chat or Logs
     *
     * @param logMessage   The Log Message to Send
     * @param logArguments Additional Formatting Arguments
     */
    /*public void info(final String logMessage, Object... logArguments) {
        if (WolfMod.player != null && !WolfMod.CONFIG.hasChanged && !WolfMod.closing && WolfMod.CONFIG.showLoggingInChat) {
            StringUtils.sendMessageToPlayer(WolfMod.player, "§6§l[§f§l" + loggerName + "§6]§r " + logMessage);
        } else {
            logInstance.info(logMessage, logArguments);
        }
    }*/

    /**
     * Sends a Message with an INFO Level to either Chat or Logs, if in Debug Mode
     *
     * @param logMessage   The Log Message to Send
     * @param logArguments Additional Formatting Arguments
     */
    /*public void debugInfo(final String logMessage, Object... logArguments) {
        if (Reference.IS_DEV) {
            info("[Debug] " + logMessage, logArguments);
        }
    }*/

    /**
     * Sends a Message with an WARNING Level to either Chat or Logs, if in Debug Mode
     *
     * @param logMessage   The Log Message to Send
     * @param logArguments Additional Formatting Arguments
     */
    /*public void debugWarn(final String logMessage, Object... logArguments) {
        if (Reference.IS_DEV) {
            warn("[Debug] " + logMessage, logArguments);
        }
    }*/

    /**
     * Sends a Message with an ERROR Level to either Chat or Logs, if in Debug Mode
     *
     * @param logMessage   The Log Message to Send
     * @param logArguments Additional Formatting Arguments
     */
    /*public void debugError(final String logMessage, Object... logArguments) {
        if (Reference.IS_DEV) {
            error("[Debug] " + logMessage, logArguments);
        }
    }*/
}

