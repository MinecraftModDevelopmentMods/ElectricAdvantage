package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerConnectorContext;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.poweradvantage.init.Fluids;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * The coal boiler is a bit more complicated because it consumes fluids (water) and produces steam,
 * making it a multi-type powered block.
 * @author DrCyano
 *
 */
public class GrowthChamberControllerBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{


	public GrowthChamberControllerBlock() {
		super(Material.PISTON, 0.75f, Power.GROWTHCHAMBER_POWER, Power.ELECTRIC_POWER, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new GrowthChamberControllerTileEntity();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, World world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof GrowthChamberControllerTileEntity){
			return ((GrowthChamberControllerTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}



	@Override
	public boolean isPowerSink(ConduitType pt){
		return ConduitType.areSameType(Power.ELECTRIC_POWER,pt)
				|| Fluids.isFluidType(pt);
	}
	@Override
	public boolean isPowerSource(ConduitType pt){
		return ConduitType.areSameType(Power.GROWTHCHAMBER_POWER,pt);
	}

	@Override
	public boolean canAcceptConnection(PowerConnectorContext c){
		return ConduitType.areSameType(Power.ELECTRIC_POWER,c.powerType)
				|| ConduitType.areSameType(Power.GROWTHCHAMBER_POWER, c.powerType)
				|| Fluids.isFluidType(c.powerType);
	}

	///// end multi-type overrides /////
}
