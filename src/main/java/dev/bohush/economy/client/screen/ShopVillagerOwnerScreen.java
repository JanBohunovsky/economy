package dev.bohush.economy.client.screen;

import dev.bohush.economy.screen.ShopVillagerOwnerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ShopVillagerOwnerScreen extends HandledScreen<ShopVillagerOwnerScreenHandler> {

    public ShopVillagerOwnerScreen(ShopVillagerOwnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    }
}