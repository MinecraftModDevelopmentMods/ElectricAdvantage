package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.util.LegacyFluidHandler;
import zone.moddev.mc.electricadvantage.init.Items;
import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerConnectorContext;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import zone.moddev.mc.poweradvantage.init.Fluids;
import zone.moddev.mc.poweradvantage.util.FluidIdHelper;
import net.minecraft.util.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.fluid.Fluid;
import cyano.poweradvantage.api.fluid.FluidStack;
import cyano.poweradvantage.api.fluid.FluidTank;
import cyano.poweradvantage.api.fluid.FluidTankInfo;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class PlasticRefineryTileEntity extends ElectricMachineTileEntity implements LegacyFluidHandler{


	public static final float ENERGY_PER_TICK = 12f;
	public static final short TICKS_PER_INGOT = 100;
	public static final int OIL_PER_INGOT = 100;

	private final FluidTank tank;
	private short fabTime = 0;

	private final int[] dataSyncArray = new int[4];

	public PlasticRefineryTileEntity() {
		super(zone.moddev.mc.electricadvantage.init.Entities.PLASTIC_REFINERY, "tile.electricadvantage.plastic_refinery.name",
				0, 1, 0, new ConduitType[]{Power.ELECTRIC_POWER, Fluids.fluidConduit_general},new float[]{500f,2000f});
		tank = new FluidTank(FluidTank.BUCKET_VOLUME * 2);
	}

	private boolean wasActive = false;
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side logic
			if(!this.hasRedstoneSignal() && outputIsAvailable()
					&& getTank().getFluidAmount() >= OIL_PER_INGOT){
				if(fabTime < TICKS_PER_INGOT){
					// working
					if(this.getEnergy() > ENERGY_PER_TICK){
						this.subtractEnergy(ENERGY_PER_TICK, Power.ELECTRIC_POWER);
						fabTime++;
						if(!wasActive){
							this.setActiveState(true);
							wasActive = true;
						}
					}
				} else {
					// done
					getTank().drain(OIL_PER_INGOT, true);
					this.insertItemToOutputSlots(new ItemStack(Items.petrolplastic_ingot,1));
					playSoundEffect(getPos().getX()+0.5, getPos().getY()+0.5, getPos().getZ()+0.5, SoundEvents.BLOCK_ANVIL_LAND, 0.15f, 1f);
					fabTime = 0;
					if(wasActive){
						this.setActiveState(false);
						wasActive = false;
					}
				}
			} else {
				fabTime = 0;
				if(wasActive){
					this.setActiveState(false);
					wasActive = false;
				}
			}
		}
	}

	private boolean outputIsAvailable(){
		return this.getOutputSlot(0) == null
				|| (this.getOutputSlot(0).getItem().equals(Items.petrolplastic_ingot)
						&& this.getOutputSlot(0).getCount() < this.getOutputSlot(0).getMaxStackSize());
	}

	@Override
	public boolean isPowered() {
		return getEnergy() > ENERGY_PER_TICK;
	}

	private final float[] progs = new float[1];
	@Override
	public float[] getProgress() {
		progs[0] = (float)fabTime / (float)TICKS_PER_INGOT;
		return progs;
	}

	public FluidTank getTank(){
		return tank;
	}

	@Override
	protected void saveTo(CompoundNBT tagRoot) {
		tagRoot.putShort("progress", fabTime);
		CompoundNBT tankTag = new CompoundNBT();
		this.getTank().writeToNBT(tankTag);
		tagRoot.put("Tank", tankTag);
	}

	@Override
	protected void loadFrom(CompoundNBT tagRoot) {
		if(tagRoot.contains("progress", 99)){
			this.fabTime = tagRoot.getShort("progress");
		}
		if (tagRoot.contains("Tank", 10)) {
			CompoundNBT tankTag = tagRoot.getCompound("Tank");
			getTank().readFromNBT(tankTag);
			if(tankTag.contains("Empty", 1)){
				// empty the tank if NBT says its empty (not default behavior of Tank.readFromNBT(...) )
				getTank().setFluid(null);
			}
		}
	}

	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return item != null && !item.isEmpty()
				&& ItemTags.getCollection().getOrCreate(new ResourceLocation("forge", "plastics"))
						.contains(item.getItem());
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy());
		dataSyncArray[1] = this.fabTime;
		dataSyncArray[2] = this.getTank().getFluidAmount();
		dataSyncArray[3] = (getTank().getFluid() != null && getTank().getFluid().getFluid() != null
				? FluidIdHelper.getFluidId(getTank().getFluid().getFluid())
						: FluidIdHelper.getFluidId(net.minecraft.fluid.Fluids.WATER));

	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), this.getEnergyType());
		this.fabTime = (short)dataSyncArray[1];
		Fluid fluid = FluidIdHelper.getFluid(dataSyncArray[3]);
		this.getTank().setFluid(fluid == null || dataSyncArray[2] <= 0 ? null : new FluidStack(fluid, dataSyncArray[2]));
	}

	private boolean isPlasticFluid(Fluid fluid) {
		if(fluid == null) return false;
		ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid);
		return id != null && ElectricAdvantage.INSTANCE.PLASTIC_FLUID_MATERIALS.contains(id.toString());
	}



	@Override
	public int getComparatorOutput() {
		return 15 * this.getTank().getFluidAmount() / this.getTank().getCapacity();
	}
///// Overrides to make this a multi-type block /////

	/**
	 * Adds "energy" as a fluid to the FluidTank returned by getTank(). This implementation ignores
	 * all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float addEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(this.canFill(null, Fluids.conduitTypeToFluid(type))){
				return this.fill(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
			} else {
				return 0;
			}
		}else{
			return super.addEnergy(amount, type);
		}
	}

	/**
	 * Sets the tank contents using the energy API method
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 */
	@Override
	public void setEnergy(float amount,ConduitType type) {
		if(Fluids.isFluidType(type)){
			// do nothing, use getTank() methods instead
		}else{
			super.setEnergy(amount, type);
		}
	}
	/**
	 * Subtracts "energy" as a fluid to the FluidTank returned by getTank(). This implementation
	 * ignores all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float subtractEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(this.canDrain(null, Fluids.conduitTypeToFluid(type))){
				FluidStack drained = this.drain(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
				return drained == null ? 0 : -1 * drained.amount;
			} else {
				return 0;
			}
		}else{
			return super.subtractEnergy(amount, type);
		}
	}

	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(this.hasRedstoneSignal()) return PowerRequest.REQUEST_NOTHING;
		if(isPlasticFluid(Fluids.conduitTypeToFluid(offer))){
			PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY+1,
					(getTank().getCapacity() - getTank().getFluidAmount()),
					this);
			return request;
		} else {
			return super.getPowerRequest(offer);
		}
	}


	@Override
	public boolean canAcceptConnection(PowerConnectorContext p){
		ConduitType type = p.powerType;
		return ConduitType.areSameType(getEnergyType(), type) || ConduitType.areSameType(Fluids.fluidConduit_general, type);
	}

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public int fill(Direction face, FluidStack fluid, boolean forReal) {
		if(fluid == null) return 0;
		if(isPlasticFluid(fluid.getFluid())){
			if(getTank().getFluidAmount() <= 0 || getTank().getFluid().getFluid().equals(fluid.getFluid())){
				return getTank().fill(fluid, forReal);
			}
		}
		return 0;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(Direction face, FluidStack fluid, boolean forReal) {
		if(fluid == null) return null;
		if(getTank().getFluidAmount() > 0 && getTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getTank().drain(fluid.amount,forReal);
		} else {
			return null;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param amount The amount of fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(Direction face, int amount, boolean forReal) {
		if(amount > 0 && getTank().getFluidAmount() > 0 ){
			return getTank().drain(amount,forReal);
		} else {
			return null;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canFill(Direction face, Fluid fluid) {
		if(isPlasticFluid(fluid)) {
			if(getTank().getFluidAmount() <= 0) return true;
			return getTank().getFluidAmount() <= getTank().getCapacity() && fluid.equals(getTank().getFluid().getFluid());
		}
		return false;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canDrain(Direction face, Fluid fluid) {
		if(fluid == null) return false;
		return getTank().getFluidAmount() > 0 && fluid.equals(getTank().getFluid().getFluid());
	}

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @return array of FluidTankInfo describing all of the FluidTanks
	 */
	@Override
	public FluidTankInfo[] getTankInfo(Direction face) {
		FluidTankInfo[] arr = new FluidTankInfo[1];
		arr[0] = getTank().getInfo();
		return arr;
	}

	@Override
	public boolean isPowerSource(ConduitType e){
		return false;
	}
	@Override
	public boolean isPowerSink(ConduitType e){
		return true;
	}
	private final ConduitType[] types = {Power.ELECTRIC_POWER,Fluids.fluidConduit_general};
	@Override
	public ConduitType[] getTypes(){
		return types;
	}

	///// end multi-type overrides /////
}
