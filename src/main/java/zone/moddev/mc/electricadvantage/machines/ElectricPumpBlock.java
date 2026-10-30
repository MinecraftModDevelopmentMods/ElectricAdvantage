package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Blocks;
import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerConnectorContext;
import zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.poweradvantage.init.Fluids;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ElectricPumpBlock extends ElectricMachineBlock{

	@Override
	public ElectricMachineTileEntity newBlockEntity(net.minecraft.world.IBlockReader world) {
		return new ElectricPumpTileEntity();
	}
	
///// Overrides to make this a multi-type block /////
	
	/**
	 * This method is called whenever the block is placed into the world
	 */
	@Override
	public void onRemove(BlockState state, World w, BlockPos coord, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) destroyPipe(w, coord);
		super.onRemove(state, w, coord, newState, isMoving);
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void wasExploded(World w, BlockPos coord, Explosion boom){
		destroyPipe(w,coord);
		super.wasExploded(w, coord, boom);
	}
	
	
	private void destroyPipe(World w, BlockPos coord) {
		if(w.isClientSide) return;
		// destroy connected drill bits
		BlockPos c = coord.below();
		while(c.getY() > 0 && w.getBlockState(c).getBlock() == Blocks.pump_pipe_electric){
			w.setBlockAndUpdate(c, net.minecraft.block.Blocks.AIR.defaultBlockState());
			c = c.below();
		}
	}


	@Override
	public boolean canAcceptConnection(PowerConnectorContext connection){
		return  ConduitType.areSameType(getType(), connection.powerType) || ConduitType.areSameType(Fluids.fluidConduit_general, connection.powerType);
	}


	private final ConduitType[] types = {Power.ELECTRIC_POWER, Fluids.fluidConduit_general};

	@Override
	public ConduitType[] getTypes(){
		return types;
	}/**
	 * Determines whether this block/entity should receive energy
	 * @return true if this block/entity should receive energy
	 */
	@Override
	public boolean isPowerSink(ConduitType p){
		return ConduitType.areSameType(Power.ELECTRIC_POWER,p);
	}
	/**
	 * Determines whether this block/entity can provide energy
	 * @return true if this block/entity can provide energy
	 */
	@Override
	public boolean isPowerSource(ConduitType p){
		return Fluids.isFluidType(p);
	}
	
	///// end multi-type overrides /////

}
