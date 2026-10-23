package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class GrowthChamberBlock extends ElectricMachineBlock{

	public GrowthChamberBlock(){
		super(Material.PISTON,Power.GROWTHCHAMBER_POWER);
	}

	@Override
	public ElectricMachineTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new GrowthChamberTileEntity();
	}
}
