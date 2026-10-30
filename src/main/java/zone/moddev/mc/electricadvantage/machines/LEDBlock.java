package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import zone.moddev.mc.electricadvantage.blocks.ElectricConduitBlock;

/** Electric conduit that consumes a small amount of power to emit light. */
public class LEDBlock extends ElectricConduitBlock implements ITileEntityProvider {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public LEDBlock() {
        super();
        registerDefaultState(getStateDefinition().any()
                .setValue(WEST, false).setValue(DOWN, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(UP, false).setValue(NORTH, false)
                .setValue(LIT, false));
    }

    @Override
    public TileEntity newBlockEntity(IBlockReader world) {
        return new LEDTileEntity();
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, net.minecraft.util.math.BlockPos pos) {
        return state.getBlock() == this && state.getValue(LIT) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    public BlockState getStateFromLegacyMeta(int meta) {
        return defaultBlockState().setValue(LIT, (meta & 1) != 0);
    }

    public int getLegacyMetaFromState(BlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public boolean isPowerSink(ConduitType type) {
        return true;
    }
}
