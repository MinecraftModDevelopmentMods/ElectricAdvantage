package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.world.World;

public class ElectricOvenBlock extends ElectricMachineBlock{
	
	public ElectricOvenBlock(){
		super();
	}

	@Override
	public ElectricMachineTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new ElectricOvenTileEntity();
	}

}
