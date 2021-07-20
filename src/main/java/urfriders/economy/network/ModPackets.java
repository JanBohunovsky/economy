package urfriders.economy.network;

import net.minecraft.util.Identifier;
import urfriders.economy.Economy;

public class ModPackets {
    public static final Identifier UPDATE_SHOP_C2S = new Identifier(Economy.MOD_ID, "update_shop");
    public static final Identifier SELECT_TRADE_C2S = new Identifier(Economy.MOD_ID, "select_trade");
}
