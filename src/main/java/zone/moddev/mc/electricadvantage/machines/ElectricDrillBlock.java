package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.GUIBlock;
import cyano.poweradvantage.api.ITypedConduit;
import cyano.poweradvantage.api.PowerConnectorContext;
import cyano.poweradvantage.api.PoweredEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry;

public class ElectricDrillBlock extends GUIBlock implements ITypedConduit {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final ConduitType[] types = {Power.ELECTRIC_POWER};

    public ElectricDrillBlock() {
        super(Block.Properties.create(Material.PISTON).hardnessAndResistance(0.75F).variableOpacity().notSolid());
        setDefaultState(getStateContainer().getBaseState()
                .with(ACTIVE, false)
                .with(FACING, Direction.DOWN));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING);
    }

    @Override
    public ElectricMachineTileEntity createNewTileEntity(IBlockReader world) {
        return new ElectricDrillTileEntity();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, world, pos, oldState, isMoving);
        if (state.getBlock() == oldState.getBlock()) return;
        ConduitRegistry.getInstance().conduitBlockPlacedEvent(
                world, world.dimension.getType().getId(), pos, Power.ELECTRIC_POWER);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            ConduitRegistry.getInstance().conduitBlockRemovedEvent(
                    world, world.dimension.getType().getId(), pos, Power.ELECTRIC_POWER);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IInventory) {
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) tile);
                world.updateComparatorOutputLevel(pos, this);
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        ConduitRegistry.getInstance().conduitBlockRemovedEvent(
                world, world.dimension.getType().getId(), pos, Power.ELECTRIC_POWER);
        super.onExplosionDestroy(world, pos, explosion);
    }

    @Override
    public ConduitType[] getTypes() {
        return types;
    }

    @Override
    public boolean isPowerSink(ConduitType type) {
        return true;
    }

    @Override
    public boolean isPowerSource(ConduitType type) {
        return false;
    }

    @Override
    public boolean canAcceptConnection(PowerConnectorContext connection) {
        return ConduitType.areSameType(Power.ELECTRIC_POWER, connection.powerType);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getFace().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state,
                                LivingEntity placer, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof PoweredEntity) {
                ((PoweredEntity) tile).setCustomInventoryName(stack.getDisplayName().getString());
            }
        }
    }

    /** Decodes all six legacy facings; bit 3 is the active flag. */
    public BlockState getStateFromLegacyMeta(int meta) {
        return getDefaultState()
                .with(FACING, Direction.byIndex(meta & 0x7))
                .with(ACTIVE, (meta & 0x8) != 0);
    }

    public int getLegacyMetaFromState(BlockState state) {
        return state.get(FACING).getIndex() | (state.get(ACTIVE) ? 0x8 : 0);
    }

    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof ElectricMachineTileEntity
                ? ((ElectricMachineTileEntity) tile).getComparatorOutput() : 0;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }
}
