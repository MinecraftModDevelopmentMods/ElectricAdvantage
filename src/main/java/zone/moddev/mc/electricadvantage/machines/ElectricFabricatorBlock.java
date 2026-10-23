package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.world.World;

public class ElectricFabricatorBlock extends ElectricMachineBlock{

	@Override
	public ElectricMachineTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new ElectricFabricatorTileEntity();
	}

}
