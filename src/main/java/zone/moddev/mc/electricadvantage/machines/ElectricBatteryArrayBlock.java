package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.ConduitType;
import net.minecraft.world.World;

public class ElectricBatteryArrayBlock extends ElectricGeneratorBlock {

	public ElectricBatteryArrayBlock(){
		super();
	}
	
	@Override
	public ElectricGeneratorTileEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new ElectricBatteryArrayTileEntity();
	}
	@Override
	public boolean isPowerSink(ConduitType e){
		return true;
	}

}
