package zone.moddev.mc.electricadvantage.blocks;

import zone.moddev.mc.electricadvantage.init.Power;
import net.minecraft.block.material.Material;

public class ElectricConduitBlock  extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public ElectricConduitBlock() {
		super(Material.PISTON, 0.75f, 2f/16f, Power.ELECTRIC_POWER);
	}
}
