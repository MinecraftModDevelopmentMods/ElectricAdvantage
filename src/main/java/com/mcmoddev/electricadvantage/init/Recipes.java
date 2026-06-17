package com.mcmoddev.electricadvantage.init;

import com.mcmoddev.electricadvantage.compat.BaseMetalsCompat;
import com.mcmoddev.poweradvantage.PowerAdvantage;
import com.mcmoddev.poweradvantage.RecipeMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public abstract class Recipes {

	private static boolean initDone = false;

	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Items.init();

		RecipeMode recipeMode = PowerAdvantage.recipeMode;
		
		// Recipes for all recipe modes
		OreDictionary.registerOre("blockBrick", net.minecraft.init.Blocks.BRICK_BLOCK);
		OreDictionary.registerOre("gunpowder", net.minecraft.init.Items.GUNPOWDER);
		GameRegistry.addSmelting(Items.lithium_powder,new ItemStack(Items.lithium_ingot),0.5f);
		GameRegistry.addSmelting(Blocks.lithium_ore,new ItemStack(Items.lithium_ingot),0.5f);
		BaseMetalsCompat.addCrusherRecipe("oreLithium",new ItemStack(Items.lithium_powder,2));
		BaseMetalsCompat.addCrusherRecipe("ingotLithium",new ItemStack(Items.lithium_powder,1));
		BaseMetalsCompat.addCrusherRecipe("oreSulfur",new ItemStack(Items.sulfur_powder,4));
		GameRegistry.addSmelting(Items.silicon_blend, new ItemStack(Items.silicon_ingot), 0.5f);
		GameRegistry.addSmelting(Items.solder_blend, new ItemStack(Items.solder), 0.5f);
		
		// recipe-mode specific non-crafting integration
		if(recipeMode == RecipeMode.APOCALYPTIC){
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_powered_generator, new ItemStack(Items.power_supply_unit,1));
			BaseMetalsCompat.addCrusherRecipe(Blocks.arc_furnace, new ItemStack(Items.power_supply_unit,1));
			BaseMetalsCompat.addCrusherRecipe(Blocks.photovoltaic_generator, new ItemStack(Items.power_supply_unit,1));
			BaseMetalsCompat.addCrusherRecipe(Items.power_supply_unit, new ItemStack(Items.control_circuit,1));
			BaseMetalsCompat.addCrusherRecipe(Items.control_circuit, new ItemStack(Items.integrated_circuit,1));
		} else if(recipeMode == RecipeMode.NORMAL) {
			// normal
			BaseMetalsCompat.registerOreDictionaryCopies("solder", "ingotLead", "ingotTin", "ingotSilver");
		}
		
		
		initDone = true;
	}
}
