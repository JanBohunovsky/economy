package dev.bohush.economy.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bohush.economy.Economy;
import dev.bohush.economy.client.gui.widget.*;
import dev.bohush.economy.entity.ModEntities;
import dev.bohush.economy.entity.ShopVillagerEntity;
import dev.bohush.economy.network.ModPackets;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import dev.bohush.economy.shop.villager.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class ShopStyleManagementSubScreen extends HandledSubScreen<ShopOwnerScreenHandler> {
    protected static final Identifier TEXTURE = new Identifier(Economy.MOD_ID, "textures/gui/shop_style.png");
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    private final Random random;
    private final List<SoundEvent> villagerSounds;
    private ShopVillagerStyle villagerStyle;
    private ShopVillagerEntity villagerEntity;
    private ShopVillagerEntityRendererWidget villagerRendererWidget;
    private SmallButtonWidget saveButton;
    private SmallButtonWidget cancelButton;
    private boolean followingMouse = true;
    private boolean loadStyleFromShop = true;

    protected ShopStyleManagementSubScreen(ShopOwnerScreenHandler handler) {
        super(handler);
        this.backgroundWidth = 277;
        this.random = new Random();
        this.villagerSounds = List.of(
            SoundEvents.ENTITY_VILLAGER_NO,
            SoundEvents.ENTITY_VILLAGER_YES
//            SoundEvents.ENTITY_VILLAGER_TRADE
//            SoundEvents.ENTITY_VILLAGER_AMBIENT
        );

        this.villagerStyle = ShopVillagerStyle.EMPTY;
        this.villagerEntity = new ShopVillagerEntity(ModEntities.SHOP_VILLAGER, MinecraftClient.getInstance().world) {
            @Override
            public ShopVillagerStyle getStyle() {
                return villagerStyle;
            }
        };
    }

    @Override
    protected void init() {
        if (this.loadStyleFromShop) {
            this.villagerStyle = this.handler.shop.getVillagerStyle();
            this.loadStyleFromShop = false;
        }

        // Villager
        this.villagerRendererWidget = new ShopVillagerEntityRendererWidget(this.x + 53, this.y + 138, 60, this.villagerEntity);
        this.villagerRendererWidget.setFollowingMouse(this.followingMouse);
        this.addDrawable(this.villagerRendererWidget);

        var toggleFollowMouseButton = new ToggleButtonWidget(this.x + 7, this.y + 150, new TranslatableText("shop.style.followCursor"), on -> {
            this.followingMouse = on;
            this.villagerRendererWidget.setFollowingMouse(on);
        });
        toggleFollowMouseButton.setOn(this.followingMouse);
        this.addDrawableChild(toggleFollowMouseButton);

        int left = this.x + 102;
        int labelTop = this.y + 9;
        int buttonTop = labelTop + this.textRenderer.fontHeight + 1;
        int buttonWidth = 169;
        int buttonHeight = 20;
        int offsetY = buttonHeight + this.textRenderer.fontHeight + 3;

        // Biome
        this.addDrawable(new LabelWidget(left, labelTop + offsetY * 0, new TranslatableText("shop.style.biome")));
        var biomeSelectionWidget = new SelectionWidget<BiomeClothes>(
            left, buttonTop + offsetY * 0, buttonWidth,
            BiomeClothes.values(),
            this.villagerStyle.getBiomeClothes(),
            true,
            BiomeClothes::getDisplayText,
            (widget, value) -> {
                this.villagerStyle = this.villagerStyle.withBiomeClothes(value);
                this.updateButtons();
            }
        );
        this.addDrawableChild(biomeSelectionWidget);

        // Profession
        this.addDrawable(new LabelWidget(left, labelTop + offsetY * 1, new TranslatableText("shop.style.profession")));
        var professionSelectionWidget = new SelectionWidget<ProfessionClothes>(
            left, buttonTop + offsetY * 1, buttonWidth,
            ProfessionClothes.values(),
            this.villagerStyle.getProfessionClothes(),
            true,
            ProfessionClothes::getDisplayText,
            (widget, value) -> {
                this.villagerStyle = this.villagerStyle.withProfessionClothes(value);
                this.updateButtons();
            }
        );
        this.addDrawableChild(professionSelectionWidget);

        // Hat
        this.addDrawable(new LabelWidget(left, labelTop + offsetY * 2, new TranslatableText("shop.style.hat")));
        var hatSelectionWidget = new SelectionWidget<Hat>(
            left, buttonTop + offsetY * 2, buttonWidth,
            Hat.values(),
            this.villagerStyle.getHat(),
            true,
            Hat::getDisplayText,
            (widget, value) -> {
                this.villagerStyle = this.villagerStyle.withHat(value);
                this.updateButtons();
            }
        );
        this.addDrawableChild(hatSelectionWidget);

        // Accessory
        this.addDrawable(new LabelWidget(left, labelTop + offsetY * 3, new TranslatableText("shop.style.accessory")));
        var accessorySelectionWidget = new SelectionWidget<Accessory>(
            left, buttonTop + offsetY * 3, buttonWidth,
            Accessory.values(),
            this.villagerStyle.getAccessory(),
            true,
            Accessory::getDisplayText,
            (widget, value) -> {
                this.villagerStyle = this.villagerStyle.withAccessory(value);
                this.updateButtons();
            }
        );
        this.addDrawableChild(accessorySelectionWidget);

        this.saveButton = new SmallButtonWidget(this.x + 146, this.y + 146,
            new TranslatableText("gui.button.save"),
            SmallButtonWidget.Style.SUCCESS,
            SmallButtonWidget.Symbol.CHECKMARK,
            button -> {
                var buf = PacketByteBufs.create();
                this.villagerStyle.toPacket(buf);
                ClientPlayNetworking.send(ModPackets.UPDATE_STYLE_C2S, buf);

                this.handler.shop.setVillagerStyle(this.villagerStyle);
                this.updateButtons();
            }
        );

        this.cancelButton = new SmallButtonWidget(this.saveButton.x + this.saveButton.getWidth() + 4, this.saveButton.y,
            new TranslatableText("gui.button.cancel"),
            SmallButtonWidget.Style.DANGER,
            button -> {
                this.villagerStyle = this.handler.shop.getVillagerStyle();
                biomeSelectionWidget.setValue(this.villagerStyle.getBiomeClothes());
                professionSelectionWidget.setValue(this.villagerStyle.getProfessionClothes());
                hatSelectionWidget.setValue(this.villagerStyle.getHat());
                accessorySelectionWidget.setValue(this.villagerStyle.getAccessory());
                this.updateButtons();
            }
        );

        this.updateButtons();
        this.addDrawableChild(this.saveButton);
        this.addDrawableChild(this.cancelButton);
    }

    private void updateButtons() {
        var changed = !this.villagerStyle.equals(this.handler.shop.getVillagerStyle());
        this.saveButton.active = changed;
        this.cancelButton.active = changed;
    }

    @Override
    public void enter() {
        this.handler.slots.clear();
        this.villagerRendererWidget.resetRotation();
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    protected void drawMouseoverTooltip(Screen screen, MatrixStack matrices, int x, int y) {
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
        drawTexture(matrices, x, y, this.getZOffset(), u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            return false;
        }

        // Randomly (10%) play villager sound on successful interaction with widgets.
        if (this.followingMouse && this.random.nextFloat() <= 0.1f) {
            var soundEvent = this.villagerSounds.get(this.random.nextInt(this.villagerSounds.size()));
            this.client.player.playSound(soundEvent, 1, this.villagerEntity.getSoundPitch());
        }

        return true;
    }
}
