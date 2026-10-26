package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.electricadvantage.util.farming.VirtualCrop;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;
import java.util.List;

public class GrowthChamberTileEntity extends ElectricMachineTileEntity {

	private static final int TICKS_PER_GROWTH = 100;
	private static final float ENERGY_PER_TICK = 1;
	private static final int GROWTH_AREA = 3;
	private final int[] progression = new int[GROWTH_AREA];
	private final int[] progressionMax = new int[GROWTH_AREA];
	private final VirtualCrop[] crops = new VirtualCrop[GROWTH_AREA];
	private byte[] pendingGrowth;
	
	public GrowthChamberTileEntity() {
		super(zone.moddev.mc.electricadvantage.init.Entities.GROWTH_CHAMBER, "tile.electricadvantage.growth_chamber.name",Power.GROWTHCHAMBER_POWER, ENERGY_PER_TICK*64, GROWTH_AREA, 6, 0);
	}


	@Override
	public void tickUpdate(boolean isServer) {
		if(isServer){
			restorePendingCrops();
			boolean[] recalculate = inventoryChanged();
			boolean active = false;
			boolean flagSync = false;
			for(int slot = 0; slot < GROWTH_AREA; slot++){
				if(recalculate[slot] || progression[slot] >= progressionMax[slot]){
					crops[slot] = getVirtualCrop(getInputSlot(slot));
					progression[slot] = 0;
					if(crops[slot] != null){
						progressionMax[slot] = crops[slot].getMaxGrowth() * TICKS_PER_GROWTH;
					} else {
						progressionMax[slot] = 0;
					}
					flagSync = true;
				}
			}
			if(hasRedstoneSignal() || getEnergy(Power.GROWTHCHAMBER_POWER) < ENERGY_PER_TICK){
				active = false;
				for(int slot = 0; slot < GROWTH_AREA; slot++){
					if(progression[slot] > 0){
						progression[slot] = 0;
						flagSync = true;
					}
				}
			}else{
				for(int slot = 0; slot < GROWTH_AREA; slot++){
					if(crops[slot] != null){
						active = true;
						if(getEnergy(Power.GROWTHCHAMBER_POWER) < ENERGY_PER_TICK) break;
						progression[slot]++;
						subtractEnergy(ENERGY_PER_TICK,Power.GROWTHCHAMBER_POWER);
						if(progression[slot] % TICKS_PER_GROWTH == 0){
							if(crops[slot].grow()){
								// Growth done!
								List<ItemStack> harvest = crops[slot].getHarvest();
								if(harvest != null && (!harvest.isEmpty()) 
										&& this.hasSpaceForItemInOutputSlots(harvest.get(0))){
									playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, SoundEvents.BLOCK_GRASS_BREAK, 0.35f, 1f);
									for(ItemStack i : harvest){
										ItemStack remainder = this.insertItemToOutputSlots(i);
										if(remainder != null){
											getWorld().addEntity(
													new ItemEntity(
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
									seed.shrink(1);
									if(seed.isEmpty()){
										setInputSlot(slot,null);
									}
								}
								progression[slot] = 0;
								progressionMax[slot] = 0;
							}
							if(crops[slot] != null){
								progressionMax[slot] = crops[slot].getMaxGrowth() * TICKS_PER_GROWTH;
							} else {
								progressionMax[slot] = 0;
							}
						}
					} else {
						progression[slot] = 0;
						progressionMax[slot] = 0;
					}
				}

			}
			if(flagSync){
				this.sync();
			}
			this.setActiveState(active);
		}
	}


	
	
	private final int[] dataArray = new int[1+GROWTH_AREA*2];
	@Override
	public int[] getDataFieldArray() {
		return dataArray;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataArray[0]), this.getEnergyType());
		System.arraycopy(dataArray, 1, progression, 0, progression.length);
		System.arraycopy(dataArray, 1+GROWTH_AREA, progressionMax, 0, progressionMax.length);
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataArray[0] = Float.floatToIntBits(this.getEnergy(Power.GROWTHCHAMBER_POWER));
		System.arraycopy(progression, 0, dataArray, 1, progression.length);
		System.arraycopy(progressionMax, 0, dataArray, 1+GROWTH_AREA, progressionMax.length);
	}


	@Override
	public boolean isPowered() {
		return this.getEnergy(Power.GROWTHCHAMBER_POWER) > ENERGY_PER_TICK;
	}


	private float[] progressBars = new float[GROWTH_AREA];
	@Override
	public float[] getProgress() {
		for(int i = 0; i < progression.length; i++){
			if(progressionMax[i] <= 0){
				progressBars[i] = 0;
			} else {
				progressBars[i] = Math.max(0.125976563F,(float)progression[i] / (float)progressionMax[i]);
			}
		}
		return progressBars;
	}


	@Override
	protected void saveTo(CompoundNBT tagRoot) {
		byte[] growthStages = pendingGrowth == null ? new byte[GROWTH_AREA]
				: Arrays.copyOf(pendingGrowth, GROWTH_AREA);
		for(int i = 0; i < growthStages.length; i++){
			if(crops[i] != null)
				growthStages[i] = crops[i].getCurrentGrowth();
		}
		tagRoot.putByteArray("growth", growthStages);
		tagRoot.putIntArray("prog", progression);
	}


	@Override
	protected void loadFrom(CompoundNBT tagRoot) {
		Arrays.fill(crops, null);
		Arrays.fill(progression, 0);
		Arrays.fill(progressionMax, 0);
		oldInventory = null;
		pendingGrowth = tagRoot.contains("growth", 7)
				? Arrays.copyOf(tagRoot.getByteArray("growth"), GROWTH_AREA) : null;
		if(tagRoot.contains("prog", 11)){
			int[] saved = tagRoot.getIntArray("prog");
			System.arraycopy(saved, 0, progression, 0, Math.min(saved.length, progression.length));
		}
	}

	private void restorePendingCrops() {
		if (pendingGrowth == null) return;
		for (int i = 0; i < GROWTH_AREA; i++) {
			crops[i] = getVirtualCrop(getInputSlot(i));
			if (crops[i] != null) {
				crops[i].setCurrentGrowth(pendingGrowth[i]);
				progressionMax[i] = crops[i].getMaxGrowth() * TICKS_PER_GROWTH;
			} else {
				progression[i] = 0;
			}
		}
		// Seed the comparison cache so the first tick does not discard restored work.
		ItemStack[] inventory = getInventory();
		oldInventory = new ItemStack[inventory.length];
		for (int i = 0; i < inventory.length; i++) oldInventory[i] = copyStackOrNull(inventory[i]);
		pendingGrowth = null;
	}


	private VirtualCrop getVirtualCrop(ItemStack inputSlot) {
		if(isEmptyStack(inputSlot)) return null;
		try{
			return VirtualCrop.createVirtualCrop(inputSlot, getWorld(), getPos());
		} catch(Exception ex){
			ElectricAdvantage.LOGGER.error("Error occurred while trying to make a virtual crop for {}", inputSlot, ex);
			return null;
		}
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
				oldInventory[i] = copyStackOrNull(newInventory[i]);
			}
			Arrays.fill(changes, true);
			return changes;
		}
		for(int i = 0; i < oldInventory.length; i++){
			changes[i] = !areItemsEqualNullable(oldInventory[i], newInventory[i]);
		}
		for(int i = 0; i < oldInventory.length; i++){
			oldInventory[i] = copyStackOrNull(newInventory[i]);
		}
		return changes;
	}

	@Override
	public boolean isPowerSink(ConduitType t){
		return ConduitType.areSameType(Power.GROWTHCHAMBER_POWER,t);
	}

	@Override
	public PowerRequest getPowerRequest(ConduitType t){
		if( ConduitType.areSameType(Power.GROWTHCHAMBER_POWER,t)){
			return new PowerRequest(
					PowerRequest.MEDIUM_PRIORITY,
					this.getEnergyCapacity(Power.GROWTHCHAMBER_POWER) - this.getEnergy(Power.GROWTHCHAMBER_POWER),
					this
			);
		}
		return PowerRequest.REQUEST_NOTHING;
	}
}
