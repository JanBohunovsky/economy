package urfriders.economy.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import urfriders.economy.screen.ShopVillagerOwnerScreenHandler;

@Environment(EnvType.CLIENT)
public class ShopVillagerOwnerScreen extends ShopVillagerBaseScreen<ShopVillagerOwnerScreenHandler> {

    public ShopVillagerOwnerScreen(ShopVillagerOwnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

    }
}
