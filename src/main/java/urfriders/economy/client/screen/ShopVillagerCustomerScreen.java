package urfriders.economy.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import urfriders.economy.screen.ShopVillagerCustomerScreenHandler;

@Environment(EnvType.CLIENT)
public class ShopVillagerCustomerScreen extends ShopVillagerBaseScreen<ShopVillagerCustomerScreenHandler> {

    public ShopVillagerCustomerScreen(ShopVillagerCustomerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
}
