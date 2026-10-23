package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.simple.BlockSimplePowerMachine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
        setDefaultState(getStateContainer().getBaseState()
                .with(ACTIVE, false)
                .with(POWERED, false)
                .with(FACING, EnumFacing.NORTH));
    }

    public ElectricMachineBlock(Material material) {
        this(material, Power.ELECTRIC_POWER);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(ACTIVE, FACING, POWERED);
    }

    /** Decodes the 1.10/1.12 metadata used by world migration fixtures. */
    public IBlockState getStateFromLegacyMeta(int meta) {
        return getDefaultState()
                .with(FACING, metaToFacing(meta))
                .with(ACTIVE, (meta & 0x4) != 0)
                .with(POWERED, (meta & 0x8) != 0);
    }

    /** Encodes the legacy metadata layout for migration tests. */
    public int getLegacyMetaFromState(IBlockState state) {
        int meta = facingToMeta(state.get(FACING));
        if (state.get(ACTIVE)) meta |= 0x4;
        if (state.get(POWERED)) meta |= 0x8;
        return meta;
    }

    private static int facingToMeta(EnumFacing facing) {
        switch (facing) {
            case WEST: return 1;
            case SOUTH: return 2;
            case EAST: return 3;
            default: return 0;
        }
    }

    private static EnumFacing metaToFacing(int meta) {
        switch (meta & 0x3) {
            case 1: return EnumFacing.WEST;
            case 2: return EnumFacing.SOUTH;
            case 3: return EnumFacing.EAST;
            default: return EnumFacing.NORTH;
        }
    }

    @Override
    public abstract ElectricMachineTileEntity createNewTileEntity(IBlockReader world);

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof ElectricMachineTileEntity
                ? ((ElectricMachineTileEntity) tile).getComparatorOutput() : 0;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public void onReplaced(IBlockState state, World world, BlockPos pos, IBlockState newState, boolean isMoving) {
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
