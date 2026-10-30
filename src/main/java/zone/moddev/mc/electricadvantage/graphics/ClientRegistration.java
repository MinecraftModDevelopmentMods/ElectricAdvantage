package zone.moddev.mc.electricadvantage.graphics;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import zone.moddev.mc.electricadvantage.init.Blocks;
import zone.moddev.mc.electricadvantage.init.Entities;

/** Client-only renderer registration. */
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(Blocks.electric_track, RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Entities.ELECTRIC_DRILL, LaserDrillRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Entities.LASER_TURRET, LaserTurretRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(Entities.HYDROTURBINE, HydroturbineRenderer::new);
    }
}
