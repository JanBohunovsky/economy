package dev.bohush.economy.network;

import dev.bohush.economy.Economy;
import net.minecraft.util.Identifier;

public class ModPackets {
    public static final Identifier UPDATE_OFFERS_S2C = new Identifier(Economy.MOD_ID, "update_offers");

    public static final Identifier UPDATE_STYLE_C2S = new Identifier(Economy.MOD_ID, "update_style");
}
