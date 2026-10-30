package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.fluid.FluidStack;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.electricadvantage.init.Power;

import static org.junit.Assert.*;

/** Focused pickup transitions; the independent rig covers natural server ticks. */
public class ElectricOperationalPumpPickupTest {
    private static final BlockPos PUMP = new BlockPos(8, 84, 8);
    private static final BlockPos SOURCE = PUMP.down().north();
    private static final float RESERVE = 8000;
    private static final float PICKUP_COST = 1000 + 32;

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void waterloggedSlabKeepsItsBlockAndSlabType() throws Exception {
        BlockState wet = Blocks.OAK_SLAB.getDefaultState()
                .with(SlabBlock.TYPE, SlabType.TOP)
                .with(BlockStateProperties.WATERLOGGED, true);
        StubPump pump = fixture(wet);
        pump.tickUpdate(true);
        assertSuccessfulPickup(pump, Fluids.WATER);
        assertEquals(wet.with(BlockStateProperties.WATERLOGGED, false), pump.testWorld.getBlockState(SOURCE));
        assertEquals(1, pump.testWorld.writes);
    }

    @Test public void waterloggedStairsKeepFacingHalfAndShape() throws Exception {
        BlockState wet = Blocks.OAK_STAIRS.getDefaultState()
                .with(StairsBlock.FACING, Direction.SOUTH)
                .with(StairsBlock.HALF, Half.TOP)
                .with(StairsBlock.SHAPE, StairsShape.OUTER_LEFT)
                .with(BlockStateProperties.WATERLOGGED, true);
        StubPump pump = fixture(wet);
        pump.tickUpdate(true);
        assertSuccessfulPickup(pump, Fluids.WATER);
        assertEquals(wet.with(BlockStateProperties.WATERLOGGED, false), pump.testWorld.getBlockState(SOURCE));
        assertEquals(1, pump.testWorld.writes);
    }

    @Test public void ordinaryWaterAndLavaSourcesStillYieldOneBucket() throws Exception {
        for (Block block : new Block[]{Blocks.WATER, Blocks.LAVA}) {
            StubPump pump = fixture(block.getDefaultState());
            pump.tickUpdate(true);
            assertSuccessfulPickup(pump, block == Blocks.WATER ? Fluids.WATER : Fluids.LAVA);
            assertEquals(Blocks.AIR.getDefaultState(), pump.testWorld.getBlockState(SOURCE));
            assertEquals(1, pump.testWorld.writes);
        }
    }

    @Test public void sourceWaterInsideNonPickupSeagrassIsNotDestroyed() throws Exception {
        StubPump pump = fixture(Blocks.SEAGRASS.getDefaultState());
        assertTrue(pump.testWorld.getFluidState(SOURCE).isSource());
        assertFalse(pump.testWorld.getBlockState(SOURCE).getBlock() instanceof IBucketPickupHandler);
        pump.tickUpdate(true);
        assertUntouched(pump, Blocks.SEAGRASS.getDefaultState(), RESERVE, 0);
    }

    @Test public void refusedOrNullPickupDoesNotInventFluidOrDebitElectricity() throws Exception {
        for (boolean returnNull : new boolean[]{false, true}) {
            RefusingSource block = new RefusingSource(returnNull);
            StubPump pump = fixture(block.getDefaultState());
            pump.tickUpdate(true);
            assertEquals(1, block.calls);
            assertUntouched(pump, block.getDefaultState(), RESERVE, 0);
        }
    }

    @Test public void insufficientElectricityDoesNotCallPickup() throws Exception {
        RefusingSource block = new RefusingSource(false);
        StubPump pump = fixture(block.getDefaultState());
        pump.setEnergy(PICKUP_COST - 1, Power.ELECTRIC_POWER);
        pump.tickUpdate(true);
        assertEquals(0, block.calls);
        assertUntouched(pump, block.getDefaultState(), PICKUP_COST - 1, 0);
    }

    @Test public void occupiedTankDoesNotCallPickupOrOverwriteItsFluid() throws Exception {
        RefusingSource block = new RefusingSource(false);
        StubPump pump = fixture(block.getDefaultState());
        pump.getTank().setFluid(new FluidStack(Fluids.LAVA, 1));
        pump.tickUpdate(true);
        assertEquals(0, block.calls);
        assertUntouched(pump, block.getDefaultState(), RESERVE, 1);
        assertSame(Fluids.LAVA, pump.getTank().getFluid().getFluid());
    }

    @Test public void flowingWaterAndDrySlabsCannotBePickedUpAsSources() throws Exception {
        for (BlockState state : new BlockState[]{
                Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 1),
                Blocks.OAK_SLAB.getDefaultState()}) {
            StubPump pump = fixture(state);
            pump.tickUpdate(true);
            assertUntouched(pump, state, RESERVE, 0);
        }
    }

    private static StubPump fixture(BlockState source) throws Exception {
        PumpWorld world = withoutConstructor(PumpWorld.class);
        world.blocks = new HashMap<>();
        world.blocks.put(SOURCE, source);
        StubPump pump = new StubPump();
        pump.testWorld = world;
        pump.setPos(PUMP);
        pump.setEnergy(RESERVE, Power.ELECTRIC_POWER);
        CompoundNBT patch = new CompoundNBT();
        patch.putByte("NextPump", (byte)0);
        pump.loadFrom(patch);
        return pump;
    }

    private static void assertSuccessfulPickup(StubPump pump, Fluid expected) {
        assertEquals(1000, pump.getTank().getFluidAmount());
        assertSame(expected, pump.getTank().getFluid().getFluid());
        assertEquals(RESERVE - PICKUP_COST, pump.getEnergy(Power.ELECTRIC_POWER), 0);
        assertEquals(2, pump.sounds);
    }

    private static void assertUntouched(StubPump pump, BlockState expected, float energy, int fluid) {
        assertEquals(expected, pump.testWorld.getBlockState(SOURCE));
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

    private static final class PumpWorld extends ServerWorld {
        Map<BlockPos, BlockState> blocks;
        int writes;
        private PumpWorld() {
            super(null, null, null, null, null, null, null);
        }
        @Override public boolean isBlockLoaded(BlockPos pos) { return true; }
        @Override public BlockState getBlockState(BlockPos pos) {
            return blocks.getOrDefault(pos, Blocks.STONE.getDefaultState());
        }
        @Override public IFluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
        @Override public boolean isAirBlock(BlockPos pos) { return getBlockState(pos).isAir(); }
        @Override public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
            writes++;
            blocks.put(pos.toImmutable(), state);
            return true;
        }
    }

    private static final class RefusingSource extends Block implements IBucketPickupHandler {
        final boolean returnNull;
        int calls;
        RefusingSource(boolean returnNull) {
            super(Block.Properties.create(Material.ROCK));
            this.returnNull = returnNull;
        }
        @Override public IFluidState getFluidState(BlockState state) { return Fluids.WATER.getStillFluidState(false); }
        @Override public Fluid pickupFluid(IWorld world, BlockPos pos, BlockState state) {
            calls++;
            return returnNull ? null : Fluids.EMPTY;
        }
    }
}
