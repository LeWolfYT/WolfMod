package com.lewolfyt.wolfmod.client.modules.rpc.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lewolfyt.wolfmod.Reference;

public class UrlUtils {
    /**
     * The User Agent to Identify As when Accessing other URLs
     */
    private static final String USER_AGENT = Reference.MODID + "/" + Reference.MCVERSION;

    /**
     * The GSON Json Builder to Use while Parsing Json
     */
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Retrieve Output from a URL as a readable String
     *
     * @param url      The URL to Access
     * @param encoding The Charset Encoding to parse URL Contents in
     * @return The Output from the url as a String
     * @throws Exception If connection or Input is unable to be established
     */
    public static String getURLText(final URL url, final String encoding) throws Exception {
        return getString(getURLReader(url, encoding));
    }

    private static String getString(BufferedReader urlReader) throws Exception {
        final StringBuilder response = new StringBuilder();
        String inputLine;
        while (!StringUtils.isNullOrEmpty((inputLine = urlReader.readLine()))) {
            response.append(inputLine);
        }
        urlReader.close();
        return response.toString();
    }

    /**
     * Retrieve Output from a URL as a readable String
     *
     * @param url      The URL to Access
     * @param encoding The Charset Encoding to parse URL Contents in
     * @return The Output from the url as a String
     * @throws Exception If connection or Input is unable to be established
     */
    public static String getURLText(final String url, final String encoding) throws Exception {
        return getString(getURLReader(url, encoding));
    }

    /**
     * Retrieve a {@link BufferedReader} to read a response from a URL
     *
     * @param url      The URL to access (To be converted to a URL)
     * @param encoding The Charset Encoding to parse URL Contents in
     * @return a {@link BufferedReader} to read an output response from
     * @throws Exception If a connection is unable to be established
     */
    public static BufferedReader getURLReader(final String url, final String encoding) throws Exception {
        return getURLReader(new URL(url), encoding);
    }

    /**
     * Retrieve a {@link BufferedReader} to read a response from a URL
     *
     * @param url      The URL to access
     * @param encoding The Charset Encoding to parse URL Contents in
     * @return a {@link BufferedReader} to read an output response from
     * @throws Exception If a connection is unable to be established
     */
    public static BufferedReader getURLReader(final URL url, final String encoding) throws Exception {
        return new BufferedReader(getURLStreamReader(url, encoding));
    }

    /**
     * Retrieve an {@link InputStream} from a URL
     *
     * @param url The URL to access
     * @return an {@link InputStream} from the URL
     * @throws Exception If a connection is unable to be established
     */
    public static InputStream getURLStream(final URL url) throws Exception {
        final URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        return (connection.getInputStream());
    }

    /**
     * Retrieve an {@link InputStreamReader} from a URL
     *
     * @param url      The URL to access
     * @param encoding The Charset Encoding to parse URL Contents in
     * @return an {@link InputStreamReader} from the URL
     * @throws Exception If a connection is unable to be established
     */
    public static InputStreamReader getURLStreamReader(final URL url, final String encoding) throws Exception {
        return new InputStreamReader(getURLStream(url), Charset.forName(encoding));
    }

    /**
     * Converts a URLs Output into Formatted Json
     *
     * @param url         The URL to access (To be converted into a URL)
     * @param targetClass The target class to base parsing on
     * @param <T>         The data type for the resulting Json
     * @return The URL's Output, as Formatted Json
     * @throws Exception If a connection is unable to be established or parsing fails
     */
    public static <T> T getJSONFromURL(String url, Class<T> targetClass) throws Exception {
        return getJSONFromURL(new URL(url), targetClass);
    }

    /**
     * Converts a URLs Output into Formatted Json
     *
     * @param url         The URL to access
     * @param targetClass The target class to base parsing on
     * @param <T>         The data type for the resulting Json
     * @return The URL's Output, as Formatted Json
     * @throws Exception If a connection is unable to be established or parsing fails
     */
    public static <T> T getJSONFromURL(URL url, Class<T> targetClass) throws Exception {
        return GSON.fromJson(getURLStreamReader(url, "UTF-8"), targetClass);
    }

    /**
     * Opens the Specified Url in a Browser, if able
     *
     * @param targetUrl The URL to Open, as a String
     */
    public static void openUrl(final String targetUrl) {
        try {
            openUrl(new URI(targetUrl));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens the Specified Url in a Browser, if able
     *
     * @param targetUrl The URL to Open, as a URL
     */
    public static void openUrl(final URL targetUrl) {
        try {
            openUrl(targetUrl.toURI());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens the Specified Url in a Browser, if able
     *
     * @param targetUrl The URL to Open, as a URI
     */
    public static void openUrl(final URI targetUrl) {
        try {
            final Desktop desktop = Desktop.getDesktop();
            desktop.browse(targetUrl);
        } catch (Exception ex) {
            try {
                final Runtime runtime = Runtime.getRuntime();
                runtime.exec("xdg-open " + targetUrl.toString());
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }
}

