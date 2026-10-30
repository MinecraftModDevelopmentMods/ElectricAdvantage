package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.simple.BlockSimplePowerMachine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import zone.moddev.mc.electricadvantage.init.Power;

public abstract class ElectricGeneratorBlock extends BlockSimplePowerMachine {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public ElectricGeneratorBlock() {
        this(Material.PISTON);
    }

    public ElectricGeneratorBlock(Material material) {
        super(material, 0.75f, Power.ELECTRIC_POWER);
		initializeDefaultState();
	}

	protected ElectricGeneratorBlock(Block.Properties properties) {
		super(properties, Power.ELECTRIC_POWER);
		initializeDefaultState();
	}

	private void initializeDefaultState() {
        registerDefaultState(getStateDefinition().any()
                .setValue(ACTIVE, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING);
    }

    /** Decodes the 1.10/1.12 metadata used by world migration fixtures. */
    public BlockState getStateFromLegacyMeta(int meta) {
        Direction facing = Direction.from3DDataValue(meta & 0x7);
        if (facing.getAxis() == Direction.Axis.Y) facing = Direction.NORTH;
        return defaultBlockState().setValue(FACING, facing).setValue(ACTIVE, (meta & 0x8) != 0);
    }

    public int getLegacyMetaFromState(BlockState state) {
        return state.getValue(FACING).get3DDataValue() | (state.getValue(ACTIVE) ? 0x8 : 0);
    }

    @Override
    public abstract ElectricGeneratorTileEntity newBlockEntity(IBlockReader world);

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        return tile instanceof ElectricGeneratorTileEntity
                ? ((ElectricGeneratorTileEntity) tile).getComparatorOutput() : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IInventory) {
                InventoryHelper.dropContents(world, pos, (IInventory) tile);
                ((IInventory) tile).clearContent();
                world.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean isPowerSink(ConduitType type) {
        return false;
    }

    @Override
    public boolean isPowerSource(ConduitType type) {
        return true;
    }

    public ConduitType getType() {
        return Power.ELECTRIC_POWER;
    }
}
