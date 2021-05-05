package cyano.electricadvantage.init;

import com.mcmoddev.lib.registry.CrusherRecipeRegistry;
import com.mcmoddev.poweradvantage.PowerAdvantage;
import com.mcmoddev.poweradvantage.RecipeMode;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod.EventBusSubscriber
public class Recipes {

	private static boolean initDone = false;

	@SubscribeEvent
	public static void init(RegistryEvent.Register<IRecipe> event){
		if(initDone) return;

		Blocks.init();
		Items.init();

		RecipeMode recipeMode = PowerAdvantage.recipeMode;

		// Recipes for all recipe modes
		OreDictionary.registerOre("blockBrick", net.minecraft.init.Blocks.BRICK_BLOCK);
		OreDictionary.registerOre("gunpowder", net.minecraft.init.Items.GUNPOWDER);
		GameRegistry.addSmelting(Items.lithium_powder,new ItemStack(Items.lithium_ingot),0.5f);
		GameRegistry.addSmelting(Blocks.lithium_ore,new ItemStack(Items.lithium_ingot),0.5f);
		CrusherRecipeRegistry.addNewCrusherRecipe("oreLithium",new ItemStack(Items.lithium_powder,2));
		CrusherRecipeRegistry.addNewCrusherRecipe("ingotLithium",new ItemStack(Items.lithium_powder,1));
		CrusherRecipeRegistry.addNewCrusherRecipe("oreSulfur",new ItemStack(Items.sulfur_powder,4));

		addRecipe(event, new ItemStack(Blocks.electric_conduit,6),"xxx","ccc","xxx",'x',"plastic",'c',"ingotCopper");
		addRecipe(event, new ItemStack(Blocks.electric_conduit,6),"xxx","ccc","xxx",'x',"rubber",'c',"ingotCopper");
		addRecipe(event, new ItemStack(Blocks.electric_conduit,1),"xx","cc",'x',"plastic",'c',"rodCopper");
		addRecipe(event, new ItemStack(Blocks.electric_conduit,1),"xx","cc",'x',"rubber",'c',"rodCopper");
		addRecipe(event, new ItemStack(Items.blank_circuit_board,2),"plastic","plateCopper");
		addRecipe(event, new ItemStack(Items.control_circuit,1),Items.blank_circuit_board,"microchip","solder");
		GameRegistry.addSmelting(Items.silicon_blend, new ItemStack(Items.silicon_ingot), 0.5f);
		GameRegistry.addSmelting(Items.solder_blend, new ItemStack(Items.solder), 0.5f);
		addRecipe(event, new ItemStack(Items.silicon_blend,1),"sand","dustCarbon");
		addRecipe(event, new ItemStack(Items.solder_blend,3),"dustTin","dustTin","dustLead");
		addRecipe(event, new ItemStack(Items.solder_blend,3),"dustTin","dustTin","dustSilver");
		addRecipe(event, new ItemStack(Blocks.led_bar,3),"ggg","xxx","ccc",'g',"paneGlass",'x',"microchip",'c',"wire");

		addRecipe(event, batteryRecipe(Items.lead_acid_battery,"ingotLead","sulfur", net.minecraft.init.Items.WATER_BUCKET));
		addRecipe(event, batteryRecipe(Items.lead_acid_battery,"ingotLead","dustSulfur", net.minecraft.init.Items.WATER_BUCKET));
		addRecipe(event, batteryRecipe(Items.nickel_hydride_battery,"ingotNickel","dustRedstone", net.minecraft.init.Items.WATER_BUCKET));
		addRecipe(event, batteryRecipe(Items.alkaline_battery,"ingotIron","gunpowder","ingotZinc"));
		addRecipe(event, batteryRecipe(Items.lithium_battery,"ingotLithium","dustRedstone","dustCarbon"));

		addRecipe(event, new ItemStack(Blocks.electric_track,1),Blocks.electric_conduit, cyano.poweradvantage.init.Blocks.steel_frame);

		// non-apocalyctic recipes (high-tech machines cannot be crafted in post-apocalyspe mode)
		if(recipeMode != RecipeMode.APOCALYPTIC){
			addRecipe(event, new ItemStack(Blocks.photovoltaic_generator,1),"ggg","sss","wuw",'g',"paneGlass",'s',"ingotSilicon",'w',"wire",'u',"PSU");
		}


		// recipe-mode specific recipes
		if(recipeMode == RecipeMode.TECH_PROGRESSION){
			addRecipe(event, new ItemStack(Items.integrated_circuit,3),"prp","sss","ccc",'p',"plastic",'s',"ingotSilicon",'r',"dustRedstone",'c',"nuggetCopper");
			addRecipe(event, new ItemStack(Items.integrated_circuit,3),"prp","sss","ccc",'p',"plastic",'s',"ingotSilicon",'r',"dustRedstone",'c',"nuggetTin");
			addRecipe(event, new ItemStack(Items.integrated_circuit,3),"prp","sss","ccc",'p',"plastic",'s',"ingotSilicon",'r',"dustRedstone",'c',"nuggetGold");
			addRecipe(event, new ItemStack(Items.power_supply_unit,1),"wcw"," s ",'w',"wire",'c',"circuitBoard",'s',"plateSteel");
		} else if(recipeMode == RecipeMode.APOCALYPTIC){
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.steam_powered_generator, new ItemStack(Items.power_supply_unit,1));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.arc_furnace, new ItemStack(Items.power_supply_unit,1));
			CrusherRecipeRegistry.addNewCrusherRecipe(Blocks.photovoltaic_generator, new ItemStack(Items.power_supply_unit,1));
			CrusherRecipeRegistry.addNewCrusherRecipe(Items.power_supply_unit, new ItemStack(Items.control_circuit,1));
			CrusherRecipeRegistry.addNewCrusherRecipe(Items.control_circuit, new ItemStack(Items.integrated_circuit,1));
			addRecipe(event, new ItemStack(Items.power_supply_unit,1),"wcw"," s ",'w',"wire",'c',"circuitBoard",'s',"plateSteel");
		} else {
			// normal
			OreDictionary.registerOre("solder", com.mcmoddev.basemetals.init.Items.lead_ingot);
			OreDictionary.registerOre("solder", com.mcmoddev.basemetals.init.Items.tin_ingot);
			OreDictionary.registerOre("solder", com.mcmoddev.basemetals.init.Items.silver_ingot);
			addRecipe(event, new ItemStack(Items.integrated_circuit,3),"sss","ccc",'s',"ingotSilicon",'c',"nuggetCopper");
			addRecipe(event, new ItemStack(Items.integrated_circuit,3),"sss","ccc",'s',"ingotSilicon",'c',"nuggetTin");
			addRecipe(event, new ItemStack(Items.integrated_circuit,3),"sss","ccc",'s',"ingotSilicon",'c',"nuggetGold");
			addRecipe(event, new ItemStack(Items.blank_circuit_board,2),"plastic","ingotCopper");
			addRecipe(event, new ItemStack(Items.power_supply_unit,1),"wcw"," s ",'w',"wire",'c',"circuitBoard",'s',"ingotSteel");
			addRecipe(event, new ItemStack(Items.power_supply_unit,1),"wcw"," s ",'w',"wire",'c',"circuitBoard",'s',"ingotIron");
		}

		// Machine recipes
		addRecipe(event, electricMachineRecipe(Blocks.steam_powered_generator, "conduitSteam","governor"));
		addRecipe(event, new ItemStack(Blocks.arc_furnace),
						"bbb", "bub", "bbb",
						'b', "blockBrick",
						'u', "PSU");
		addRecipe(event, electricMachineRecipe(Blocks.hydroelectric_generator, "sprocket", "sprocket"));
		addRecipe(event, electricMachineRecipe(Blocks.battery_array, "chest"));
		addRecipe(event, electricMachineRecipe(Blocks.rock_crusher, "sprocket", "gemDiamond"));
		addRecipe(event, electricMachineRecipe(Blocks.laser_turret, "gemDiamond", "gemEmerald"));
		addRecipe(event, electricMachineRecipe(Blocks.laser_turret, "gemEmerald", "gemDiamond"));
		addRecipe(event, electricMachineRecipe(Blocks.laser_drill, "blockDiamond"));
		addRecipe(event, electricMachineRecipe(Blocks.fabricator, net.minecraft.init.Blocks.CRAFTING_TABLE));
		addRecipe(event, electricMachineRecipe(Blocks.growth_chamber, net.minecraft.init.Items.FLOWER_POT, "microchip"));
		addRecipe(event, electricMachineRecipe(Blocks.growth_chamber_controller, net.minecraft.init.Items.FLOWER_POT, "circuitBoard"));
		addRecipe(event, electricMachineRecipe(Blocks.oven, "paneGlass", "PSU"));

		addRecipe(event, new ItemStack(Blocks.electric_switch),
				" L ", "pfp",
						'L', net.minecraft.init.Blocks.LEVER,
						'p', "wire",
						'f', "frameSteel");
		addRecipe(event, electricMachineRecipe(Blocks.electric_still, net.minecraft.init.Items.BUCKET, net.minecraft.init.Items.BUCKET));
		addRecipe(event, electricMachineRecipe(Blocks.electric_pump, net.minecraft.init.Blocks.PISTON, net.minecraft.init.Items.BUCKET));
		addRecipe(event, electricMachineRecipe(Blocks.plastic_refinery, net.minecraft.init.Blocks.PISTON, "sprocket"));


		initDone = true;
	}

	static void addRecipe(RegistryEvent.Register<IRecipe> event, ShapedOreRecipe recipe) {
		event.getRegistry().register(recipe);
	}

	static void addRecipe(RegistryEvent.Register<IRecipe> event, ItemStack stack, Object... recipe) {
		event.getRegistry().register(new ShapedOreRecipe(stack.getItem().getRegistryName(), stack, recipe)
				.setRegistryName(stack.getItem().getRegistryName()));
	}

	private static ShapedOreRecipe electricMachineRecipe(Block output, Object item) {
		ShapedOreRecipe ret = new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output),
						"uX ","pmp",
						'X', item,
						'u', "PSU",
						'p', "plateSteel",
						'm', "frameSteel");
		ret.setRegistryName(output.getRegistryName());
		return ret;
	}

	private static ShapedOreRecipe electricMachineRecipe(Block output, Object item1, Object item2) {
		ShapedOreRecipe ret = new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output),
						"uXY", "pmp",
						'X', item1,
						'Y', item2,
						'u', "PSU",
						'p', "plateSteel",
						'm', "frameSteel");
		ret.setRegistryName(output.getRegistryName());
		return ret;
	}

	private static ShapedOreRecipe batteryRecipe(Item output, Object top, Object middle, Object bottom) {
		ShapedOreRecipe ret = new ShapedOreRecipe(output.getRegistryName(), new ItemStack(output),
						"pXp", "pYp", "pZp",
						'X', top,
						'Y', middle,
						'Z', bottom,
						'p', "plastic");
		ret.setRegistryName(output.getRegistryName());
		return ret;
	}
}