package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.gui.widget.OfferListWidget;
import dev.bohush.economy.client.gui.widget.ToolbarButtonWidget;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import dev.bohush.economy.shop.ShopOffer;
import dev.bohush.economy.shop.ShopOfferList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ShopOwnerScreen extends HandledScreen<ShopOwnerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_owner.png");
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;
    protected static final Text OFFERS_TEXT = new TranslatableText("shop.offers");

    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private DefaultedList<Slot> savedSlots = null;

    private ToolbarButtonWidget moveUpOfferButton;
    private ToolbarButtonWidget moveDownOfferButton;
    private ToolbarButtonWidget deleteOfferButton;
    private ToolbarButtonWidget newOfferButton;

    public ShopOwnerScreen(ShopOwnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 277;
        this.playerInventoryTitleX = 109;
    }

    @Override
    protected void init() {
        super.init();

        int titleWidth = this.textRenderer.getWidth(this.title);
        int titleOffset = 101;
        this.titleX = titleOffset + (this.backgroundWidth - titleOffset - titleWidth) / 2;

        var offerListWidget = new OfferListWidget(this.x + 7, this.y + 17, new ShopOfferList(), this::onOfferSelected, () -> null);
        this.addDrawableChild(offerListWidget);

        int toolbarX = this.x + 58;
        int buttonOffset = ToolbarButtonWidget.SIZE + 3;

        this.moveUpOfferButton = new ToolbarButtonWidget(toolbarX, this.y + 5,
            ToolbarButtonWidget.Style.DEFAULT,
            ToolbarButtonWidget.Symbol.ARROW_UP,
            new LiteralText("Move offer up"),
            this::onToolbarButtonPressed);
        this.addDrawableChild(moveUpOfferButton);

        this.moveDownOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset, this.y + 5,
            ToolbarButtonWidget.Style.DEFAULT,
            ToolbarButtonWidget.Symbol.ARROW_DOWN,
            new LiteralText("Move offer down"),
            this::onToolbarButtonPressed);
        this.addDrawableChild(moveDownOfferButton);

        this.deleteOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset * 2, this.y + 5,
            ToolbarButtonWidget.Style.DANGER,
            ToolbarButtonWidget.Symbol.MINUS,
            new LiteralText("Delete offer"),
            this::onToolbarButtonPressed);
        this.addDrawableChild(deleteOfferButton);

        this.newOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset * 3, this.y + 5,
            ToolbarButtonWidget.Style.SUCCESS,
            ToolbarButtonWidget.Symbol.PLUS,
            new LiteralText("Add new offer"),
            this::onToolbarButtonPressed);
        this.addDrawableChild(newOfferButton);
    }

    private void onOfferSelected(int offerIndex, ShopOffer offer) {
        LOGGER.info("offer selected: {}", offerIndex);
    }

    private void onToolbarButtonPressed(ToolbarButtonWidget button) {
        LOGGER.info("toolbar button pressed: {}", button.getTooltipText().asString());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSLASH) {
            if (this.savedSlots == null) {
                this.savedSlots = DefaultedList.of();
                this.savedSlots.addAll(this.handler.slots);
                this.handler.slots.clear();
            } else {
                this.handler.slots.addAll(this.savedSlots);
                this.savedSlots = null;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_1) {
            this.moveUpOfferButton.setActive(!this.moveUpOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_2) {
            this.moveDownOfferButton.setActive(!this.moveDownOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_3) {
            this.deleteOfferButton.setActive(!this.deleteOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_4) {
            this.newOfferButton.setActive(!this.newOfferButton.isActive());
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        this.textRenderer.draw(matrices, OFFERS_TEXT, 8, this.titleY, 0x404040);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
