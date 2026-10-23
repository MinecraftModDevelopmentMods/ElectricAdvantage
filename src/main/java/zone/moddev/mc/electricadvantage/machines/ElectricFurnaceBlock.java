package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.world.World;

public class ElectricFurnaceBlock extends ElectricMachineBlock{
	
	public ElectricFurnaceBlock(){
		super();
	}

	@Override
	public ElectricMachineTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new ElectricFurnaceTileEntity();
	}

}
