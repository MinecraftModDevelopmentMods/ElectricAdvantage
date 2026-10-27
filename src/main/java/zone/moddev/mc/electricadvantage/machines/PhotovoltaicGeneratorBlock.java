package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
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
	public boolean isNormalCube(BlockState bs, net.minecraft.world.IBlockReader world, net.minecraft.util.math.BlockPos pos)
	{
		return true;
	}
	


}
