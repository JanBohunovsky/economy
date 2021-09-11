package dev.bohush.economy.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RenderSystemUtils {
    public static void setShaderColor(int color, float alpha) {
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        RenderSystem.setShaderColor(red, green, blue, alpha);
    }
}
