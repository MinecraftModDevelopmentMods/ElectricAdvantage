package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerConnectorContext;
import zone.moddev.mc.poweradvantage.init.Fluids;

public class PlasticRefineryBlock extends ElectricMachineBlock{

	@Override
	public ElectricMachineTileEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new PlasticRefineryTileEntity();
	}

	@Override
	public boolean isPowerSource(ConduitType e){
		return false;
	}
	@Override
	public boolean isPowerSink(ConduitType e){
		return true;
	}
	private final ConduitType[] types = {Power.ELECTRIC_POWER,Fluids.fluidConduit_general};
	@Override
	public ConduitType[] getTypes(){
		return types;
	}
	@Override
	public boolean canAcceptConnection(PowerConnectorContext c){
		return ConduitType.areSameType(Power.ELECTRIC_POWER,c.powerType)
				|| Fluids.isFluidType(c.powerType);
	}
	///// end multi-type overrides /////

}
