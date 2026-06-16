package com.mcmoddev.electricadvantage.init;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.Level;

public abstract class Villages {
	// TODO: add machinist villager
	private static final ResourceLocation[] PROFESSION_LIST = {
			new ResourceLocation("minecraft:farmer"),
			new ResourceLocation("minecraft:librarian"),
			new ResourceLocation("minecraft:priest"),
			new ResourceLocation("minecraft:smith"),
			new ResourceLocation("minecraft:butcher")
	};

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;

		Entities.init();
		try {
			insertTrades(1, 1, 2, new EntityVillager.ListItemForEmeralds(
					Items.petrolplastic_ingot, new EntityVillager.PriceInfo(-8, -4)));
			insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(
					Item.getItemFromBlock(Blocks.electric_conduit), new EntityVillager.PriceInfo(-6, -3)));
			insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(
					Items.solder, new EntityVillager.PriceInfo(-10, -5)));
			insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(
					Items.integrated_circuit, new EntityVillager.PriceInfo(-6, -3)));
			insertTrades(1, 1, 1, new EntityVillager.ListItemForEmeralds(
					Items.blank_circuit_board, new EntityVillager.PriceInfo(-6, -3)));
			insertTrades(1, 1, 2, new EntityVillager.ListItemForEmeralds(
					Items.power_supply_unit, new EntityVillager.PriceInfo(1, 3)));
		} catch (RuntimeException e) {
			FMLLog.log(Level.ERROR, e, "Failed to add trades to villagers");
		}

		
		initDone = true;
	}

	private static void insertTrades(int professionID, int careerID, int tradeLevel, EntityVillager.ITradeList... trades) {
		VillagerRegistry.VillagerProfession profession = VillagerRegistry.instance().getRegistry().getValue(PROFESSION_LIST[professionID]);
		if (profession == null) {
			throw new IllegalStateException("No villager profession registered for " + PROFESSION_LIST[professionID]);
		}
		VillagerRegistry.VillagerCareer career = profession.getCareer(careerID - 1);
		career.addTrade(tradeLevel, trades);
	}
}
