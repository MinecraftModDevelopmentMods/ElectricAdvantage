package com.mcmoddev.electricadvantage.machines;

import net.minecraftforge.fluids.FluidStack;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.junit.BeforeClass;
import org.junit.Test;
import com.mcmoddev.electricadvantage.init.Power;

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
        controller.getTank().setFluid(new FluidStack(FluidRegistry.WATER, 2));
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

    @Test public void hydroelectricDisplayIsNormalizedWithoutChangingStoredPowerOrRatedGeneration() throws Exception {
        HydroelectricGeneratorTileEntity active = new HydroelectricGeneratorTileEntity() {
            @Override public boolean isActive() { return true; }
        };
        HydroelectricGeneratorTileEntity idle = new HydroelectricGeneratorTileEntity() {
            @Override public boolean isActive() { return false; }
        };
        active.setEnergy(37, Power.ELECTRIC_POWER);
        idle.setEnergy(19, Power.ELECTRIC_POWER);
        assertEquals(1, hydroelectricDisplay(active).length);
        assertEquals(1, hydroelectricDisplay(active)[0], 0);
        assertEquals(0, hydroelectricDisplay(idle)[0], 0);
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
        assertTrue("Use native block traversal", world.reads > 0);
        assertEquals(32, origin.distanceTo(end), 0.000001);
        world.wall = world.turret.east();
        Vec3d covered = (Vec3d)trace.invoke(turret, origin, new Vec3d(1, 0, 0), 32.0);
        assertEquals("Ignoring our post must not skip the immediately adjacent cover",
                0.5, origin.distanceTo(covered), 0.000001);
    }

    @Test public void nativeSightQueryAcceptsValidTargetAndRespectsCover() throws Exception {
        BeamWorld world = withoutConstructor(BeamWorld.class);
        StubTurret turret = withoutConstructor(StubTurret.class);
        turret.testWorld = world;
        turret.setPos(new BlockPos(101, 82, -27));
        Entity observed = target();
        assertFalse(turret.canSeeEntity(null));
        assertTrue("Visible target must remain visible through the native 1.13 API", turret.canSeeEntity(observed));
        assertTrue("Use native block traversal, not a canned ray result", world.reads > 0);
        world.wall = new BlockPos(101, 82, -24);
        assertFalse("Actual stone must prevent sight of the same target", turret.canSeeEntity(observed));
    }

    @Test public void beamCannotSkipImmediatelyAdjacentCoverInAnyDirection() throws Exception {
        BeamWorld world = withoutConstructor(BeamWorld.class);
        world.turret = new BlockPos(101, 82, -27);
        StubTurret turret = withoutConstructor(StubTurret.class);
        turret.testWorld = world;
        turret.setPos(world.turret);
        Vec3d origin = new Vec3d(101.5, 82.75, -26.5);
        Method trace = LaserTurretTileEntity.class.getDeclaredMethod("followRayToSolidBlock",
                Vec3d.class, Vec3d.class, double.class);
        trace.setAccessible(true);
        for (EnumFacing facing : EnumFacing.values()) {
            world.wall = world.turret.offset(facing);
            Vec3d direction = new Vec3d(facing.getDirectionVec());
            Vec3d end = origin.add(direction.scale(32));
            net.minecraft.util.math.RayTraceResult expected = new net.minecraft.util.math.AxisAlignedBB(world.wall)
                    .calculateIntercept(origin, end);
            assertNotNull(expected);
            Vec3d actual = (Vec3d)trace.invoke(turret, origin, direction, 32.0);
            assertEquals("Adjacent cover in direction " + facing, 0, expected.hitVec.distanceTo(actual), 0.000001);
        }
    }

    @Test public void beamWithoutAnEntityContextStillStopsAtAvailableCover() throws Exception {
        BeamWorld world = withoutConstructor(BeamWorld.class);
        StubTurret turret = withoutConstructor(StubTurret.class);
        turret.testWorld = world;
        turret.setPos(new BlockPos(101, 82, -27));
        world.wall = new BlockPos(106, 82, -27);
        Vec3d origin = new Vec3d(101.5, 82.75, -26.5);
        Method trace = LaserTurretTileEntity.class.getDeclaredMethod("followRayToSolidBlock",
                Vec3d.class, Vec3d.class, double.class);
        trace.setAccessible(true);
        Vec3d end = (Vec3d)trace.invoke(turret, origin, new Vec3d(1, 0, 0), 32.0);
        assertEquals(4.5, origin.distanceTo(end), 0.000001);
        assertTrue(world.reads > 0);
    }

    private static Entity target() {
        EntityItem target = new EntityItem(null);
        target.setPosition(101.5, 82, -20.5);
        return target;
    }

    private static float[] hydroelectricDisplay(HydroelectricGeneratorTileEntity generator) throws Exception {
        try {
            return (float[])HydroelectricGeneratorTileEntity.class.getMethod("getProgress").invoke(generator);
        } catch (NoSuchMethodException legacy) {
            // The legacy screen passes this reading directly to the progress bar.
            return new float[]{generator.getPowerOutput()};
        }
    }

    private static GrowthChamberControllerTileEntity controller() throws Exception {
        GrowthChamberControllerTileEntity controller = new GrowthChamberControllerTileEntity();
        controller.setSoil(1000);
        controller.getTank().setFluid(new FluidStack(FluidRegistry.WATER, 1800));
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

    private static final class LightWorld extends WorldServer {
        BlockPos surface;
        int brightness, skyReads, lightReads;
        private LightWorld() {
            super(null, null, null, 0, null);
        }
        @Override public boolean canSeeSky(BlockPos pos) {
            assertEquals("Sky access must be sampled above the solid panel", surface, pos);
            skyReads++;
            return false;
        }
        @Override public int getLightFromNeighbors(BlockPos pos) {
            assertEquals("Incident block light must be sampled above the solid panel", surface, pos);
            lightReads++;
            return brightness;
        }
    }

    private static final class BeamWorld extends WorldServer {
        BlockPos turret, wall;
        int reads;
        private BeamWorld() { super(null, null, null, 0, null); }
        @Override public IBlockState getBlockState(BlockPos pos) {
            reads++;
            if (pos.equals(wall)) return Blocks.STONE.getDefaultState();
            if (pos.equals(turret)) return new LaserTurretBlock(false).getDefaultState();
            return Blocks.AIR.getDefaultState();
        }
        @Override public Entity getEntityByID(int id) { return null; }
    }

    private static final class StubTurret extends LaserTurretTileEntity {
        World testWorld;
        @Override public World getWorld() { return testWorld; }
    }
}
