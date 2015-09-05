package cyano.electricadvantage.machines;

import java.util.Arrays;
import java.util.List;

import cyano.electricadvantage.init.Power;
import cyano.electricadvantage.util.farming.VirtualCrop;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class GrowthChamberTileEntity extends ElectricMachineTileEntity {

	private static final int TICKS_PER_GROWTH = 50;
	private static final float ENERGY_PER_TICK = 1;
	private static final int GROWTH_AREA = 3;
	private final int[] progression = new int[GROWTH_AREA];
	private final VirtualCrop[] crops = new VirtualCrop[GROWTH_AREA];
	
	public GrowthChamberTileEntity() {
		super(GrowthChamberTileEntity.class.getName(),Power.GROWTHCHAMBER_POWER, 32, GROWTH_AREA, 6, 0);
	}


	@Override
	public void tickUpdate(boolean isServer) {
		if(isServer){
			boolean[] recalculate = inventoryChanged();
			boolean active = false;
			boolean flagSync = false;
			if(hasRedstoneSignal() || getEnergy() < ENERGY_PER_TICK){
				for(int slot = 0; slot < GROWTH_AREA; slot++){
					if(progression[slot] > 0){
						progression[slot] = 0;
						flagSync = true;
					}
				}
			}else{
				for(int slot = 0; slot < GROWTH_AREA; slot++){
					if(recalculate[slot]){
						crops[slot] = getVirtualCrop(getInputSlot(slot));
						progression[slot] = 0;
						flagSync = true;
					}
					if(crops[slot] != null){
						active = true;
						progression[slot]++;
						subtractEnergy(ENERGY_PER_TICK, getType());
						if(progression[slot] >= TICKS_PER_GROWTH){
							if(crops[slot].grow()){
								// Growth done!
								List<ItemStack> harvest = crops[slot].getHarvest();
								if(harvest != null && (!harvest.isEmpty()) 
										&& this.hasSpaceForItemInOutputSlots(harvest.get(0))){
									for(ItemStack i : harvest){
										ItemStack remainder = this.insertItemToOutputSlots(i);
										if(remainder != null){
											getWorld().spawnEntityInWorld(
													new EntityItem(
															getWorld(),
															2*(getWorld().rand.nextDouble()-0.5),
															2*(getWorld().rand.nextDouble()-0.5),
															2*(getWorld().rand.nextDouble()-0.5),
															remainder
													)
											);
										}
									}
									crops[slot] = null;
									ItemStack seed = getInputSlot(slot);
									seed.stackSize--;
									if(seed.stackSize <= 0){
										setInputSlot(slot,null);
									}
								}
							}
							progression[slot] = 0;
						}
					} else {
						progression[slot] = 0;
					}
				}
			}
			if(flagSync){
				this.sync();
			}
			this.setActiveState(active);
		}
	}
	
	private final int[] dataArray = new int[1+GROWTH_AREA];
	@Override
	public int[] getDataFieldArray() {
		return dataArray;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataArray[0]), this.getType());
		System.arraycopy(dataArray, 1, progression, 0, progression.length);
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataArray[0] = Float.floatToIntBits(this.getEnergy());
		System.arraycopy(progression, 0, dataArray, 1, progression.length);
	}


	@Override
	public boolean isPowered() {
		return this.getEnergy() > ENERGY_PER_TICK;
	}


	private float[] progressBars = new float[GROWTH_AREA];
	@Override
	public float[] getProgress() {
		for(int i = 0; i < progression.length; i++){
			if(crops[i] == null){
				progressBars[i] = 0;
			} else {
				progressBars[i] = Math.max(0.125976563F,(float)crops[i].getCurrentGrowth() / (float)crops[i].getMaxGrowth());
			}
		}
		return progressBars;
	}


	@Override
	protected void saveTo(NBTTagCompound tagRoot) {
		byte[] growthStages = new byte[GROWTH_AREA];
		for(int i = 0; i < growthStages.length; i++){
			growthStages[i] = crops[i].getCurrentGrowth();
		}
		tagRoot.setByteArray("growth", growthStages);
		tagRoot.setIntArray("prog", progression);
	}


	@Override
	protected void loadFrom(NBTTagCompound tagRoot) {
		if(tagRoot.hasKey("growth")){
			byte[] growthStages = tagRoot.getByteArray("growth");
			for(int i = 0; i < growthStages.length && i < GROWTH_AREA; i++){
				if(crops[i] == null){
					crops[i] = getVirtualCrop(this.getInputSlot(i));
				}
				if(crops[i] != null){
					crops[i].setCurrentGrowth(growthStages[i]);
				}
			}
		}
		if(tagRoot.hasKey("prog")){
			System.arraycopy(tagRoot.getIntArray("prog"), 0, progression, 0, progression.length);
		}
	}


	private VirtualCrop getVirtualCrop(ItemStack inputSlot) {
		return VirtualCrop.createVirtualCrop(inputSlot, getWorld(), getPos());
	}


	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return getVirtualCrop(item) != null;
	}


	private ItemStack[] oldInventory = null;
	private boolean[] inventoryChanged(){
		ItemStack[] newInventory = getInventory();
		boolean[] changes = new boolean[newInventory.length];
		if(oldInventory == null){
			oldInventory = new ItemStack[newInventory.length];
			for(int i = 0; i < oldInventory.length; i++){
				oldInventory[i] = (newInventory[i] == null ? null : newInventory[i].copy());
			}
			Arrays.fill(changes, true);
			return changes;
		}
		for(int i = 0; i < oldInventory.length; i++){
			changes[i] = !ItemStack.areItemStacksEqual(oldInventory[i], newInventory[i]);
		}
		for(int i = 0; i < oldInventory.length; i++){
			oldInventory[i] = (newInventory[i] == null ? null : newInventory[i].copy());
		}
		return changes;
	}
}
