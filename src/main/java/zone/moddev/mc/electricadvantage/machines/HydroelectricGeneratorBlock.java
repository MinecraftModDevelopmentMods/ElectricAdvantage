package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class HydroelectricGeneratorBlock extends ElectricGeneratorBlock {

	public HydroelectricGeneratorBlock(){
		super();
	}

	@Override
	public ElectricGeneratorTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new HydroelectricGeneratorTileEntity();
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return super.isValidPosition(state, worldIn, pos)
				&& !worldIn.getBlockState(pos.down()).getMaterial().isSolid();
	}
}
