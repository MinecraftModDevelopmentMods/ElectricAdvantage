package com.mcmoddev.electricadvantage.machines;

import com.mcmoddev.electricadvantage.init.Power;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class GrowthChamberBlock extends ElectricMachineBlock{

	public GrowthChamberBlock(){
		super(Material.PISTON,Power.GROWTHCHAMBER_POWER);
	}

	@Override
	public ElectricMachineTileEntity createNewTileEntity(World w, int m) {
		return new GrowthChamberTileEntity();
	}
}
