package zone.moddev.mc.electricadvantage.graphics;

import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.machines.LaserTurretTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT) // This is needed for classes that extend client-only classes
public class LaserTurretRenderer extends TileEntityRenderer<LaserTurretTileEntity>{

	
	private final ResourceLocation texture = new ResourceLocation(ElectricAdvantage.MODID+":textures/entity/laser_turret.png");
	private final RenderType renderType = ElectricRenderTypes.unshaded(texture, true);
	
	public LaserTurretRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	
	@Override
	public void render(final LaserTurretTileEntity e, final float partialTick, final MatrixStack matrices,
            final IRenderTypeBuffer buffers, final int packedLight, final int packedOverlay) {
		final float sideU0 = 0;
		final float sideU1 = 0.5f;
		final float sideV0 = 0;
		final float sideV1 = 0.5f;
		final float endU0 = 0.5f;
		final float endU1 = 1;
		final float endV0;
		final float endV1;
		if(e.isActive()){
			endV0 = 0;
			endV1 = 0.5f;
		}else{
			endV0 = 0.5f;
			endV1 = 1.0f;
		}

		final float laserU0 = 0;
		final float laserU1 = 0.5f;
		final float laserV0 = 0.5f;
		final float laserV1 = 1.0f;

		final float radius = 0.25f;
		final float laserRadius = 0.125f;
		

		float tickRemainder = 1 - partialTick;
		float yaw = -1*(tickRemainder * e.rotOldYaw + partialTick * e.rotYaw) - 90;
		float pitch = tickRemainder * e.rotOldPitch + partialTick * e.rotPitch;
		

		matrices.pushPose();
		matrices.translate(0.5f, 0.75f, 0.5f);
		matrices.mulPose(Vector3f.YP.rotationDegrees(yaw));
		matrices.mulPose(Vector3f.XP.rotationDegrees(pitch));
        BufferedQuads worldRenderer = new BufferedQuads(matrices,
                buffers.getBuffer(renderType), packedLight, packedOverlay);
		
		worldRenderer.pos( radius, radius, -radius).tex(endU0, endV0).endVertex();
		worldRenderer.pos( radius,-radius, -radius).tex(endU0, endV1).endVertex();
		worldRenderer.pos(-radius,-radius, -radius).tex(endU1, endV1).endVertex();
		worldRenderer.pos(-radius, radius, -radius).tex(endU1, endV0).endVertex();

		worldRenderer.pos(-radius, radius,  radius).tex(sideU0, sideV0).endVertex();
		worldRenderer.pos(-radius,-radius,  radius).tex(sideU0, sideV1).endVertex();
		worldRenderer.pos( radius,-radius,  radius).tex(sideU1, sideV1).endVertex();
		worldRenderer.pos( radius, radius,  radius).tex(sideU1, sideV0).endVertex();

		worldRenderer.pos(-radius, radius, -radius).tex(sideU0, sideV0).endVertex();
		worldRenderer.pos(-radius,-radius, -radius).tex(sideU0, sideV1).endVertex();
		worldRenderer.pos(-radius,-radius,  radius).tex(sideU1, sideV1).endVertex();
		worldRenderer.pos(-radius, radius,  radius).tex(sideU1, sideV0).endVertex();

		worldRenderer.pos( radius, radius,  radius).tex(sideU0, sideV0).endVertex();
		worldRenderer.pos( radius,-radius,  radius).tex(sideU0, sideV1).endVertex();
		worldRenderer.pos( radius,-radius, -radius).tex(sideU1, sideV1).endVertex();
		worldRenderer.pos( radius, radius, -radius).tex(sideU1, sideV0).endVertex();

		worldRenderer.pos(-radius, radius, -radius).tex(sideU0, sideV0).endVertex();
		worldRenderer.pos(-radius, radius,  radius).tex(sideU0, sideV1).endVertex();
		worldRenderer.pos( radius, radius,  radius).tex(sideU1, sideV1).endVertex();
		worldRenderer.pos( radius, radius, -radius).tex(sideU1, sideV0).endVertex();

		worldRenderer.pos(-radius,-radius,  radius).tex(sideU0, sideV0).endVertex();
		worldRenderer.pos(-radius,-radius, -radius).tex(sideU0, sideV1).endVertex();
		worldRenderer.pos( radius,-radius, -radius).tex(sideU1, sideV1).endVertex();
		worldRenderer.pos( radius,-radius,  radius).tex(sideU1, sideV0).endVertex();

		if(e.showLaserLine() && e.laserBlastLength > 0){
			final double x1, y1, z1, x2, y2, z2,  x3, y3, z3, x4, y4, z4;
			x1 = 0;
			y1 = -laserRadius;
			z1 = 0;
			x2 = 0;
			y2 = laserRadius;
			z2 = -e.laserBlastLength;
			
			x3 = -laserRadius;
			y3 = 0;
			z3 = z1;
			x4 = laserRadius;
			y4 = 0;
			z4 = z2;
			
            worldRenderer = new BufferedQuads(matrices,
                    buffers.getBuffer(renderType), 15 << 20 | 15 << 4, packedOverlay);
			
			worldRenderer.pos( x1, y1, z1).tex(laserU0, laserV1).endVertex();
			worldRenderer.pos( x2, y1, z2).tex(laserU1, laserV1).endVertex();
			worldRenderer.pos( x2, y2, z2).tex(laserU1, laserV0).endVertex();
			worldRenderer.pos( x1, y2, z1).tex(laserU0, laserV0).endVertex();
			
			worldRenderer.pos( x1, y2, z1).tex(laserU0, laserV1).endVertex();
			worldRenderer.pos( x2, y2, z2).tex(laserU1, laserV1).endVertex();
			worldRenderer.pos( x2, y1, z2).tex(laserU1, laserV0).endVertex();
			worldRenderer.pos( x1, y1, z1).tex(laserU0, laserV0).endVertex();


			worldRenderer.pos( x3, y3, z3).tex(laserU0, laserV1).endVertex();
			worldRenderer.pos( x3, y4, z4).tex(laserU1, laserV1).endVertex();
			worldRenderer.pos( x4, y4, z4).tex(laserU1, laserV0).endVertex();
			worldRenderer.pos( x4, y3, z3).tex(laserU0, laserV0).endVertex();
			
			worldRenderer.pos( x4, y3, z3).tex(laserU0, laserV1).endVertex();
			worldRenderer.pos( x4, y4, z4).tex(laserU1, laserV1).endVertex();
			worldRenderer.pos( x3, y4, z4).tex(laserU1, laserV0).endVertex();
			worldRenderer.pos( x3, y3, z3).tex(laserU0, laserV0).endVertex();
			
		}

		matrices.popPose();
	}

	
}
