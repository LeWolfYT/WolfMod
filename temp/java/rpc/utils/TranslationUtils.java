package com.lewolfyt.wolfmod.client.modules.rpc.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.common.collect.Maps;
import com.lewolfyt.wolfmod.Reference;
import com.lewolfyt.wolfmod.client.modules.rpc.Presence;

public class TranslationUtils {
    /**
     * The Stored Mapping of Language Request History
     * <p>
     * Format: languageId:doesExist
     */
    private final Map<String, Boolean> requestMap = Maps.newHashMap();
    /**
     * Whether the Translations are utilizing Unicode Characters
     */
    public boolean isUnicode = false;
    /**
     * The Language ID to Locate and Retrieve Translations
     */
    private String languageId = Reference.MCProtocolID >= 315 ? "en_us" : "en_US";
    /**
     * The Target ID to locate the Language File
     */
    private String modId;
    /**
     * The Charset Encoding to parse translations in
     */
    private String encoding;
    /**
     * The Stored Mapping of Valid Translations
     * <p>
     * Format: unlocalizedKey:localizedString
     */
    private Map<String, String> translationMap = Maps.newHashMap();
    /**
     * If using a .Json or .Lang Language File
     */
    private boolean usingJson = false;

    /**
     * Sets initial Data and Retrieves Valid Translations
     */
    public TranslationUtils() {
        this(false);
    }

    /**
     * Sets initial Data and Retrieves Valid Translations
     *
     * @param useJson Toggles whether to use .Json or .Lang, if present
     */
    public TranslationUtils(final boolean useJson) {
        this("", useJson);
    }

    /**
     * Sets initial Data and Retrieves Valid Translations
     *
     * @param modId Sets the Target Mod ID to locate Language Files
     */
    public TranslationUtils(final String modId) {
        this(modId, false);
    }

    /**
     * Sets initial Data and Retrieves Valid Translations
     *
     * @param modId   Sets the Target Mod ID to locate Language Files
     * @param useJson Toggles whether to use .Json or .Lang, if present
     */
    public TranslationUtils(final String modId, final boolean useJson) {
        this(modId, useJson, "UTF-8");
    }

    /**
     * Sets initial Data and Retrieves Valid Translations
     *
     * @param modId    Sets the Target Mod ID to locate Language Files
     * @param useJson  Toggles whether to use .Json or .Lang, if present
     * @param encoding The Charset Encoding to parse Language Files
     */
    public TranslationUtils(final String modId, final boolean useJson, final String encoding) {
        setLanguage(Presence.CONFIG != null ? Presence.CONFIG.languageId : languageId);
        setModId(modId);
        setUsingJson(useJson);
        setEncoding(encoding);
        getTranslationMap(encoding);
        checkUnicode();
    }

    /**
     * Converts a Language Identifier using the Specified Conversion Mode, if possible
     * <p>
     * Note: If None is Used on a Valid Value, this function can be used as verification, if any
     *
     * @param originalId The original Key to Convert (5 Character Limit)
     * @param mode       The Conversion Mode to convert the keycode to
     * @return The resulting converted Language Identifier, or the mode's unknown key
     */
    public static String convertId(final String originalId, final ConversionMode mode) {
        String resultId = originalId;

        if (originalId.length() == 5 && originalId.contains("_")) {
            if (mode == ConversionMode.PackFormat2 || (mode == ConversionMode.None && ModUtils.MCProtocolID < 315)) {
                resultId = resultId.substring(0, 3).toLowerCase() + resultId.substring(3).toUpperCase();
            } else if (mode == ConversionMode.PackFormat3 || mode == ConversionMode.None) {
                resultId = resultId.toLowerCase();
            }
        }

        if (resultId.equals(originalId) && mode != ConversionMode.None) {
            Reference.LOG.debugWarn(Reference.TRANSLATOR.translate("wolfmod.modules.rpc.logger.warning.convert.invalid", resultId, mode.name()));
        }

        return resultId.trim();
    }

    /**
     * The Event to Run on each Client Tick, if passed initialization events
     * <p>
     * Comprises of Synchronizing Data, and Updating Translation Data as needed
     */
    void onTick() {
        if (Presence.CONFIG != null && !languageId.equals(Presence.CONFIG.languageId) &&
                (!requestMap.containsKey(Presence.CONFIG.languageId) || requestMap.get(Presence.CONFIG.languageId))) {
            setLanguage(Presence.CONFIG.languageId);
            getTranslationMap(encoding);
            checkUnicode();
        }

        if (Presence.instance.gameSettings != null && isUnicode != Presence.instance.gameSettings.forceUnicodeFont) {
            checkUnicode();
        }
    }

    /**
     * Determines whether the translations contain Unicode Characters
     */
    private void checkUnicode() {
        isUnicode = false;
        int extendedCharCount = 0;
        int totalLength = 0;

        for (String currentString : translationMap.values()) {
            final int currentLength = currentString.length();
            totalLength += currentLength;

            for (int index = 0; index < currentLength; ++index) {
                if (currentString.charAt(index) >= 256) {
                    ++extendedCharCount;
                }
            }
        }

        float f = (float) extendedCharCount / (float) totalLength;
        isUnicode = (double) f > 0.1D || (Presence.instance.gameSettings != null && Presence.instance.gameSettings.forceUnicodeFont);
    }

    /**
     * Toggles whether to use .Lang or .Json Language Files
     *
     * @param usingJson Toggles whether to use .Json or .Lang, if present
     */
    private void setUsingJson(final boolean usingJson) {
        this.usingJson = usingJson;
    }

    /**
     * Sets the Language ID to Retrieve Translations for, if present
     *
     * @param languageId The Language ID (Default: en_US)
     */
    private void setLanguage(final String languageId) {
        if (!StringUtils.isNullOrEmpty(languageId)) {
            this.languageId = languageId;
        } else {
            this.languageId = Reference.MCProtocolID >= 315 ? "en_us" : "en_US";
        }
    }

    /**
     * Sets the Charset Encoding to parse Translations in, if present
     *
     * @param encoding The Charset Encoding (Default: UTF-8)
     */
    private void setEncoding(final String encoding) {
        if (!StringUtils.isNullOrEmpty(encoding)) {
            this.encoding = encoding;
        } else {
            this.encoding = "UTF-8";
        }
    }

    /**
     * Sets the Mod ID to target when locating Language Files
     *
     * @param modId The Mod ID to target
     */
    private void setModId(final String modId) {
        if (!StringUtils.isNullOrEmpty(modId)) {
            this.modId = modId;
        } else {
            this.modId = null;
        }
    }

    /**
     * Retrieves and Synchronizes a List of Translations from a Language File
     */
    private void getTranslationMap(final String encoding) {
        translationMap = Maps.newHashMap();

        final InputStream in = FileUtils.getResourceAsStream(TranslationUtils.class, "/assets/"
                + (!StringUtils.isNullOrEmpty(modId) ? modId + "/" : "") +
                "lang/" + languageId + (usingJson ? ".json" : ".lang"));

        if (in != null) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(encoding)));
            try {
                String currentString;
                while ((currentString = reader.readLine()) != null) {
                    currentString = currentString.trim();
                    if (!currentString.startsWith("#") && !currentString.startsWith("[{}]") && (usingJson ? currentString.contains(":") : currentString.contains("="))) {
                        final String[] splitTranslation = usingJson ? currentString.split(":", 2) : currentString.split("=", 2);
                        if (usingJson) {
                            String str1 = splitTranslation[0].substring(1, splitTranslation[0].length() - 1).replace("\\n", "\n").replace("\\", "").trim();
                            String str2 = splitTranslation[1].substring(2, splitTranslation[1].length() - 2).replace("\\n", "\n").replace("\\", "").trim();
                            translationMap.put(str1, str2);
                        } else {
                            translationMap.put(splitTranslation[0].trim(), splitTranslation[1].trim());
                        }
                    }
                }

                in.close();
            } catch (Exception ex) {
            	Reference.LOG.error("An Exception has Occurred while Loading Translation Mappings, Things may not work well...");
                ex.printStackTrace();
            }
        } else {
        	Reference.LOG.error("Translations for " + modId + " do not exist for " + languageId);
            requestMap.put(languageId, false);
            setLanguage(Reference.MCProtocolID >= 315 ? "en_us" : "en_US");
        }
    }

    /**
     * Translates an Unlocalized String, based on the Translations retrieved
     *
     * @param stripColors    Whether to Remove Color and Formatting Codes
     * @param translationKey The unLocalized String to translate
     * @param parameters     Extra Formatting Arguments, if needed
     * @return The Localized Translated String
     */
    public String translate(final boolean stripColors, final String translationKey, final Object... parameters) {
        boolean hasError = false;
        String translatedString = translationKey;
        try {
            if (translationMap.containsKey(translationKey)) {
                translatedString = String.format(translationMap.get(translationKey), parameters);
            } else {
                hasError = true;
            }
        } catch (Exception ex) {
        	Reference.LOG.error("Exception Parsing " + translationKey);
            ex.printStackTrace();
            hasError = true;
        }

        if (hasError) {
        	Reference.LOG.error("Unable to retrieve a Translation for " + translationKey);
        }
        return stripColors ? StringUtils.stripColors(translatedString) : translatedString;
    }

    /**
     * Translates an Unlocalized String, based on the Translations retrieved
     *
     * @param translationKey The unLocalized String to translate
     * @param parameters     Extra Formatting Arguments, if needed
     * @return The Localized Translated String
     */
    public String translate(final String translationKey, final Object... parameters) {
        return translate(Presence.CONFIG != null && Presence.CONFIG.stripTranslationColors, translationKey, parameters);
    }

    /**
     * A Mapping storing the possible Conversion Modes for this module
     */
    public enum ConversionMode {
        PackFormat2, PackFormat3, None, Unknown
    }
}
