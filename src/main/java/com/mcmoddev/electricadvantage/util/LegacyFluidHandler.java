package com.mcmoddev.electricadvantage.util;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public interface LegacyFluidHandler extends IFluidHandler {

	int fill(EnumFacing face, FluidStack fluid, boolean forReal);

	FluidStack drain(EnumFacing face, FluidStack fluid, boolean forReal);

	FluidStack drain(EnumFacing face, int amount, boolean forReal);

	boolean canFill(EnumFacing face, Fluid fluid);

	boolean canDrain(EnumFacing face, Fluid fluid);

	FluidTankInfo[] getTankInfo(EnumFacing face);

	@Override
	default IFluidTankProperties[] getTankProperties() {
		FluidTankInfo[] tankInfo = getTankInfo(null);
		IFluidTankProperties[] properties = new IFluidTankProperties[tankInfo.length];
		for (int i = 0; i < tankInfo.length; i++) {
			final FluidTankInfo info = tankInfo[i];
			properties[i] = new IFluidTankProperties() {
				@Override
				public FluidStack getContents() {
					return info.fluid;
				}

				@Override
				public int getCapacity() {
					return info.capacity;
				}

				@Override
				public boolean canFill() {
					return true;
				}

				@Override
				public boolean canDrain() {
					return true;
				}

				@Override
				public boolean canFillFluidType(FluidStack fluidStack) {
					return fluidStack != null && LegacyFluidHandler.this.canFill(null, fluidStack.getFluid());
				}

				@Override
				public boolean canDrainFluidType(FluidStack fluidStack) {
					return fluidStack != null && LegacyFluidHandler.this.canDrain(null, fluidStack.getFluid());
				}
			};
		}
		return properties;
	}

	@Override
	default int fill(FluidStack resource, boolean doFill) {
		return fill(null, resource, doFill);
	}

	@Override
	default FluidStack drain(FluidStack resource, boolean doDrain) {
		return drain(null, resource, doDrain);
	}

	@Override
	default FluidStack drain(int maxDrain, boolean doDrain) {
		return drain(null, maxDrain, doDrain);
	}
}
