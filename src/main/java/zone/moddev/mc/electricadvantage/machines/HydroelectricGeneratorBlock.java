package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReaderBase;

public class HydroelectricGeneratorBlock extends ElectricGeneratorBlock {

	public HydroelectricGeneratorBlock(){
		super();
	}

	@Override
	public ElectricGeneratorTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new HydroelectricGeneratorTileEntity();
	}

	@Override
	public boolean isValidPosition(IBlockState state, IWorldReaderBase worldIn, BlockPos pos)
	{
		return super.isValidPosition(state, worldIn, pos)
				&& !worldIn.getBlockState(pos.down()).getMaterial().isSolid();
	}
}
