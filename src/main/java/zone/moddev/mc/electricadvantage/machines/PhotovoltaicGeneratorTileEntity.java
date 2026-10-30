package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PhotovoltaicGeneratorTileEntity extends ElectricGeneratorTileEntity {

	private static final float PI = (float)Math.PI;
	private static final float SUM_OF_SINE_TICKS = 24000 / PI / 8;
	public static final float SOLAR_PER_DAY = 12500; // quarter the energy of a piece of coal per day
	public static final float MAX_SOLAR_OUTPUT = PI * SOLAR_PER_DAY / SUM_OF_SINE_TICKS;
	public static final float BLOCKLIGHT_FACTOR = 0.1f; // light from blocks less effective than sunlight
	private float powerOutput = 0;
	
	public PhotovoltaicGeneratorTileEntity() {
		super(zone.moddev.mc.electricadvantage.init.Entities.PHOTOVOLTAIC_GENERATOR, "tile.electricadvantage.photovoltaic_generator.name", 0);
	}

	@Override
	public float getPowerOutput() {
		return powerOutput;
	}
	
	@Override
	public int getComparatorOutput(){
		return (int)(15 * powerOutput);
	}


	@Override
	public boolean isActive(){
		return (hasRedstoneSignal() == false) && powerOutput > 0.0001;
	}
	
	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return false;
	}

	@Override
	protected void saveTo(CompoundNBT tagRoot) {
		// do nothing
	}

	@Override
	protected void loadFrom(CompoundNBT tagRoot) {
		// do nothing
	}

	final int[] dataArray = new int[1];
	@Override
	public int[] getDataFieldArray() {
		return dataArray;
	}

	@Override
	public void onDataFieldUpdate() {
		powerOutput = Float.intBitsToFloat(dataArray[0]);
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataArray[0] = Float.floatToIntBits(powerOutput);
	}

	@Override
	public void tickUpdate(boolean isServer) {
		// do nothing
	}
	float oldPower = 0;
	@Override
	public void powerUpdate(){
		float power;
		if(hasRedstoneSignal() == false){
			float solar = getLightIntensityAt(getPos(),getWorld());
			power = Math.max(0,MAX_SOLAR_OUTPUT * solar);
			this.addEnergy(power, Power.ELECTRIC_POWER);
		} else {
			power = 0f;
		}
		powerOutput = power / MAX_SOLAR_OUTPUT;
		if(powerOutput != oldPower){
			oldPower = powerOutput;
			this.sync();
		}
		
		super.powerUpdate();
	}
	
	public static float getLightIntensityAt(BlockPos pos, World w){
		// Sample incident light above the solid panel, not inside its body.
		BlockPos lightPos = pos.up();
		float light;
		if(w.canSeeSky(lightPos) && w.dimension.hasSkyLight()){
			// outdoor light imposes no penalty
			light = MathHelper.sin((float)w.getDayTime() * 2.61799E-4f); // (worldTime * pi / 12000);
			// handle weather
			if(w.isThundering()){
				light *= 0.2f;
			} else if(w.isRaining()){
				light *= 0.35f;
			}
		} else {
			float blockLight = w.getLight(lightPos) * 6.66666E-2f; // block light / 15
			light = BLOCKLIGHT_FACTOR * blockLight;
		}
		return light;
	}

}
