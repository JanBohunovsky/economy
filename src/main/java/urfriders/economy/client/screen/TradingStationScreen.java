package urfriders.economy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import urfriders.economy.Economy;
import urfriders.economy.network.ModNetworking;
import urfriders.economy.screen.TradingStationScreenHandler;

public class TradingStationScreen extends HandledScreen<TradingStationScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/trading_station.png");

    private CustomButton customButton;

    public TradingStationScreen(TradingStationScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        customButton = addDrawableChild(new CustomButton(this.x + 61, this.y + 61, 54, 20, new LiteralText("Create"), (button) -> {
            System.out.println("Custom button click");
            ClientPlayNetworking.send(ModNetworking.SPAWN_VILLAGER, PacketByteBufs.empty());
        }));
    }

    @Environment(EnvType.CLIENT)
    class CustomButton extends ButtonWidget {

        public CustomButton(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress);
        }
    }
}
