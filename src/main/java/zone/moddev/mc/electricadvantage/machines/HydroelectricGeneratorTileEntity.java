package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.entities.HydroturbineEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.Random;

public class HydroelectricGeneratorTileEntity extends ElectricGeneratorTileEntity {

	
	public static final float ENERGY_PER_TICK = 4;
	
	private static final long checkInterval = 101;
	private static final Random initRand = new Random();
	private final long checkOffset = initRand.nextInt((int)checkInterval);
	
	private boolean isInitialized;
	
	public HydroelectricGeneratorTileEntity() {
		super(zone.moddev.mc.electricadvantage.init.Entities.HYDROELECTRIC_GENERATOR, "tile.electricadvantage.hydroelectric_generator.name", 0);
		isInitialized = false;
	}

	@Override
	public void tickUpdate(boolean isServer) {
		if(isServer){
			if(!isInitialized){
				initialize();
			}
			if((getLevel().getGameTime() + checkOffset) % checkInterval == 0){
				// periodically check if the hydroturbine entity is still present
				List turbines = getLevel().getEntitiesOfClass(HydroturbineEntity.class, new AxisAlignedBB(
						getBlockPos().getX(),getBlockPos().getY()-1,getBlockPos().getZ(),
						getBlockPos().getX()+1,getBlockPos().getY(),getBlockPos().getZ()+1));
				if(turbines == null || turbines.isEmpty()){
					// turbine entity got deleted
					initialize();
				}
			}
		}
	}
	
	private void initialize() {
		isInitialized = true;
		if(getBlockPos().getY() > 0){
			getLevel().addFreshEntity(new HydroturbineEntity(getLevel(),this));
		}
	}

	@Override
	public float getPowerOutput() {
		if(this.isActive()){
			return 15;
		} else {
			return 0;
		}
	}

	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return false;
	}

	@Override
	protected void saveTo(CompoundNBT tagRoot) {
		tagRoot.putBoolean("initDone", this.isInitialized);
	}

	@Override
	protected void loadFrom(CompoundNBT tagRoot) {
		if(tagRoot.contains("initDone", 1)){
			this.isInitialized = tagRoot.getBoolean("initDone");
		}
	}

	final int[] dataArray = new int[1];
	@Override
	public int[] getDataFieldArray() {
		return dataArray;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataArray[0]), getEnergyType());
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataArray[0] = Float.floatToIntBits(getEnergy());
	}

	@Override
	public void setActive(boolean active){
		// make public
		super.setActive(active);
	}
}
