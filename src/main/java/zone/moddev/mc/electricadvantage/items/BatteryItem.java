package zone.moddev.mc.electricadvantage.items;

import zone.moddev.mc.electricadvantage.common.IRechargeableItem;
import zone.moddev.mc.electricadvantage.init.Power;
import cyano.poweradvantage.api.ConduitType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.text.NumberFormat;
import java.util.List;


public class BatteryItem extends net.minecraft.item.Item implements IRechargeableItem {
	private final float capacity;
	private final NumberFormat nf;
	
	public BatteryItem(float capacity, Item.Properties properties){
		super(properties.maxStackSize(1));
		this.capacity = capacity;
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
	}
	
	@Override
	public float getEnergy(ItemStack stack) {
		if(stack.getItem() instanceof IRechargeableItem){
			BatteryItem battery = (BatteryItem)stack.getItem();
			NBTTagCompound dataTag;
			if(!stack.hasTag()){
				return battery.capacity; // new battery starts with 100% charge
			} else {
				dataTag = stack.getTag();
				if(dataTag.contains("energy", 99)){
					return dataTag.getFloat("energy");
				} else {
					return battery.capacity; // new battery starts with 100% charge
				}
			}
		} else {
			return 0;
		}
	}
	@Override
	public void setEnergy(ItemStack stack, float energy){
		if(stack.getItem() instanceof BatteryItem){
			NBTTagCompound dataTag;
			dataTag = stack.getOrCreateTag();
			dataTag.setFloat("energy", energy);
			stack.setTag(dataTag);
		}
	}
	/**
	 * Adds energy to a battery item
	 * @param stack battery item stack
	 * @param energy energy to add
	 * @return Returns the actual amount of energy added
	 */
	@Override
	public float addEnergy(ItemStack stack, float energy, ConduitType type){
		if(ConduitType.areSameType(type, Power.ELECTRIC_POWER) && stack.getItem() instanceof BatteryItem){
			BatteryItem battery = (BatteryItem)stack.getItem();
			NBTTagCompound dataTag;
			dataTag = stack.getOrCreateTag();
			float currentEnergy;
			if(dataTag.contains("energy", 99)){
				currentEnergy = dataTag.getFloat("energy");
			} else {
				currentEnergy = battery.capacity;
			}
			float newEnergy = Math.max(0, Math.min(currentEnergy+energy, battery.capacity));
			dataTag.setFloat("energy", newEnergy);
			stack.setTag(dataTag);
			return newEnergy - currentEnergy;
		} else {
			return 0;
		}
	}
	
	
	@Override
	public float getMaxEnergy(ItemStack item){
		return this.capacity;
	}
	
	@Override
	public ConduitType getEnergyType(){
		return Power.ELECTRIC_POWER;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag){
		super.addInformation(stack, world, list, flag);
		float max = getMaxEnergy(stack);
		if(max <= 0) return; // not a battery!
		float e = getEnergy(stack);
		list.add(new TextComponentString(nf.format(100 * e / max) + "% ")
				.appendSibling(new TextComponentTranslation("tooltip.battery.charge")));
		list.add(new TextComponentString("(" + nf.format(max) + "kJ ")
				.appendSibling(new TextComponentTranslation("tooltip.battery.capacity"))
				.appendText(")"));
	}
}
