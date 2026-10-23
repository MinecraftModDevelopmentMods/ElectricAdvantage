package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
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
        setDefaultState(getStateContainer().getBaseState()
                .with(WEST, false).with(DOWN, false).with(SOUTH, false)
                .with(EAST, false).with(UP, false).with(NORTH, false)
                .with(LIT, false));
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader world) {
        return new LEDTileEntity();
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getBlock() == this && state.get(LIT) ? 15 : 0;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(LIT);
    }

    public IBlockState getStateFromLegacyMeta(int meta) {
        return getDefaultState().with(LIT, (meta & 1) != 0);
    }

    public int getLegacyMetaFromState(IBlockState state) {
        return state.get(LIT) ? 1 : 0;
    }

    @Override
    public boolean isPowerSink(ConduitType type) {
        return true;
    }
}
