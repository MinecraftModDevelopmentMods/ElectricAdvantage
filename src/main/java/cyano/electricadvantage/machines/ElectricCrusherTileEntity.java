package cyano.electricadvantage.machines;

import java.util.Arrays;

import cyano.basemetals.registry.CrusherRecipeRegistry;
import cyano.basemetals.registry.recipe.ICrusherRecipe;
import cyano.electricadvantage.init.Power;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;

public class ElectricCrusherTileEntity extends ElectricMachineTileEntity{
	

	public static final float ENERGY_PER_TICK = 12f;

	

	private short[] smashTime = new short[3];
	private final short totalSmashTime = 200;
	

	public ElectricCrusherTileEntity() {
		super(ElectricCrusherTileEntity.class.getSimpleName(), 
				3, 6, 0);
		Arrays.fill(smashTime, (short)0);
	}


	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			boolean active = false;
			for(int i = 0; i < this.numberOfInputSlots(); i++){
				if(getInputSlot(i) == null){
					smashTime[i] = 0;
					continue;
				}
				if(getEnergy() >= ENERGY_PER_TICK){
					if(canCrush(i)){
						subtractEnergy(ENERGY_PER_TICK,Power.ELECTRIC_POWER);
						smashTime[i]++;
						if(smashTime[i] >= totalSmashTime){
							doCrush(i);
							smashTime[i] = 0;
						}
						active = true;
					} else {
						smashTime[i] = 0;
					}
				} else {
					if(smashTime[i] > 0) smashTime[i]--;
				}
			}
			this.setActive(active && getEnergy() >= ENERGY_PER_TICK);
		}
	}


	short[] oldValues = null;
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		if(oldValues == null){
			oldValues = new short[smashTime.length];
		}
		if (areNotEqual(oldValues,smashTime)){
			System.arraycopy(smashTime, 0, oldValues, 0, smashTime.length);
			this.sync();
		}
	}
	
	private static boolean areNotEqual(short[] a, short[] b){
		if(a.length == b.length){
			for(int i = 0; i < a.length; i++){
				if(a[i] != b[i]) return true;
			}
			return false;
		}
		return true;
	}
	
	private boolean canCrush(int slot){
		ItemStack input = this.getInputSlot(slot);
		if(input == null) return false;
		ICrusherRecipe recipe = CrusherRecipeRegistry.getInstance().getRecipeForInputItem(input);
		if(recipe == null) return false;
		ItemStack output = recipe.getOutput();
		if(output == null) return false;
		return this.hasSpaceForItemInOutputSlots(output);
	}
	
	private void doCrush(int slot){
		ItemStack input = this.getInputSlot(slot);
		ItemStack output = CrusherRecipeRegistry.getInstance().getRecipeForInputItem(input).getOutput().copy();
		this.insertItemToOutputSlots(output);
		input.stackSize--;
		if(input.stackSize <= 0){
			setInputSlot(slot,null);
		}
	}
	
	private final float[] progress = new float[3];
	@Override
	public float[] getProgress() {
		for(int i = 0; i < progress.length; i++){
			progress[i] = (float)smashTime[i] / (float)totalSmashTime;
		}
		return progress;
	}
	
	
	@Override
	protected void saveTo(NBTTagCompound tagRoot) {
		NBTTagList times = new NBTTagList();
		for(int i = 0; i < numberOfInputSlots(); i++){
			times.appendTag(new NBTTagShort(smashTime[i]));
		}
		tagRoot.setTag("smashTime", times);
	}

	@Override
	protected void loadFrom(NBTTagCompound tagRoot) {
		NBTTagList times = tagRoot.getTagList("smashTime", 2);
		for(int i = 0; i < numberOfInputSlots(); i++){
			smashTime[i] = ((NBTTagShort)times.get(i)).getShort();
		}
	}

	@Override
	protected boolean isValidInputItem(ItemStack item) {
		if(item == null) return false;
		return CrusherRecipeRegistry.getInstance().getRecipeForInputItem(item) != null;
	}

	private int[] dataArray = null;
	@Override
	public int[] getDataFieldArray() {
		if(dataArray == null){
			dataArray = new int[numberOfInputSlots()];
		}
		return dataArray;
	}

	@Override
	public void onDataFieldUpdate() {
		int[] arr = getDataFieldArray();
		for(int i = 0; i < numberOfInputSlots(); i++){
			smashTime[i] = (short) arr[i];
		}
	}

	@Override
	public void prepareDataFieldsForSync() {
		int[] arr = getDataFieldArray();
		for(int i = 0; i < numberOfInputSlots(); i++){
			arr[i] = smashTime[i];
		}
	}

}