package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.init.Blocks;
import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.poweradvantage.util.InventoryWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElectricDrillTileEntity extends ElectricMachineTileEntity{


	public static final int MAX_RANGE = 64;
	public static final float ENERGY_COST_MOVE = 250f;
	public static final float ENERGY_COST_PROGRESS_TICK = 24f;
	public static final float MINING_TIME_FACTOR = 32.0f;


	private final int[] dataSyncArray = new int[4];
	private int progress = 0;
	private int progressGoal = 0;
	private BlockPos targetBlockCoord = null;
	private Block[] targetBlocks = null;
	private ListNBT savedTargetStates = null;
	private List<ItemStack> targetBlockItems = null;
	private int laserLength = 0;


	private boolean deferred = false;

	public ElectricDrillTileEntity() {
		super(zone.moddev.mc.electricadvantage.init.Entities.ELECTRIC_DRILL, "tile.electricadvantage.electric_drill.name",
				0, 6, 0);
	}

	@Override
	public boolean isPowered() {
		return getEnergy() > ENERGY_COST_PROGRESS_TICK;
	}

	private float[] progArr = new float[1];
	@Override
	public float[] getProgress() {
		if(progressGoal > 0){
			progArr[0] = (float)progress / (float)progressGoal;
		} else {
			progArr[0] = 0;
		}
		return progArr;
	}
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			if(deferred){
				restoreTargetBlock();
			}
			updateFacing();

			// disabled by redstone
			if(hasRedstoneSignal()){
				if (progress > 0){
					progress = 0;
				}
			} else {
				if(targetBlockCoord != null){
					// mining time
					if(getEnergy() > ENERGY_COST_PROGRESS_TICK && hasSpaceForItems(targetBlockItems) && canMine(targetBlockCoord)){
						this.subtractEnergy(ENERGY_COST_PROGRESS_TICK, Power.ELECTRIC_POWER);
						progress++;
						if(progress >= progressGoal){
							// Mined it!
							playSoundEffect(targetBlockCoord.getX()+0.5, targetBlockCoord.getY()+0.5, targetBlockCoord.getZ()+0.5, getLevel().getBlockState(targetBlockCoord).getSoundType().getPlaceSound(), 0.5f, 1f);
							playSoundEffect(getBlockPos().getX()+0.5, getBlockPos().getY()+0.5, getBlockPos().getZ()+0.5, SoundEvents.SAND_BREAK, 0.5f, 1f);
							BlockPos[] targets = this.getArea(targetBlockCoord);
							for(int i = 0; i < targets.length; i++)
								if(canMine(targets[i])){
									getLevel().setBlock(targets[i], net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);
								}
							for(ItemStack item : targetBlockItems){
								addItem(item);
							}
							untargetBlock();
						}
					} else {
						// lose progress
						if(progress > 0)progress--;
					}
				}

			}

			this.setActiveState(isActive());
		}

	}

	private boolean hasSpaceForItems(List<ItemStack> items) {
		if(items.size() == 1){
			return super.hasSpaceForItemInOutputSlots(items.get(0));
		} else {
			for(ItemStack i : items){
				if(super.hasSpaceForItemInOutputSlots(i) == false){
					return false;
				}
			}
			return true;
		}
	}

	public ItemStack addItem(ItemStack in){
		return super.insertItemToOutputSlots(in);
	}

	private Direction trackDirection(){
		for(Direction dir : Direction.values()){
			if(getLevel().getBlockState(getBlockPos().relative(dir)).getBlock() == Blocks.electric_track){
				return dir;
			}
		}
		return null;
	}

	private boolean followTrack(){
		if(this.getEnergy() < ENERGY_COST_MOVE) return false;
		Direction trackDir = trackDirection();
		if(trackDir == null) return false;

		// clone this block into neighboring block
		this.untargetBlock();
		World w = getLevel();
		BlockPos nextPos = getBlockPos().relative(trackDir);
		w.setBlock(nextPos, w.getBlockState(getBlockPos()), 2);
		ElectricDrillTileEntity te = (ElectricDrillTileEntity)w.getBlockEntity(nextPos);
		CompoundNBT dataTransfer = new CompoundNBT();
		this.save(dataTransfer);
		te.load(w.getBlockState(nextPos), dataTransfer);
		te.setPosition(nextPos);
		te.clearRemoved();
		te.subtractEnergy(ENERGY_COST_MOVE, getEnergyType());
		te.setChanged();
		Arrays.fill(this.getInventory(), null);

		// replace this block with steam pipe
		w.setBlockAndUpdate(getBlockPos().relative(getFacing()), zone.moddev.mc.poweradvantage.init.Blocks.steel_frame.defaultBlockState());
		w.setBlock(getBlockPos(), Blocks.electric_conduit.defaultBlockState(), 2);


		return true;
	}


	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy());
		dataSyncArray[1] = progress;
		dataSyncArray[2] = progressGoal;
		dataSyncArray[3] = laserLength;
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), this.getEnergyType());
		this.progress = dataSyncArray[1];
		progressGoal = dataSyncArray[2];
		laserLength = dataSyncArray[3];
		updateFacing();
	}
	private float oldEnergy = 0;
	private int oldProgress = 0;
	private int oldLength = 0;
	@Override public void powerUpdate(){
		if(deferred){
			restoreTargetBlock();
		}
		super.powerUpdate();
		boolean flagSync = progress != oldProgress || oldEnergy != getEnergy();
		oldProgress = progress;
		oldEnergy = getEnergy();

		boolean redstone = hasRedstoneSignal();


		Direction f = getFacing();
		BlockPos n = getBlockPos().relative(f);

		// manage drill bits and find next block
		if(redstone || this.getEnergy() <= 0){
			// no power
			flagSync = true;
			this.untargetBlock();
		} else {
			// drill baby drill!
			if(targetBlockCoord == null ){
				// find new block
				boolean hitEnd = false;
				for(int i = 0; i <= MAX_RANGE ; i++){
					if(i == MAX_RANGE){
						hitEnd = true;
						break;
					}

					if(getLevel().isEmptyBlock(n) || getLevel().getBlockState(n).getMaterial().isReplaceable()){
						// this block is not worth mining, replace it
						n = n.relative(f);
						if(n.getY() == 0 || n.getY() == 255){
							hitEnd = true;
							break;
						}
					} else {
						// found a block!
						if(canMine(n)){
							targetBlock(n);
						} else {
							hitEnd = true;
						}
						flagSync = true;
						break;
					}
				}
				// if hit end of range, move along track
				if(hitEnd){
					boolean moved = followTrack();
					if(moved){
						zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry.getInstance()
								.conduitBlockRemovedEvent(getLevel(), getBlockPos(), getEnergyType());
						return;
					}
				}
			} else {
				// currently drilling a block
				// block revalidation
				BlockPos[] targets = getArea(targetBlockCoord);
				for(int i = 0; i < targets.length; i++){
					if(!getLevel().getBlockState(targets[i]).getBlock().equals(targetBlocks[i])){
						// Block changed! Invalidate!
						untargetBlock();
						flagSync = true;
						break;
					}
				}
			}
		}

		calculateDrillLength();
		if(oldLength != laserLength){
			oldLength = laserLength;
			flagSync = true;
		}

		if(flagSync){
			this.sync();
		}


		// push inventory to adjacent chest
		BlockPos adj = getBlockPos().relative(f.getOpposite());
		if(!redstone && !getLevel().isEmptyBlock(adj)){
			inventoryTransfer(adj,f);
		}

		// fry any idiots who stand in the laser's path
		if(isActive() && targetBlocks != null && targetBlockCoord != null){
			BlockPos pos = getBlockPos();
			AxisAlignedBB beamArea = new AxisAlignedBB(targetBlockCoord.getX(),targetBlockCoord.getY(),targetBlockCoord.getZ(),
					pos.getX()+1,pos.getY()+1,pos.getZ()+1);
			List<LivingEntity> victims = getLevel().getEntitiesOfClass(LivingEntity.class, beamArea);
			for(Entity e : victims){
				e.setSecondsOnFire(3);
				e.hurt(Power.laser_damage, 2f);
			}
		}
	}


	public int getDrillLength(){
		return laserLength;
	}

	public void calculateDrillLength(){
		if(this.isActive() && this.targetBlocks != null && this.targetBlockCoord != null){
			BlockPos pos = getBlockPos();
			// distance calculation is taking a short-cut by assuming that 2 out of the 3 XYZ coordinates are identical
			laserLength = Math.abs((pos.getX() - targetBlockCoord.getX()) + (pos.getY() - targetBlockCoord.getY()) + (pos.getZ() - targetBlockCoord.getZ()));
		} else {
			laserLength = 0;
		}
	}

	private void targetBlock (BlockPos pos){
		BlockPos[] targets = getArea(pos);
		progress = 0;
		targetBlockCoord = pos;
		progressGoal = 0;
		targetBlocks = new Block[targets.length];
		savedTargetStates = new ListNBT();
		targetBlockItems = new ArrayList<>(targets.length);
		for(int i = 0; i < targets.length; i++){
			targetBlocks[i] = getLevel().getBlockState(targets[i]).getBlock();
			savedTargetStates.add(NBTUtil.writeBlockState(getLevel().getBlockState(targets[i])));
			progressGoal += this.getBlockStrength(targets[i]);
			targetBlockItems.addAll(Block.getDrops(getLevel().getBlockState(targets[i]),
					(ServerWorld) getLevel(), targets[i], getLevel().getBlockEntity(targets[i])));
		}
		deferred = false;
	}

	private void restoreTargetBlock(){
		int savedProgress = progress;
		BlockPos[] targets = getArea(targetBlockCoord);
		if(savedTargetStates != null){
			if(savedTargetStates.size() != targets.length){
				untargetBlock();
				return;
			}
			for(int i = 0; i < targets.length; i++){
				if(!savedTargetStates.getCompound(i).equals(NBTUtil.writeBlockState(getLevel().getBlockState(targets[i])))){
					untargetBlock();
					return;
				}
			}
		}
		// Rebuild the nine-block target and drops without resetting saved work.
		targetBlock(targetBlockCoord);
		progress = Math.max(0, Math.min(savedProgress, progressGoal));
	}

	private BlockPos[] getArea(BlockPos center){
		BlockPos[] a = new BlockPos[9];
		a[0] = center;
		switch(this.getFacing().getAxis()){
			case X:
				a[1] = center.north();
				a[2] = center.below();
				a[3] = center.south();
				a[4] = center.above();
				a[5] = a[2].north();
				a[6] = a[2].south();
				a[7] = a[4].north();
				a[8] = a[4].south();
				break;
			case Z:
				a[1] = center.below();
				a[2] = center.west();
				a[3] = center.above();
				a[4] = center.east();
				a[5] = a[1].east();
				a[6] = a[1].west();
				a[7] = a[3].east();
				a[8] = a[3].west();
				break;
			case Y:
			default:
				a[1] = center.north();
				a[2] = center.west();
				a[3] = center.south();
				a[4] = center.east();
				a[5] = a[1].east();
				a[6] = a[1].west();
				a[7] = a[3].east();
				a[8] = a[3].west();
				break;
		}
		return a;
	}

	private void deferredTargetBlock (BlockPos n){
		targetBlockCoord = n;
		deferred = true;
	}

	private void untargetBlock(){
		deferred = false;
		progress = 0;
		progressGoal = 0;
		targetBlockCoord = null;
		targetBlocks = null;
		savedTargetStates = null;
		targetBlockItems = null;
	}

	private Direction facingCache = null;
	public Direction getFacing(){
		if(facingCache == null) updateFacing();
		return facingCache;
	}
	private void updateFacing(){
		final Direction old = facingCache;
		facingCache = (Direction)getLevel().getBlockState(getBlockPos()).getValue(ElectricDrillBlock.FACING);
		if(old != null && old != facingCache){
			untargetBlock();
			this.sync();
		}
	}


	private boolean canMine(BlockPos coord){
		BlockState bs  = getLevel().getBlockState(coord);
		Block b = bs.getBlock();
		// indestructable blocks have negative hardness
		return !(bs.getDestroySpeed(getLevel(), coord) < 0);
	}

	private int getBlockStrength(BlockPos coord){
		if(getLevel().isEmptyBlock(coord)){
			return 0;
		}
		BlockState bs  = getLevel().getBlockState(coord);
		Block block = bs.getBlock();
		return (int)(Math.max(MINING_TIME_FACTOR * bs.getDestroySpeed(getLevel(), coord),0.5f * MINING_TIME_FACTOR));
	}



	private void inventoryTransfer(BlockPos adj, Direction otherFace) {
		ItemStack[] inventory = this.getInventory();
		TileEntity te = getLevel().getBlockEntity(adj);
		if(te instanceof IInventory ){
			ISidedInventory inv = InventoryWrapper.wrap((IInventory)te);
			int[] accessibleSlots = inv.getSlotsForFace(otherFace);
			if(accessibleSlots.length == 0) return;
			for(int mySlot = 0; mySlot < inventory.length; mySlot++){
				if(inventory[mySlot] == null || inventory[mySlot].isEmpty()) continue;
				for(int i = 0; i < accessibleSlots.length; i++){
					int theirSlot = accessibleSlots[i];
					ItemStack theirItem = inv.getItem(theirSlot);
					if(inv.canPlaceItemThroughFace(theirSlot, inventory[mySlot], otherFace)){
						if(theirItem == null || theirItem.isEmpty()){
							ItemStack newItem = inventory[mySlot].copy();
							newItem.setCount(1);
							inv.setItem(theirSlot, newItem);
							inventory[mySlot].shrink(1);
							if(inventory[mySlot].isEmpty()) inventory[mySlot] = null;
							inv.setChanged();
							this.setChanged();
							return;
						} else if(ItemStack.isSame(theirItem, inventory[mySlot])
								&& ItemStack.tagMatches(theirItem, inventory[mySlot])
								&& theirItem.getCount() < theirItem.getMaxStackSize()
								&& theirItem.getCount() < inv.getMaxStackSize()){
							theirItem.grow(1);
							inventory[mySlot].shrink(1);
							if(inventory[mySlot].isEmpty()) inventory[mySlot] = null;
							inv.setChanged();
							this.setChanged();
							return;
						}
					}
				}
			}

		}
	}



	public float getProgressLevel(){
		if(progressGoal > 0)
			return (float)progress / (float)progressGoal;
		return 0;
	}




	@Override
	protected void saveTo(CompoundNBT tagRoot) {
		tagRoot.putShort("progress",(short)progress);
		if(targetBlockCoord != null){
			tagRoot.putInt("targetX", targetBlockCoord.getX());
			tagRoot.putInt("targetY", targetBlockCoord.getY());
			tagRoot.putInt("targetZ", targetBlockCoord.getZ());
			if(savedTargetStates != null) tagRoot.put("TargetStates", savedTargetStates.copy());
		}
	}

	@Override
	protected void loadFrom(CompoundNBT tagRoot) {
		untargetBlock();
		if(tagRoot.contains("progress", 99)){
			progress = tagRoot.getShort("progress");
		}
		if(tagRoot.contains("targetX", 99) && tagRoot.contains("targetY", 99) && tagRoot.contains("targetZ", 99)){
			int x = tagRoot.getInt("targetX");
			int y = tagRoot.getInt("targetY");
			int z = tagRoot.getInt("targetZ");
			savedTargetStates = tagRoot.contains("TargetStates", 9) ? tagRoot.getList("TargetStates", 10).copy() : null;
			// Note: world object for tile entities is set AFTER loading them from NBT
			if(getLevel() == null){
				this.deferredTargetBlock(new BlockPos(x,y,z));
			} else {
				this.deferredTargetBlock(new BlockPos(x,y,z));
				this.restoreTargetBlock();
			}
		}
	}

	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return true;
	}

	@Override
	protected void setActiveState(boolean active){
		BlockState oldState = getLevel().getBlockState(getBlockPos());
		if(oldState.getBlock() instanceof ElectricDrillBlock
				&& (Boolean)oldState.getValue(ElectricDrillBlock.ACTIVE) != active ){
			BlockState newState = oldState.setValue(ElectricDrillBlock.ACTIVE, active);
			getLevel().setBlock(getBlockPos(), newState, 3);
		}
	}
	@Override
	protected void setPowerState(boolean powered){
		// do nothing, this block does not have a powered state
	}

	@Override
	public boolean isActive(){
		return isPowered() && (!this.hasRedstoneSignal() && (this.getEnergy() > 0 || (Boolean)getLevel().getBlockState(getBlockPos()).getValue(ElectricDrillBlock.ACTIVE)));
	}

	// Helps with laser rendering
	final private int renderRange = MAX_RANGE * 2;
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(getBlockPos().offset(-renderRange, -renderRange, -renderRange), getBlockPos().offset(renderRange, renderRange, renderRange));
		//return super.INFINITE_EXTENT_AABB;
	}
}
