package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.gui.widget.OfferListWidget;
import dev.bohush.economy.client.gui.widget.OfferLockButtonWidget;
import dev.bohush.economy.client.gui.widget.SmallButtonWidget;
import dev.bohush.economy.client.gui.widget.ToolbarButtonWidget;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import dev.bohush.economy.shop.ShopOffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShopOfferManagementSubScreen extends HandledSubScreen<ShopOwnerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_trade.png");
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    private final Text title;
    private final Text playerInventoryTitle;

    private int titleX;
    private int titleY;
    private int playerInventoryTitleX;
    private int playerInventoryTitleY;

    private SmallButtonWidget saveButton;
    private OfferLockButtonWidget lockButton;
    private ToolbarButtonWidget moveUpOfferButton;
    private ToolbarButtonWidget moveDownOfferButton;
    private ToolbarButtonWidget deleteOfferButton;
    private ToolbarButtonWidget newOfferButton;

    public ShopOfferManagementSubScreen(ShopOwnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler);
        this.backgroundWidth = 277;

        this.titleX = 8;
        this.titleY = 6;

        this.playerInventoryTitleX = 109;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        this.title = title;
        this.playerInventoryTitle = inventory.getDisplayName();
    }

    @Override
    protected void init() {
        int inventoryWidth = 18 * 9;
        int inventoryX = this.playerInventoryTitleX - 1;

        int titleWidth = this.textRenderer.getWidth(this.title);
        this.titleX = inventoryX + (inventoryWidth - titleWidth) / 2;

        var offerListWidget = new OfferListWidget(this.x + 7, this.y + 17, true,
            this.handler.shop.getOffers(),
            this::onOfferSelected,
            this.handler::getSelectedOffer);
        this.addDrawableChild(offerListWidget);

        this.saveButton = new SmallButtonWidget(0, this.y + 64,
            new TranslatableText("gui.button.save"),
            SmallButtonWidget.Style.SUCCESS,
            SmallButtonWidget.Symbol.CHECKMARK,
            button -> this.clickButton(this.lockButton.locked
                ? ShopOwnerScreenHandler.SAVE_LOCKED_OFFER_BUTTON
                : ShopOwnerScreenHandler.SAVE_UNLOCKED_OFFER_BUTTON));
        this.saveButton.x = this.x + inventoryX + (inventoryWidth - saveButton.getWidth()) / 2;
        this.addDrawableChild(this.saveButton);

        this.lockButton = new OfferLockButtonWidget(this.x + 250, this.y + 36, button -> {
            this.lockButton.locked = !this.lockButton.locked;
        });
        this.addDrawableChild(this.lockButton);

        int toolbarX = this.x + 8;//58;
        int buttonOffset = ToolbarButtonWidget.SIZE + 3;

        this.newOfferButton = new ToolbarButtonWidget(toolbarX, this.y + 5,
            ToolbarButtonWidget.Style.SUCCESS,
            ToolbarButtonWidget.Symbol.PLUS,
            new TranslatableText("shop.offer.add"),
            button -> {
                this.clickButton(ShopOwnerScreenHandler.NEW_OFFER_BUTTON);
                offerListWidget.scrollTo(this.handler.getOfferIndex());
                updateButtonState();
            });
        this.addDrawableChild(this.newOfferButton);

        this.deleteOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset, this.y + 5,
            ToolbarButtonWidget.Style.DANGER,
            ToolbarButtonWidget.Symbol.MINUS,
            new TranslatableText("shop.offer.delete"),
            button -> {
                this.clickButton(ShopOwnerScreenHandler.DELETE_OFFER_BUTTON);
                offerListWidget.scrollTo(this.handler.getOfferIndex());
                updateButtonState();
            });
        this.addDrawableChild(this.deleteOfferButton);

        this.moveUpOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset * 2, this.y + 5,
            ToolbarButtonWidget.Style.DEFAULT,
            ToolbarButtonWidget.Symbol.ARROW_UP,
            new TranslatableText("shop.offer.move_up"),
            button -> {
                this.clickButton(ShopOwnerScreenHandler.MOVE_OFFER_UP_BUTTON);
                offerListWidget.scrollTo(this.handler.getOfferIndex());
                updateButtonState();
            });
        this.addDrawableChild(this.moveUpOfferButton);

        this.moveDownOfferButton = new ToolbarButtonWidget(toolbarX + buttonOffset * 3, this.y + 5,
            ToolbarButtonWidget.Style.DEFAULT,
            ToolbarButtonWidget.Symbol.ARROW_DOWN,
            new TranslatableText("shop.offer.move_down"),
            button -> {
                this.clickButton(ShopOwnerScreenHandler.MOVE_OFFER_DOWN_BUTTON);
                offerListWidget.scrollTo(this.handler.getOfferIndex());
                updateButtonState();
            });
        this.addDrawableChild(this.moveDownOfferButton);

        this.updateButtonState();
    }

    private void onOfferSelected(int offerIndex, ShopOffer offer) {
        this.clickButton(offerIndex);
        this.updateButtonState();
    }

    private void updateButtonState() {
        var offer = this.handler.getSelectedOffer();
        var index = this.handler.getOfferIndex();

        if (offer == null) {
            this.deleteOfferButton.active = false;
            this.moveUpOfferButton.active = false;
            this.moveDownOfferButton.active = false;
            this.saveButton.active = false;
            this.lockButton.locked = false;
            this.lockButton.active = false;
            return;
        }

        this.deleteOfferButton.active = true;
        this.moveUpOfferButton.active = index > 0;
        this.moveDownOfferButton.active = index < this.handler.shop.getOffers().size() - 1;
        this.saveButton.active = true;
        this.lockButton.active = true;
        this.lockButton.locked = offer.isLocked();
    }

    private void clickButton(int id) {
        this.handler.onButtonClick(this.client.player, id);
        this.client.interactionManager.clickButton(this.handler.syncId, id);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, this.titleX, this.titleY, 0x404040);
        this.textRenderer.draw(matrices, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040);
    }

    @Override
    public void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (this.lockButton.locked) {
            RenderSystem.enableBlend();
            this.drawTexture(matrices, this.x + 190, this.y + 37, this.backgroundWidth, 0, 15, 15);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawDebugText(matrices);
    }

    private void drawDebugText(MatrixStack matrices) {
        this.textRenderer.draw(matrices, Integer.toString(this.handler.getOfferIndex()), 3, 3, 0xffffff);
        if (this.handler.getSelectedOffer() != null) {
            ShopOffer offer = this.handler.getSelectedOffer();
            this.textRenderer.draw(matrices, String.format("%,d", offer.getSellItemStock()), 3, 13, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForFirstItem()), 3, 23, 0xffffff);
            this.textRenderer.draw(matrices, String.format("%,d", offer.getAvailableSpaceForSecondItem()), 3, 33, 0xffffff);
        }
    }

    @Override
    protected void drawMouseoverTooltip(Screen screen, MatrixStack matrices, int x, int y) {
        if (this.lockButton.locked
            && x >= this.x + 187 && x < this.x + 187 + 22
            && y >= this.y + 34 && y < this.y + 34 + 21) {
            screen.renderTooltip(matrices, new TranslatableText("shop.offer.locked"), x, y);
        }
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_HEIGHT, TEXTURE_WIDTH);
    }
}
