package zone.moddev.mc.electricadvantage.graphics;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import zone.moddev.mc.electricadvantage.entities.HydroturbineEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricDrillTileEntity;
import zone.moddev.mc.electricadvantage.machines.LaserTurretTileEntity;

/** Client-only renderer registration. */
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(ElectricDrillTileEntity.class, new LaserDrillRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(LaserTurretTileEntity.class, new LaserTurretRenderer());
        RenderingRegistry.registerEntityRenderingHandler(HydroturbineEntity.class, HydroturbineRenderer::new);
    }
}
