package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import net.minecraft.util.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class ElectricOvenTileEntity extends ElectricMachineTileEntity{


	public static final float ENERGY_PER_TICK = 4f;
	private final short totalCookTime = 100;
	private short progress = 0;
	
	public ElectricOvenTileEntity() {
		super(zone.moddev.mc.electricadvantage.init.Entities.ELECTRIC_OVEN, "tile.electricadvantage.electric_oven.name", 1, 1, 0);
	}

	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			boolean active = false;
			if(getInputSlot(0) == null || hasRedstoneSignal()){
				progress = 0;
			} else if(getEnergy() >= ENERGY_PER_TICK){
				ItemStack output = getSmeltingResult(getInputSlot(0));
				if(isFoodItem(output) && hasSpaceForItemInOutputSlots(output)){
					subtractEnergy(ENERGY_PER_TICK,Power.ELECTRIC_POWER);
					progress++;
					if(progress >= totalCookTime){
						this.insertItemToOutputSlots(output.copy());
						getInputSlot(0).shrink(1);
						if(getInputSlot(0).isEmpty()){
							setInputSlot(0,null);
						}
						progress = 0;
						playSoundEffect(getBlockPos().getX()+0.5, getBlockPos().getY()+0.5, getBlockPos().getZ()+0.5, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
					}
					active = true;
				} else {
					progress = 0;
				}
			} else {
				if(progress > 0) progress--;
			}
			this.setActiveState(active);
		}
		
	}
	
	public static boolean isFoodItem(ItemStack item){
		if(item == null) return false;
		Item i = item.getItem();
		return !item.isEmpty() && i.isEdible();
	}
	
	public boolean becomesFoodItem(ItemStack item){
		if(item == null) return false;
		return isFoodItem(getSmeltingResult(item));
		
	}
	
	
	@Override
	public boolean isPowered() {
		return getEnergy() > ENERGY_PER_TICK;
	}

	private final float[] progs = new float[1];
	@Override
	public float[] getProgress() {
		progs[0] = (float)progress / (float)totalCookTime;
		return progs;
	}

	@Override
	protected void saveTo(CompoundNBT tagRoot) {
		tagRoot.putShort("cookTime", progress);
	}

	@Override
	protected void loadFrom(CompoundNBT tagRoot) {
		if(tagRoot.contains("cookTime", 99)){
			progress = tagRoot.getShort("cookTime");
		}
	}

	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return becomesFoodItem(item);
	}

	final int[] dataArray = new int[2];
	@Override
	public int[] getDataFieldArray() {
		return dataArray;
	}

	@Override
	public void onDataFieldUpdate() {
		setEnergy(Float.intBitsToFloat(dataArray[0]),getEnergyType());
		progress = (short)dataArray[1];
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataArray[0] = Float.floatToIntBits(getEnergy());
		dataArray[1] = progress;
	}


}
