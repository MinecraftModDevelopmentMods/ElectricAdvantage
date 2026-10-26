package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;

import java.util.Arrays;
import java.util.List;

public abstract class ElectricGeneratorTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine {

	private final ItemStack[] inventory;
	private final int[] inputSlots;
	
	
	public ElectricGeneratorTileEntity(TileEntityType<?> tileType, String name,int numInputSlots) {
		super(tileType, Power.ELECTRIC_POWER, 1000, name);
		inventory = new ItemStack[numInputSlots];
		inputSlots = new int[numInputSlots];
		for(int i = 0; i < inventory.length; i++){
			if(i < inputSlots.length)inputSlots[i] = i;
		}
	}

	public ElectricGeneratorTileEntity(TileEntityType<?> tileType, String name,int numInputSlots, ConduitType[] types, float[] energyBuffers) {
		super(tileType, types, energyBuffers, name);
		inventory = new ItemStack[numInputSlots];
		inputSlots = new int[numInputSlots];
		for(int i = 0; i < inventory.length; i++){
			if(i < inputSlots.length)inputSlots[i] = i;
		}
	}


	private boolean redstone = false;
	private float oldEnergy = 0f;
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		
		redstone = getWorld().isBlockPowered(getPos());
		
		if(oldEnergy != this.getEnergy(Power.ELECTRIC_POWER)){
			this.sync();
		}
	}
	
	protected boolean hasRedstoneSignal(){
		return redstone;
	}

	
	protected void setActive(boolean active){
		BlockState old = getWorld().getBlockState(getPos());
		if(old.getBlock() instanceof ElectricGeneratorBlock 
				&& (Boolean)old.get(ElectricGeneratorBlock.ACTIVE) != active){
			getWorld().setBlockState(getPos(), old.with(ElectricGeneratorBlock.ACTIVE, active), 3);
		}
	}

	protected void playSoundEffect(double x, double y, double z, SoundEvent sound, float volume, float pitch){
		if(getWorld().isRemote) return;
		final double range = 16;
		List<ServerPlayerEntity> players = getWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(
				x - range, y - range, z - range,
				x + range, y + range, z + range));
		for(ServerPlayerEntity player : players){
			player.connection.sendPacket(new SPlaySoundPacket(sound.getRegistryName(), SoundCategory.BLOCKS,
					new net.minecraft.util.math.Vec3d(x, y, z), volume, pitch));
		}
	}

	public boolean isActive(){
		return (Boolean)getWorld().getBlockState(getPos()).get(ElectricGeneratorBlock.ACTIVE);
	}

	@Override
	public boolean isPowerSink(ConduitType e){
		return false;
	}

	@Override
	public boolean isPowerSource(ConduitType e){
		return true;
	}
	
	public abstract float getPowerOutput();

	public ItemStack getInputSlot(int i){
		if(i < inputSlots.length){
			return inventory[inputSlots[i]];
		} else {
			ElectricAdvantage.LOGGER.warn("Invalid slot access in {}. Stack trace: {}",
					getClass().getName(), Arrays.toString(Thread.currentThread().getStackTrace()).replace(',', '\n'));
			return null;
		}
	}
	

	public int numberOfInputSlots(){return inputSlots.length;}
	
	
	@Override
	protected ItemStack[] getInventory() {
		return inventory;
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

	public void setEnergy(float amount){
		setEnergy(amount,Power.ELECTRIC_POWER);
	}

	public ConduitType getEnergyType() {return Power.ELECTRIC_POWER;}

	public int getComparatorOutput(){
		return (int)(15 * this.getEnergy(Power.ELECTRIC_POWER) / this.getEnergyCapacity(Power.ELECTRIC_POWER));
	}

	
	@Override
	public CompoundNBT write(CompoundNBT tagRoot){
		super.write(tagRoot);
		saveTo(tagRoot);
		return tagRoot;
	}
	
	@Override
	public void read(CompoundNBT tagRoot){
		super.read(tagRoot);
		loadFrom(tagRoot);
	}
	
	protected abstract void saveTo(CompoundNBT tagRoot);
	protected abstract void loadFrom(CompoundNBT tagRoot);

	@Override public int getInventoryStackLimit(){return 64;}
}
