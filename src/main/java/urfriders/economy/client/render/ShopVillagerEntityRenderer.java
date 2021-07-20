package urfriders.economy.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.VillagerEntityRenderer;

@Environment(EnvType.CLIENT)
public class ShopVillagerEntityRenderer extends VillagerEntityRenderer {

    public ShopVillagerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
}
