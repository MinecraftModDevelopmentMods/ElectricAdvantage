package zone.moddev.mc.electricadvantage.blocks;

import cyano.poweradvantage.api.ConduitBlock;
import cyano.poweradvantage.api.PowerConnectorContext;
import cyano.poweradvantage.api.simple.BlockSimplePowerConduit;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.electricadvantage.machines.ElectricDrillBlock;
import zone.moddev.mc.electricadvantage.machines.LaserTurretBlock;
import zone.moddev.mc.electricadvantage.machines.PhotovoltaicGeneratorBlock;
import static org.junit.Assert.*;

public class NativeBlockContractTest {
    @BeforeClass public static void bootstrapNativeRegistries() { Bootstrap.bootStrap(); }

    @Test public void scaffoldRetainsNonSolidFullCollisionAndSharedStateProperties() {
        ElectricScaffoldBlock block = new ElectricScaffoldBlock();
        BlockState state = block.defaultBlockState();
        assertFalse(state.canOcclude());
        assertFalse(state.isRedstoneConductor(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO));
        assertEquals(VoxelShapes.block().bounds(),
                block.getCollisionShape(state, null, BlockPos.ZERO, ISelectionContext.empty()).bounds());
        assertFalse(state.isSolidRender(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO));
        assertTrue(state.isCollisionShapeFullBlock(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO));
        assertEquals(6, state.getProperties().size());
        assertSame(BlockSimplePowerConduit.NORTH, ElectricScaffoldBlock.NORTH);
        assertSame(BlockSimplePowerConduit.SOUTH, ElectricScaffoldBlock.SOUTH);
        assertSame(BlockSimplePowerConduit.EAST, ElectricScaffoldBlock.EAST);
        assertSame(BlockSimplePowerConduit.WEST, ElectricScaffoldBlock.WEST);
        assertSame(BlockSimplePowerConduit.UP, ElectricScaffoldBlock.UP);
        assertSame(BlockSimplePowerConduit.DOWN, ElectricScaffoldBlock.DOWN);
        for (PathType type : PathType.values()) assertFalse(block.isPathfindable(state, null, BlockPos.ZERO, type));
    }

    @Test public void scaffoldInheritsSharedPlacementAndNetworkCallbacks() throws Exception {
        Class<ElectricScaffoldBlock> scaffold = ElectricScaffoldBlock.class;
        assertSame(BlockSimplePowerConduit.class, scaffold.getSuperclass());
        assertSame(BlockSimplePowerConduit.class,
                scaffold.getMethod("getStateForPlacement", BlockItemUseContext.class).getDeclaringClass());
        assertSame(BlockSimplePowerConduit.class,
                scaffold.getMethod("updateShape", BlockState.class, Direction.class, BlockState.class,
                        IWorld.class, BlockPos.class, BlockPos.class).getDeclaringClass());
        assertSame(BlockSimplePowerConduit.class,
                scaffold.getMethod("canAcceptConnection", PowerConnectorContext.class).getDeclaringClass());
        assertSame(ConduitBlock.class,
                scaffold.getMethod("onPlace", BlockState.class, World.class, BlockPos.class,
                        BlockState.class, boolean.class).getDeclaringClass());
        assertSame(ConduitBlock.class,
                scaffold.getMethod("onRemove", BlockState.class, World.class, BlockPos.class,
                        BlockState.class, boolean.class).getDeclaringClass());
        assertSame(ConduitBlock.class,
                scaffold.getMethod("wasExploded", World.class, BlockPos.class, Explosion.class).getDeclaringClass());
        ElectricScaffoldBlock block = new ElectricScaffoldBlock();
        assertEquals(1, block.getTypes().length);
        assertSame(Power.ELECTRIC_POWER, block.getTypes()[0]);
        assertFalse(block.isPowerSink(Power.ELECTRIC_POWER));
        assertFalse(block.isPowerSource(Power.ELECTRIC_POWER));
    }

    @Test public void customMachinePropertiesKeepDrillAndBothTurretsNonSolid() {
        assertFalse(new ElectricDrillBlock().defaultBlockState().canOcclude());
        assertFalse(new LaserTurretBlock(false).defaultBlockState().canOcclude());
        assertFalse(new LaserTurretBlock(true).defaultBlockState().canOcclude());
    }

    @Test public void hiddenPumpPipeRemainsNonSolidAndSolarPanelRemainsSolid() {
        assertFalse(new PumpPipeBlock().defaultBlockState().canOcclude());
        PhotovoltaicGeneratorBlock panel = new PhotovoltaicGeneratorBlock();
        assertTrue("The 1.14 photovoltaic block explicitly overrides isSolid to true", panel.defaultBlockState().canOcclude());
        assertTrue(panel.defaultBlockState().isRedstoneConductor(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO));
    }
}
