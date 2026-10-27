package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;

public class PhotovoltaicGeneratorBlock extends ElectricGeneratorBlock{

	public PhotovoltaicGeneratorBlock(){
		super(net.minecraft.block.Block.Properties.of(Material.STONE)
				.strength(0.75F).sound(SoundType.GLASS).dynamicShape()
                .isRedstoneConductor((state, world, pos) -> true));
	}
	
	@Override
	public ElectricGeneratorTileEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new PhotovoltaicGeneratorTileEntity();
	}
	


}
