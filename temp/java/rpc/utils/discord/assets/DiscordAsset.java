package com.lewolfyt.wolfmod.client.modules.rpc.utils.discord.assets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DiscordAsset {
    /**
     * The {@link AssetType} of this Asset
     */
    @SerializedName("type")
    @Expose
    private AssetType type;

    /**
     * The Parsed ID for this Asset
     */
    @SerializedName("id")
    @Expose
    private String id;

    /**
     * The Parsed Name for this Asset
     */
    @SerializedName("name")
    @Expose
    private String name;

    /**
     * Retrieves the {@link AssetType} for this Asset
     *
     * @return The parsed {@link AssetType} for this Asset
     */
    public AssetType getType() {
        return type;
    }

    /**
     * Retrieves the Parsed ID for this Asset
     *
     * @return The Parsed ID for this Asset
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the Parsed Name for this Asset
     *
     * @return The Parsed Name for this Asset
     */
    public String getName() {
        return name;
    }

    /**
     * Prints Asset Data as a Readable String
     *
     * @return A readable version of this Asset
     */
    @Override
    public String toString() {
        return "DiscordAsset{" + "type=" + getType() + ", id='" + getId() + '\'' + ", name='" + getName() + '\'' + '}';
    }

    /**
     * A Mapping for the Parsed Asset Type for this Asset
     */
    public enum AssetType {
        @SerializedName("1")
        @Expose
        SMALL,
        @SerializedName("2")
        @Expose
        LARGE
    }
}
