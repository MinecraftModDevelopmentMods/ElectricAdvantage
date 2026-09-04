package com.mcmoddev.electricadvantage.machines;

import com.mcmoddev.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerConnectorContext;
import com.mcmoddev.poweradvantage.conduitnetwork.ConduitRegistry;
import com.mcmoddev.poweradvantage.init.Fluids;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class PlasticRefineryBlock extends ElectricMachineBlock{

	@Override
	public ElectricMachineTileEntity createNewTileEntity(World w, int m) {
		return new PlasticRefineryTileEntity();
	}
	
///// Overrides to make this a multi-type block /////
	
	/**
	 * This method is called whenever the block is placed into the world
	 */
	@Override
	public void onBlockAdded(World w, BlockPos coord, IBlockState state){
		super.onBlockAdded(w, coord, state);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, com.mcmoddev.poweradvantage.init.Fluids.fluidConduit_general);
	}
	
	/**
	 * This method is called when the block is removed from the world by an entity.
	 */
	@Override
	public void onPlayerDestroy(World w, BlockPos coord, IBlockState state){
		super.onPlayerDestroy(w, coord, state);
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimension(), coord, com.mcmoddev.poweradvantage.init.Fluids.fluidConduit_general);
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void onBlockExploded(World w, BlockPos coord, Explosion boom){
		super.onBlockExploded(w, coord, boom);
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimension(), coord, Fluids.fluidConduit_general);
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
