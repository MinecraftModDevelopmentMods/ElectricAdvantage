package zone.moddev.mc.electricadvantage.graphics;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/** Scoped render state replaces the old global lighting/blending mutations. */
final class ElectricRenderTypes extends RenderState {
    private ElectricRenderTypes() {
        super("electricadvantage", () -> {}, () -> {});
    }

    static RenderType unshaded(ResourceLocation texture, boolean translucent) {
        return RenderType.makeType("electricadvantage_unshaded", DefaultVertexFormats.ENTITY,
                7, 256, false, translucent, RenderType.State.getBuilder()
                        .texture(new TextureState(texture, false, false))
                        .transparency(translucent ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                        .diffuseLighting(DIFFUSE_LIGHTING_DISABLED).cull(CULL_DISABLED)
                        .lightmap(LIGHTMAP_ENABLED).overlay(OVERLAY_ENABLED).build(false));
    }
}
