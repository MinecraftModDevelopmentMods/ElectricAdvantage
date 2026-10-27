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
        return RenderType.create("electricadvantage_unshaded", DefaultVertexFormats.NEW_ENTITY,
                7, 256, false, translucent, RenderType.State.builder()
                        .setTextureState(new TextureState(texture, false, false))
                        .setTransparencyState(translucent ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                        .setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false));
    }
}
