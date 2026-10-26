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
        setDefaultState(getStateContainer().getBaseState()
                .with(ACTIVE, false)
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING);
    }

    /** Decodes the 1.10/1.12 metadata used by world migration fixtures. */
    public BlockState getStateFromLegacyMeta(int meta) {
        Direction facing = Direction.byIndex(meta & 0x7);
        if (facing.getAxis() == Direction.Axis.Y) facing = Direction.NORTH;
        return getDefaultState().with(FACING, facing).with(ACTIVE, (meta & 0x8) != 0);
    }

    public int getLegacyMetaFromState(BlockState state) {
        return state.get(FACING).getIndex() | (state.get(ACTIVE) ? 0x8 : 0);
    }

    @Override
    public abstract ElectricGeneratorTileEntity createNewTileEntity(IBlockReader world);

    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof ElectricGeneratorTileEntity
                ? ((ElectricGeneratorTileEntity) tile).getComparatorOutput() : 0;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IInventory) {
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) tile);
                ((IInventory) tile).clear();
                world.updateComparatorOutputLevel(pos, this);
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
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
