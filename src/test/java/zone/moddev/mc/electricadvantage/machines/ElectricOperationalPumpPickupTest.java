package zone.moddev.mc.electricadvantage.machines;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.electricadvantage.init.Power;

import static org.junit.Assert.*;

/** 1.12 has no waterlogging: native modded drain contracts replace those later-version cases. */
public class ElectricOperationalPumpPickupTest {
    private static final BlockPos PUMP = new BlockPos(8, 84, 8);
    private static final BlockPos SOURCE = PUMP.down().north();
    private static final float RESERVE = 8000;
    private static final float PICKUP_COST = 1000 + 32;
    private static final AtomicInteger IDS = new AtomicInteger();
    private Field lookupCache;
    private Object previousCache;

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Before public void isolateFluidBlockLookup() throws Exception {
        lookupCache = FluidRegistry.class.getDeclaredField("fluidBlocks");
        lookupCache.setAccessible(true);
        previousCache = lookupCache.get(null);
        // Fixture fluids are registered before the same lazy lookup used during normal startup.
        lookupCache.set(null, null);
    }

    @After public void restoreFluidBlockLookup() throws Exception { lookupCache.set(null, previousCache); }

    @Test public void stillAndDynamicVanillaSourcesYieldExactlyOneBucket() throws Exception {
        for (Block block : new Block[]{Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA}) {
            StubPump pump = fixture(block.getDefaultState());
            pump.tickUpdate(true);
            Fluid expected = block == Blocks.WATER || block == Blocks.FLOWING_WATER ? FluidRegistry.WATER : FluidRegistry.LAVA;
            assertSuccessfulPickup(pump, expected);
        }
    }

    @Test public void moddedSourceUsesItsOwnRealDrainExactlyOnce() throws Exception {
        NativeSource source = source(true, 1000);
        StubPump pump = fixture(source.getDefaultState());
        pump.tickUpdate(true);
        assertTrue("Respect the modded source's permission to drain", source.permissionChecks > 0);
        assertEquals(1, source.actualDrains);
        assertSuccessfulPickup(pump, source.getFluid());
    }

    @Test public void nonDrainableModdedSourceCannotBeDeletedOrCreditedAsFluid() throws Exception {
        NativeSource source = source(false, 1000);
        StubPump pump = fixture(source.getDefaultState());
        pump.tickUpdate(true);
        assertUntouched(pump, source.getDefaultState(), RESERVE, 0);
        assertTrue(source.permissionChecks > 0);
        assertEquals(0, source.actualDrains);
    }

    @Test public void ordinarySolidsAndDrySlabsOrStairsAreNeverFluidSources() throws Exception {
        for (Block block : new Block[]{Blocks.STONE, Blocks.WOODEN_SLAB, Blocks.OAK_STAIRS, Blocks.TALLGRASS}) {
            StubPump pump = fixture(block.getDefaultState());
            pump.tickUpdate(true);
            assertUntouched(pump, block.getDefaultState(), RESERVE, 0);
        }
    }

    @Test public void refusedOrEmptyRealDrainCannotInventFluidOrDebitElectricity() throws Exception {
        for (int amount : new int[]{-1, 0}) {
            NativeSource source = source(true, amount);
            StubPump pump = fixture(source.getDefaultState());
            pump.tickUpdate(true);
            assertEquals(1, source.actualDrains);
            assertUntouched(pump, source.getDefaultState(), RESERVE, 0);
        }
    }

    @Test public void insufficientElectricityCannotDrainAModdedSource() throws Exception {
        NativeSource source = source(true, 1000);
        StubPump pump = fixture(source.getDefaultState());
        pump.setEnergy(PICKUP_COST - 1, Power.ELECTRIC_POWER);
        pump.tickUpdate(true);
        assertEquals(0, source.actualDrains);
        assertUntouched(pump, source.getDefaultState(), PICKUP_COST - 1, 0);
    }

    @Test public void occupiedTankCannotDrainASourceOrOverwriteItsFluid() throws Exception {
        NativeSource source = source(true, 1000);
        StubPump pump = fixture(source.getDefaultState());
        pump.getTank().setFluid(new FluidStack(FluidRegistry.LAVA, 1));
        pump.tickUpdate(true);
        assertEquals(0, source.actualDrains);
        assertUntouched(pump, source.getDefaultState(), RESERVE, 1);
        assertSame(FluidRegistry.LAVA, pump.getTank().getFluid().getFluid());
    }

    @Test public void flowingVanillaLiquidsCannotBePickedUpAsFullSources() throws Exception {
        for (Block block : new Block[]{Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA}) {
            IBlockState state = block.getDefaultState().withProperty(BlockLiquid.LEVEL, 1);
            StubPump pump = fixture(state);
            pump.tickUpdate(true);
            assertUntouched(pump, state, RESERVE, 0);
        }
    }

    private static NativeSource source(boolean allowed, int amount) throws Exception {
        Fluid fluid = new Fluid("advworks_electric_pump_test_" + IDS.incrementAndGet(),
                new ResourceLocation("minecraft", "blocks/water_still"),
                new ResourceLocation("minecraft", "blocks/water_flow"));
        assertTrue("Register a real, unique Forge fixture fluid", FluidRegistry.registerFluid(fluid));
        NativeSource source = new NativeSource(fluid, allowed, amount);
        Field cache = FluidRegistry.class.getDeclaredField("fluidBlocks");
        cache.setAccessible(true);
        cache.set(null, null);
        assertSame(fluid, FluidRegistry.lookupFluidForBlock(source));
        return source;
    }

    private static StubPump fixture(IBlockState source) throws Exception {
        PumpWorld world = withoutConstructor(PumpWorld.class);
        world.blocks = new HashMap<>();
        world.blocks.put(SOURCE, source);
        StubPump pump = new StubPump();
        pump.testWorld = world;
        pump.setPos(PUMP);
        pump.setEnergy(RESERVE, Power.ELECTRIC_POWER);
        NBTTagCompound patch = new NBTTagCompound();
        patch.setByte("NextPump", (byte)0);
        pump.loadFrom(patch);
        return pump;
    }

    private static void assertSuccessfulPickup(StubPump pump, Fluid expected) {
        assertEquals(1000, pump.getTank().getFluidAmount());
        assertSame(expected, pump.getTank().getFluid().getFluid());
        assertEquals(RESERVE - PICKUP_COST, pump.getEnergy(Power.ELECTRIC_POWER), 0);
        assertEquals(Blocks.AIR.getDefaultState(), pump.testWorld.getBlockState(SOURCE));
        assertEquals(1, pump.testWorld.writes);
        assertEquals(2, pump.sounds);
    }

    private static void assertUntouched(StubPump pump, IBlockState state, float energy, int fluid) {
        assertEquals(state, pump.testWorld.getBlockState(SOURCE));
        assertEquals(0, pump.testWorld.writes);
        assertEquals(energy, pump.getEnergy(Power.ELECTRIC_POWER), 0);
        assertEquals(fluid, pump.getTank().getFluidAmount());
        assertEquals(0, pump.sounds);
    }

    private static <T> T withoutConstructor(Class<T> type) throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }

    private static final class StubPump extends ElectricPumpTileEntity {
        PumpWorld testWorld;
        int sounds;
        @Override public World getWorld() { return testWorld; }
        @Override protected boolean hasRedstoneSignal() { return false; }
        @Override protected void playSoundEffect(double x, double y, double z, SoundEvent sound, float volume, float pitch) {
            sounds++;
        }
    }

    private static final class PumpWorld extends WorldServer {
        Map<BlockPos, IBlockState> blocks;
        int writes;
        private PumpWorld() { super(null, null, null, 0, null); }
        @Override public boolean isBlockLoaded(BlockPos pos) { return true; }
        @Override public IBlockState getBlockState(BlockPos pos) { return blocks.getOrDefault(pos, Blocks.STONE.getDefaultState()); }
        @Override public boolean isAirBlock(BlockPos pos) { return getBlockState(pos).getMaterial() == Material.AIR; }
        @Override public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
            writes++;
            blocks.put(pos.toImmutable(), state);
            return true;
        }
    }

    private static final class NativeSource extends BlockFluidClassic {
        final boolean allowed;
        final int amount;
        int permissionChecks, actualDrains;
        NativeSource(Fluid fluid, boolean allowed, int amount) {
            super(fluid, Material.WATER);
            this.allowed = allowed;
            this.amount = amount;
        }
        @Override public boolean isSourceBlock(IBlockAccess world, BlockPos pos) { return true; }
        @Override public boolean canDrain(World world, BlockPos pos) { permissionChecks++; return allowed; }
        @Override public FluidStack drain(World world, BlockPos pos, boolean real) {
            if (!real) return new FluidStack(getFluid(), 1000);
            actualDrains++;
            if (!allowed || amount < 0) return null;
            if (amount > 0) world.setBlockToAir(pos);
            return new FluidStack(getFluid(), amount);
        }
    }
}
