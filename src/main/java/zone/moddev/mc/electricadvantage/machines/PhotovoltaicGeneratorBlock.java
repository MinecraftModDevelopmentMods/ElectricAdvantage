package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class PhotovoltaicGeneratorBlock extends ElectricGeneratorBlock{

	public PhotovoltaicGeneratorBlock(){
		super(net.minecraft.block.Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(0.75F).sound(SoundType.GLASS).variableOpacity());
	}
	
	@Override
	public ElectricGeneratorTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new PhotovoltaicGeneratorTileEntity();
	}
	

	@Override
	public boolean isFullCube(IBlockState bs)
	{
		return true;
	}

	@Override
	public boolean isSolid(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState bs)
	{
		return true;
	}


}
