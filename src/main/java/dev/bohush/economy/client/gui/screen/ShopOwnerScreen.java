package dev.bohush.economy.client.gui.screen;

import dev.bohush.economy.client.gui.widget.BaseButtonWidget;
import dev.bohush.economy.client.gui.widget.TabButtonWidget;
import dev.bohush.economy.client.gui.widget.TabWidget;
import dev.bohush.economy.screen.ShopOwnerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ShopOwnerScreen extends HandledScreen<ShopOwnerScreenHandler> {

    private final int paddingTop = 17;
    private List<Slot> originalSlots = DefaultedList.of();
    private TabWidget tabWidget;

    public ShopOwnerScreen(ShopOwnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 277;
        this.backgroundHeight = 166 + this.paddingTop;

        this.originalSlots.addAll(this.handler.slots);

        this.tabWidget = new TabWidget();
        this.tabWidget.add(new ShopOfferManagementSubScreen(this.handler, inventory, title));
        this.tabWidget.add(new ShopStyleManagementSubScreen(this.handler));
    }

    @Nullable
    @Override
    public Element getFocused() {
        return this.tabWidget;
    }

    @Override
    protected void init() {
        super.init();
        this.tabWidget.init(this.client, this.x, this.y + paddingTop, this.width, this.height);

        var selectedIndex = this.tabWidget.getSelectedIndex();

        var offersTabButton = new TabButtonWidget(this.x, this.y, 70,
            0,
            selectedIndex,
            TabButtonWidget.Position.LEFT,
            new TranslatableText("shop.offers"),
            this::tabButtonClick
        );

        var styleTabButton = new TabButtonWidget(offersTabButton.x + offersTabButton.getWidth() - 1, this.y, 70,
            1,
            selectedIndex,
            TabButtonWidget.Position.MIDDLE,
            new TranslatableText("shop.style"),
            this::tabButtonClick
        );

        this.addDrawableChild(this.tabWidget);
        this.addDrawableChild(offersTabButton);
        this.addDrawableChild(styleTabButton);
    }

    private void tabButtonClick(BaseButtonWidget baseButton) {
        var tabButton = (TabButtonWidget)baseButton;
        this.switchTab(tabButton.getIndex());
    }

    private void switchTab(int index) {
        this.handler.slots.clear();
        this.handler.slots.addAll(this.originalSlots);

        this.tabWidget.setSelectedIndex(index);
        if (this.tabWidget.getSelectedSubScreen().isEmpty()) {
            // We don't want to show all slots when there's no sub-screen,
            // because there may be slots that have similar position, so it would look like a mess.
            this.handler.slots.clear();
        }

        for (var child : this.children()) {
            if (child instanceof TabButtonWidget tabButtonWidget) {
                tabButtonWidget.setSelected(index);
            }
        }
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int bottom = this.height - (this.textRenderer.fontHeight - 1);
        this.textRenderer.draw(matrices, String.valueOf(this.tabWidget.getSelectedIndex()), 3, bottom - 3, 0xffffff);

        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        super.drawMouseoverTooltip(matrices, x, y);
    }
}
