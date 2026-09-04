package com.mcmoddev.electricadvantage.machines;

import com.mcmoddev.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerConnectorContext;
import com.mcmoddev.poweradvantage.conduitnetwork.ConduitRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class SteamPoweredElectricGeneratorBlock extends ElectricGeneratorBlock{

	public static final ConduitType STEAM_POWER = new ConduitType("steam");
	
	@Override
	public ElectricGeneratorTileEntity createNewTileEntity(World arg0, int arg1) {
		return new SteamPoweredElectricGeneratorTileEntity();
	}

	///// Overrides to make this a multi-type block /////
	
	/**
	 * This method is called whenever the block is placed into the world
	 */
	@Override
	public void onBlockAdded(World w, BlockPos coord, IBlockState state){
		super.onBlockAdded(w, coord, state);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(w, w.provider.getDimension(), coord, STEAM_POWER);
	}
	
	/**
	 * This method is called when the block is removed from the world by an entity.
	 */
	@Override
	public void onPlayerDestroy(World w, BlockPos coord, IBlockState state){
		super.onPlayerDestroy(w, coord, state);
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimension(), coord,STEAM_POWER);
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void onBlockExploded(World w, BlockPos coord, Explosion boom){
		super.onBlockExploded(w, coord, boom);
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, w.provider.getDimension(), coord, STEAM_POWER);
	}



	@Override
	public boolean canAcceptConnection(PowerConnectorContext c){
		return ConduitType.areSameType(Power.ELECTRIC_POWER,c.powerType)
				|| ConduitType.areSameType(STEAM_POWER,c.powerType);
	}
	private final ConduitType[] types = {Power.ELECTRIC_POWER,STEAM_POWER};
	@Override
	public ConduitType[] getTypes(){
		return types;
	}

	@Override
	public boolean isPowerSink(ConduitType pt){
		return ConduitType.areSameType(STEAM_POWER,pt);
	}
	@Override
	public boolean isPowerSource(ConduitType pt){
		return ConduitType.areSameType(Power.ELECTRIC_POWER,pt);
	}

	///// end multi-type overrides /////
	
}
