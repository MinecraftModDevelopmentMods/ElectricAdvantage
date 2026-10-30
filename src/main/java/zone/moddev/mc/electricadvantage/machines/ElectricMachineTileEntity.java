package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.item.crafting.IRecipeType;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;

import java.util.Arrays;

public abstract class ElectricMachineTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine{

	private final ItemStack[] inventory;
	private final int[] inputSlots;
	private final int[] outputSlots;
	private final int[] ioSlots;
	private final int[] otherSlots;
	
	
	
	public ElectricMachineTileEntity(TileEntityType<?> tileType, String name,float bufferSize,int numInputSlots, int numOutputSlots, int numOtherSlots) {
		this(tileType,name,Power.ELECTRIC_POWER,bufferSize,numInputSlots,numOutputSlots,numOtherSlots);
	}
	
	public ElectricMachineTileEntity(TileEntityType<?> tileType, String name, ConduitType powerType, float bufferSize,int numInputSlots, int numOutputSlots, int numOtherSlots) {
		super(tileType, powerType, bufferSize, name);
		inventory = new ItemStack[numInputSlots+numOutputSlots+numOtherSlots];
		inputSlots = new int[numInputSlots];
		outputSlots = new int[numOutputSlots];
		ioSlots = new int[numInputSlots+numOutputSlots];
		otherSlots = new int[numOtherSlots];
		for(int i = 0; i < inventory.length; i++){
			if(i < inputSlots.length)inputSlots[i] = i;
			if(i < outputSlots.length)outputSlots[i] = inputSlots.length + i;
			if(i < ioSlots.length)ioSlots[i] = i;
			if(i < otherSlots.length)otherSlots[i] = inputSlots.length + outputSlots.length + i;
		}
	}
	public ElectricMachineTileEntity(TileEntityType<?> tileType, String name,int numInputSlots, int numOutputSlots, int numOtherSlots) {
		this(tileType,name,500f,numInputSlots,numOutputSlots,numOtherSlots);
	}
	public ElectricMachineTileEntity(TileEntityType<?> tileType, String name,int numInputSlots, int numOutputSlots, int numOtherSlots, ConduitType[] energyTypes, float[] energyBuffers) {
		super(tileType, energyTypes, energyBuffers, name);
		inventory = new ItemStack[numInputSlots+numOutputSlots+numOtherSlots];
		inputSlots = new int[numInputSlots];
		outputSlots = new int[numOutputSlots];
		ioSlots = new int[numInputSlots+numOutputSlots];
		otherSlots = new int[numOtherSlots];
		for(int i = 0; i < inventory.length; i++){
			if(i < inputSlots.length)inputSlots[i] = i;
			if(i < outputSlots.length)outputSlots[i] = inputSlots.length + i;
			if(i < ioSlots.length)ioSlots[i] = i;
			if(i < otherSlots.length)otherSlots[i] = inputSlots.length + outputSlots.length + i;
		}
	}
	
	private boolean redstone = false;
	private boolean redstoneInitialized = false;
	private float oldEnergy = 0f;
	private int[] syncArrayOld = null;
	private int[] syncArrayNew = null;
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		
		redstone = getWorld().isBlockPowered(getPos());
		redstoneInitialized = true;
		
		this.setPowerState(this.isPowered());
		
		// automatically detect when a sync is needed
		if(syncArrayOld == null || syncArrayNew == null 
				|| this.getDataFieldArray().length != syncArrayOld.length){
			int size = this.getDataFieldArray().length;
			syncArrayOld = new int[size];
			syncArrayNew = new int[size];
		}
		this.prepareDataFieldsForSync();
		System.arraycopy(this.getDataFieldArray(), 0, syncArrayNew, 0, syncArrayNew.length);
		if(!Arrays.equals(syncArrayOld, syncArrayNew)){
			this.sync();
			System.arraycopy(syncArrayNew, 0, syncArrayOld, 0, syncArrayOld.length);
		}
	}

	
	protected boolean hasRedstoneSignal(){
		// tickUpdate can run before the first scheduled powerUpdate after loading.
		if(!redstoneInitialized && getWorld() != null && !getWorld().isRemote){
			redstone = getWorld().isBlockPowered(getPos());
			redstoneInitialized = true;
		}
		return redstone;
	}

	
	protected void setActiveState(boolean active){
		BlockState oldState = getWorld().getBlockState(getPos());
		if(oldState.getBlock() instanceof ElectricMachineBlock 
				&& (Boolean)oldState.get(ElectricMachineBlock.ACTIVE) != active ){
			BlockState newState = oldState.with(ElectricMachineBlock.ACTIVE, active);
			getWorld().setBlockState(getPos(), newState, 3);
		}
	}
	protected void setPowerState(boolean powered){
		BlockState oldState = getWorld().getBlockState(getPos());
		if(oldState.getBlock() instanceof ElectricMachineBlock 
				&& (Boolean)oldState.get(ElectricMachineBlock.POWERED) != powered ){
			BlockState newState = oldState.with(ElectricMachineBlock.POWERED, powered);
			getWorld().setBlockState(getPos(), newState, 3);
		}
	}

	
	public boolean isActive(){
		return isPowered() && (!this.hasRedstoneSignal() && (this.getEnergy(Power.ELECTRIC_POWER) > 0 || (Boolean)getWorld().getBlockState(getPos()).get(ElectricMachineBlock.ACTIVE)));
	}
	
	public abstract boolean isPowered();

	public ItemStack getInputSlot(int i){
		if(i < inputSlots.length){
			return inventory[inputSlots[i]];
		} else {
			logInvalidSlot();
			return null;
		}
	}
	
	public ItemStack getOutputSlot(int i){
		if(i < outputSlots.length){
			return inventory[outputSlots[i]];
		} else {
			logInvalidSlot();
			return null;
		}
	}
	
	public ItemStack getOtherSlot(int i){
		if(i < otherSlots.length){
			return inventory[otherSlots[i]];
		} else {
			logInvalidSlot();
			return null;
		}
	}
	public void setInputSlot(int i, ItemStack item){
		if(i < inputSlots.length){
			inventory[inputSlots[i]] = item;
		} else {
			logInvalidSlot();
		}
	}
	
	public void setOutputSlot(int i, ItemStack item){
		if(i < outputSlots.length){
			inventory[ outputSlots[i]] = item;
		} else {
			logInvalidSlot();
		}
	}
	
	public void setOtherSlot(int i, ItemStack item){
		if(i < otherSlots.length){
			inventory[otherSlots[i]] = item;
		} else {
			logInvalidSlot();
		}
	}
	

	public int numberOfInputSlots(){return inputSlots.length;}
	public int numberOfOutputSlots(){return outputSlots.length;}
	public int numberOfOtherSlots(){return otherSlots.length;}

	private void logInvalidSlot() {
		ElectricAdvantage.LOGGER.warn("Invalid slot access in {}. Stack trace: {}",
				getClass().getName(), Arrays.toString(Thread.currentThread().getStackTrace()).replace(',', '\n'));
	}

	
	@Override
	protected ItemStack[] getInventory() {
		return inventory;
	}

	protected static boolean isEmptyStack(ItemStack stack) {
		return stack == null || stack.isEmpty();
	}

	protected static ItemStack copyStackOrNull(ItemStack stack) {
		return isEmptyStack(stack) ? null : stack.copy();
	}

	protected static boolean areItemsEqualNullable(ItemStack left, ItemStack right) {
		if(isEmptyStack(left)) return isEmptyStack(right);
		if(isEmptyStack(right)) return false;
		return ItemStack.areItemsEqual(left, right);
	}

	protected static boolean areItemStacksEqualNullable(ItemStack left, ItemStack right) {
		if(isEmptyStack(left)) return isEmptyStack(right);
		if(isEmptyStack(right)) return false;
		return ItemStack.areItemStacksEqual(left, right);
	}

	public abstract float[] getProgress();

	protected ItemStack getSmeltingResult(ItemStack input) {
		if (isEmptyStack(input) || getWorld() == null) return ItemStack.EMPTY;
		Inventory inventory = new Inventory(1);
		inventory.setInventorySlotContents(0, input);
		return getWorld().getRecipeManager().getRecipe(IRecipeType.SMELTING, inventory, getWorld())
				.map(recipe -> recipe.getCraftingResult(inventory)).orElse(ItemStack.EMPTY);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tagRoot){
		super.write(tagRoot);
		saveTo(tagRoot);
		return tagRoot;
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public void read(CompoundNBT tagRoot){
		super.read(tagRoot);
		loadFrom(tagRoot);
	}

	protected abstract void saveTo(CompoundNBT tagRoot);
	protected abstract void loadFrom(CompoundNBT tagRoot);

	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return ioSlots;
	}
	
	@Override
	public boolean canExtractItem(final int slot, final ItemStack targetItem, final Direction side) {
		return isInArray(slot,outputSlots);
	}

	public boolean hasSpaceForItemInOutputSlots(ItemStack stack){
		if(isEmptyStack(stack)) return false;
		ItemStack item = stack.copy();
		for(int slot = 0; slot < numberOfOutputSlots(); slot++){
			ItemStack dest = getOutputSlot(slot);
			if(isEmptyStack(dest)) return true;
			int stackLimit = Math.min(dest.getMaxStackSize(), this.getInventoryStackLimit());
			if(dest.getCount() >= stackLimit) continue;
			int combinedStackSize = item.getCount() + dest.getCount();
			if(ItemStack.areItemsEqual(item, dest) ){
				if(combinedStackSize <= stackLimit){
					return true;
				} else if (item.getCount() <= stackLimit) {
					item.shrink(stackLimit - dest.getCount());
				}
			}
		}
		return false;
	}
	
	public boolean hasSpaceForItemInInputSlots(ItemStack stack){
		if(isEmptyStack(stack)) return false;
		ItemStack item = stack.copy();
		for(int slot = 0; slot < numberOfInputSlots(); slot++){
			ItemStack dest = getInputSlot(slot);
			if(isEmptyStack(dest)) return true;
			int stackLimit = Math.min(dest.getMaxStackSize(), this.getInventoryStackLimit());
			if(dest.getCount() >= stackLimit) continue;
			int combinedStackSize = item.getCount() + dest.getCount();
			if(ItemStack.areItemsEqual(item, dest) ){
				if(combinedStackSize <= stackLimit){
					return true;
				} else if (item.getCount() <= stackLimit) {
					item.shrink(stackLimit - dest.getCount());
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean canInsertItem(final int slot, final ItemStack srcItem, final Direction side) {
		return isValidInputItem(srcItem) && isInArray(slot,inputSlots);
	}

	@Override
	public boolean isItemValidForSlot(final int slot, final ItemStack srcItem) {
		return isValidInputItem(srcItem) && isInArray(slot,inputSlots);
	}
	
	protected abstract boolean isValidInputItem(ItemStack item);
	
	protected final boolean isInArray(int i, int[] arr){
		for(int n = 0; n < arr.length; n++){
			if(arr[n] == i)return true;
		}
		return false;
	}
	
	public ItemStack insertItemToInputSlots(ItemStack itemStack){
        if(isEmptyStack(itemStack)) return null;
        if(itemStack.getItem() == null) return null;
		itemStack = itemStack.copy();
		for(int slot = 0; slot < numberOfInputSlots(); slot++){
			ItemStack slotContent = this.getInputSlot(slot);
			if(isEmptyStack(slotContent)){
				// empty slot
				this.setInputSlot(slot,itemStack);
				return null;
			} else if(ItemStack.areItemsEqual(itemStack, slotContent)){
				int stackLimit = Math.min(slotContent.getMaxStackSize(), this.getInventoryStackLimit());
				// found slot with same item
				if(slotContent.getCount() < stackLimit){
					// increase stack
					int delta = Math.min(itemStack.getCount(), stackLimit - slotContent.getCount());
					slotContent.grow(delta);
					itemStack.shrink(delta);
					if(itemStack.isEmpty()) return null; // done
				}
			}
		}
		// return left-over
		return itemStack;
	}

	protected void playSoundEffect(double x, double y, double z, SoundEvent sound, float volume, float pitch){
		if(getWorld().isRemote) return;
		final double range = 16;
		AxisAlignedBB bounds = new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range);
		// Broadcast to existing players without loading chunks for a spatial entity query.
		for(PlayerEntity candidate : getWorld().getPlayers()){
			if(!(candidate instanceof ServerPlayerEntity)) continue;
			ServerPlayerEntity player = (ServerPlayerEntity)candidate;
			if(player.connection == null || player.isSpectator() || !player.getBoundingBox().intersects(bounds)) continue;
			player.connection.sendPacket(new SPlaySoundPacket(sound.getRegistryName(), SoundCategory.BLOCKS,
					new net.minecraft.util.math.Vec3d(x, y, z), volume, pitch));
		}
	}
	
	public ItemStack insertItemToOutputSlots(ItemStack itemStack){
        if(isEmptyStack(itemStack)) return null;
        if(itemStack.getItem() == null) return null;
		itemStack = itemStack.copy();
		for(int slot = 0; slot < numberOfOutputSlots(); slot++){
			ItemStack slotContent = this.getOutputSlot(slot);
			if(isEmptyStack(slotContent)){
				// empty slot
				this.setOutputSlot(slot,itemStack);
				return null;
			} else if(ItemStack.areItemsEqual(itemStack, slotContent)){
				int stackLimit = Math.min(slotContent.getMaxStackSize(), this.getInventoryStackLimit());
				// found slot with same item
				if(slotContent.getCount() < stackLimit){
					// increase stack
					int delta = Math.min(itemStack.getCount(), stackLimit - slotContent.getCount());
					slotContent.grow(delta);
					itemStack.shrink(delta);
					if(itemStack.isEmpty()) return null; // done
				}
			}
		}
		// return left-over
		return itemStack;
	}
	
	public int getComparatorOutput(){
		// proportional to number of input slot items
		int sum = 0;
		int total = 0;
		for(int i = 0; i < numberOfInputSlots(); i++){
			ItemStack item = getInputSlot(i);
			if(isEmptyStack(item)){
				total += 64;
			} else {
				sum += item.getCount();
				total += item.getMaxStackSize();
			}
		}
		if(total <= 0) return 0;
		return 15 * sum / total;
	}

	public float getEnergy(){
		return getEnergy(Power.ELECTRIC_POWER);
	}

	public float getEnergyCapacity(){
		return getEnergyCapacity(Power.ELECTRIC_POWER);
	}

	public float addEnergy(float amount){
		return addEnergy(amount,Power.ELECTRIC_POWER);
	}

	public float subtractEnergy(float amount){
		return subtractEnergy(amount,Power.ELECTRIC_POWER);
	}

	public void setEnergy(float amount){ setEnergy(amount,Power.ELECTRIC_POWER); }


	public ConduitType getEnergyType() {return Power.ELECTRIC_POWER;}


	@Override public int getInventoryStackLimit(){return 64;}


	@Override
	public boolean isPowerSink(ConduitType e){
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType e){
		return false;
	}
}
