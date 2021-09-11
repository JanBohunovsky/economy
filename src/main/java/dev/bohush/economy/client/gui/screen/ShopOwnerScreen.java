package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.gui.widget.OfferListWidget;
import dev.bohush.economy.client.gui.widget.OfferLockButtonWidget;
import dev.bohush.economy.client.gui.widget.SmallButtonWidget;
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

    private SmallButtonWidget saveButton;
    private OfferLockButtonWidget lockButton;
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

        int inventoryWidth = 18 * 9;
        int inventoryX = this.playerInventoryTitleX - 1;

        int titleWidth = this.textRenderer.getWidth(this.title);
        this.titleX = inventoryX + (inventoryWidth - titleWidth) / 2;

        var offerListWidget = new OfferListWidget(this.x + 7, this.y + 17, new ShopOfferList(), this::onOfferSelected, () -> null);
        this.addDrawableChild(offerListWidget);

        this.saveButton = new SmallButtonWidget(0, this.y + 64,
            new LiteralText("Save"),
            SmallButtonWidget.Style.SUCCESS,
            SmallButtonWidget.Symbol.CHECKMARK,
            (button) -> LOGGER.info("Save"));
        this.saveButton.setX(this.x + inventoryX + (inventoryWidth - saveButton.getWidth()) / 2);
        this.addDrawableChild(this.saveButton);

        this.lockButton = new OfferLockButtonWidget(this.x + 250, this.y + 36, (button) -> {
            this.lockButton.setLocked(!this.lockButton.isLocked());
        });
        this.addDrawableChild(this.lockButton);

        int toolbarX = this.x + 58;
        int buttonOffset = ToolbarButtonWidget.SIZE + 3;

        this.newOfferButton = new ToolbarButtonWidget(toolbarX, this.y + 5,
            ToolbarButtonWidget.Style.SUCCESS,
            ToolbarButtonWidget.Symbol.PLUS,
            new LiteralText("Add new offer"),
            (button) -> LOGGER.info("New offer"));
        this.addDrawableChild(this.newOfferButton);

        this.deleteOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset, this.y + 5,
            ToolbarButtonWidget.Style.DANGER,
            ToolbarButtonWidget.Symbol.MINUS,
            new LiteralText("Delete offer"),
            (button) -> LOGGER.info("Delete offer"));
        this.addDrawableChild(this.deleteOfferButton);

        this.moveUpOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset * 2, this.y + 5,
            ToolbarButtonWidget.Style.DEFAULT,
            ToolbarButtonWidget.Symbol.ARROW_UP,
            new LiteralText("Move offer up"),
            (button) -> LOGGER.info("Move up"));
        this.addDrawableChild(this.moveUpOfferButton);

        this.moveDownOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset * 3, this.y + 5,
            ToolbarButtonWidget.Style.DEFAULT,
            ToolbarButtonWidget.Symbol.ARROW_DOWN,
            new LiteralText("Move offer down"),
            (button) -> LOGGER.info("Move down"));
        this.addDrawableChild(this.moveDownOfferButton);
    }

    private void onOfferSelected(int offerIndex, ShopOffer offer) {
        LOGGER.info("offer selected: {}", offerIndex);
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
            this.newOfferButton.setActive(!this.newOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_2) {
            this.deleteOfferButton.setActive(!this.deleteOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_3) {
            this.moveUpOfferButton.setActive(!this.moveUpOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_4) {
            this.moveDownOfferButton.setActive(!this.moveDownOfferButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_S) {
            this.saveButton.setActive(!this.saveButton.isActive());
        }
        if (keyCode == GLFW.GLFW_KEY_L) {
            this.lockButton.setActive(!this.lockButton.isActive());
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

        if (this.lockButton.isLocked()) {
            RenderSystem.enableBlend();
            this.drawTexture(matrices, this.x + 190, this.y + 37, this.backgroundWidth, 0, 15, 15);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        super.drawMouseoverTooltip(matrices, x, y);

        if (this.lockButton.isLocked()
            && x >= this.x + 187 && x < this.x + 187 + 22
            && y >= this.y + 34 && y < this.y + 34 + 21) {
            this.renderTooltip(matrices, new TranslatableText("shop.offer.locked"), x, y);
        }
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
