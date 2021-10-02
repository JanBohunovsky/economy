package dev.bohush.economy.client.gui.screen;

import net.minecraft.screen.ScreenHandler;

public abstract class HandledSubScreen<T extends ScreenHandler> extends SubScreen {
    protected T handler;

    protected HandledSubScreen(T handler) {
        super();
        this.handler = handler;
    }
}
