package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.fluid.FluidStack;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.electricadvantage.init.Power;

import static org.junit.Assert.*;

/** Operating regressions; the packaged rig independently checks natural machine ticks. */
public class ElectricOperationalNativeDefectsTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void controllerCannotRequestMoreThanItsHundredUnitElectricalBuffer() throws Exception {
        GrowthChamberControllerTileEntity controller = controller();
        assertEquals(100, controller.getPowerRequest(Power.ELECTRIC_POWER).amount, 0);
        assertEquals(0, controller.getEnergy(Power.ELECTRIC_POWER), 0);
        assertEquals(1000, controller.getSoil(), 0);
        assertEquals(1800, controller.getTank().getFluidAmount());
    }

    @Test public void controllerSubtractsElectricityAlreadyStoredFromInputCapacity() throws Exception {
        GrowthChamberControllerTileEntity controller = controller();
        controller.setEnergy(40, Power.ELECTRIC_POWER);
        assertEquals(60, controller.getPowerRequest(Power.ELECTRIC_POWER).amount, 0);
        controller.setEnergy(100, Power.ELECTRIC_POWER);
        assertEquals(0, controller.getPowerRequest(Power.ELECTRIC_POWER).amount, 0);
    }

    @Test public void scarceResourcesAndExistingElectricityBoundTheRemainingDemand() throws Exception {
        GrowthChamberControllerTileEntity controller = controller();
        controller.setSoil(1);
        controller.getTank().setFluid(new FluidStack(Fluids.WATER, 2));
        controller.setEnergy(4, Power.ELECTRIC_POWER);
        assertEquals(4, controller.getPowerRequest(Power.ELECTRIC_POWER).amount, 0);
        controller.setEnergy(999.75f, Power.GROWTHCHAMBER_POWER);
        assertEquals(0, controller.getPowerRequest(Power.ELECTRIC_POWER).amount, 0);
    }

    @Test public void fullGreenhouseBufferCannotRequestMoreElectricity() throws Exception {
        GrowthChamberControllerTileEntity controller = controller();
        controller.setEnergy(1000, Power.GROWTHCHAMBER_POWER);
        assertEquals(0, controller.getPowerRequest(Power.ELECTRIC_POWER).amount, 0);
    }

    @Test public void hydroelectricDisplayIsNormalizedWithoutChangingStoredPowerOrRatedGeneration() {
        HydroelectricGeneratorTileEntity active = new HydroelectricGeneratorTileEntity() {
            @Override public boolean isActive() { return true; }
        };
        HydroelectricGeneratorTileEntity idle = new HydroelectricGeneratorTileEntity() {
            @Override public boolean isActive() { return false; }
        };
        active.setEnergy(37, Power.ELECTRIC_POWER);
        idle.setEnergy(19, Power.ELECTRIC_POWER);
        assertEquals(1, active.getProgress().length);
        assertEquals(1, active.getProgress()[0], 0);
        assertEquals(0, idle.getProgress()[0], 0);
        assertEquals(15, active.getPowerOutput(), 0);
        assertEquals(0, idle.getPowerOutput(), 0);
        assertEquals(37, active.getEnergy(Power.ELECTRIC_POWER), 0);
        assertEquals(19, idle.getEnergy(Power.ELECTRIC_POWER), 0);
        assertEquals(4, HydroelectricGeneratorTileEntity.ENERGY_PER_TICK, 0);
    }

    @Test public void panelSamplesIncidentLightAboveItsSolidBody() throws Exception {
        LightWorld world = withoutConstructor(LightWorld.class);
        BlockPos panel = new BlockPos(101, 82, -27);
        world.surface = panel.up();
        world.brightness = 14;
        assertEquals(0.1f * 14 * 0.0666666f,
                PhotovoltaicGeneratorTileEntity.getLightIntensityAt(panel, world), 0.000001f);
        assertEquals(1, world.skyReads);
        assertEquals(1, world.lightReads);
    }

    @Test public void darkPanelSurfaceProducesNoBlocklightPower() throws Exception {
        LightWorld world = withoutConstructor(LightWorld.class);
        BlockPos panel = new BlockPos(101, 82, -27);
        world.surface = panel.up();
        assertEquals(0, PhotovoltaicGeneratorTileEntity.getLightIntensityAt(panel, world), 0);
    }

    @Test public void beamIgnoresOnlyItsOwnCollisionPostAndStopsAtTheDeclaredRange() throws Exception {
        BeamWorld world = withoutConstructor(BeamWorld.class);
        world.turret = new BlockPos(101, 82, -27);
        StubTurret turret = withoutConstructor(StubTurret.class);
        turret.testWorld = world;
        turret.setPos(world.turret);
        Vec3d origin = new Vec3d(101.5, 82.75, -26.5);
        Method trace = LaserTurretTileEntity.class.getDeclaredMethod("followRayToSolidBlock",
                Vec3d.class, Vec3d.class, double.class);
        trace.setAccessible(true);
        Vec3d end = (Vec3d)trace.invoke(turret, origin, new Vec3d(1, 0, 0), 32.0);
        assertEquals(1, world.traces);
        assertEquals(32, origin.distanceTo(end), 0.000001);
    }

    private static GrowthChamberControllerTileEntity controller() throws Exception {
        GrowthChamberControllerTileEntity controller = new GrowthChamberControllerTileEntity();
        controller.setSoil(1000);
        controller.getTank().setFluid(new FluidStack(Fluids.WATER, 1800));
        Field redstone = GrowthChamberControllerTileEntity.class.getDeclaredField("redstone");
        redstone.setAccessible(true);
        redstone.setBoolean(controller, false);
        return controller;
    }

    private static <T> T withoutConstructor(Class<T> type) throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }

    private static final class LightWorld extends ServerWorld {
        BlockPos surface;
        int brightness, skyReads, lightReads;
        private LightWorld() {
            super(null, null, null, null, null, null, null);
        }
        @Override public boolean canSeeSky(BlockPos pos) {
            assertEquals("Sky access must be sampled above the solid panel", surface, pos);
            skyReads++;
            return false;
        }
        @Override public int getLight(BlockPos pos) {
            assertEquals("Incident block light must be sampled above the solid panel", surface, pos);
            lightReads++;
            return brightness;
        }
    }

    private static final class BeamWorld extends ServerWorld {
        BlockPos turret;
        int traces;
        private BeamWorld() {
            super(null, null, null, null, null, null, null);
        }
        @Override public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
            traces++;
            assertTrue("Owning turret post must not stop its own beam",
                    context.getBlockShape(new LaserTurretBlock(false).getDefaultState(), this, turret).isEmpty());
            assertFalse("An immediately adjacent solid block must still stop the beam",
                    context.getBlockShape(Blocks.STONE.getDefaultState(), this, turret.east()).isEmpty());
            return BlockRayTraceResult.createMiss(context.func_222250_a(), Direction.EAST, new BlockPos(context.func_222250_a()));
        }
    }

    private static final class StubTurret extends LaserTurretTileEntity {
        World testWorld;
        @Override public World getWorld() { return testWorld; }
    }
}
