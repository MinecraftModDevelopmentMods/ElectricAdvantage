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

public abstract class ElectricMachineBlock extends BlockSimplePowerMachine {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ElectricMachineBlock() {
        this(Material.PISTON);
    }

    public ElectricMachineBlock(Material material, ConduitType type) {
        super(material, 0.75f, type);
		initializeDefaultState();
	}

	protected ElectricMachineBlock(Block.Properties properties) {
		super(properties, Power.ELECTRIC_POWER);
		initializeDefaultState();
	}

	private void initializeDefaultState() {
        registerDefaultState(getStateDefinition().any()
                .setValue(ACTIVE, false)
                .setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH));
    }

    public ElectricMachineBlock(Material material) {
        this(material, Power.ELECTRIC_POWER);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING, POWERED);
    }

    /** Decodes the 1.10/1.12 metadata used by world migration fixtures. */
    public BlockState getStateFromLegacyMeta(int meta) {
        return defaultBlockState()
                .setValue(FACING, metaToFacing(meta))
                .setValue(ACTIVE, (meta & 0x4) != 0)
                .setValue(POWERED, (meta & 0x8) != 0);
    }

    /** Encodes the legacy metadata layout for migration tests. */
    public int getLegacyMetaFromState(BlockState state) {
        int meta = facingToMeta(state.getValue(FACING));
        if (state.getValue(ACTIVE)) meta |= 0x4;
        if (state.getValue(POWERED)) meta |= 0x8;
        return meta;
    }

    private static int facingToMeta(Direction facing) {
        switch (facing) {
            case WEST: return 1;
            case SOUTH: return 2;
            case EAST: return 3;
            default: return 0;
        }
    }

    private static Direction metaToFacing(int meta) {
        switch (meta & 0x3) {
            case 1: return Direction.WEST;
            case 2: return Direction.SOUTH;
            case 3: return Direction.EAST;
            default: return Direction.NORTH;
        }
    }

    @Override
    public abstract ElectricMachineTileEntity newBlockEntity(IBlockReader world);

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        return tile instanceof ElectricMachineTileEntity
                ? ((ElectricMachineTileEntity) tile).getComparatorOutput() : 0;
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
        return true;
    }

    @Override
    public boolean isPowerSource(ConduitType type) {
        return false;
    }

    public ConduitType getType() {
        return Power.ELECTRIC_POWER;
    }
}
