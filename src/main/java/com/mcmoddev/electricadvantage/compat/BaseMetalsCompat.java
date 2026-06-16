package com.mcmoddev.electricadvantage.compat;

import com.mcmoddev.electricadvantage.ElectricAdvantage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Method;
import java.util.List;

public final class BaseMetalsCompat {

	private static final String MODID = "basemetals";
	private static final String[] CRUSHER_REGISTRY_CLASSES = {
			"cyano.basemetals.registry.CrusherRecipeRegistry",
			"com.mcmoddev.lib.registry.CrusherRecipeRegistry"
	};

	private static boolean registryResolved = false;
	private static Class<?> crusherRegistryClass = null;
	private static boolean missingRegistryLogged = false;

	private BaseMetalsCompat() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static boolean isLoaded() {
		return Loader.isModLoaded(MODID);
	}

	public static void addCrusherRecipe(String input, ItemStack output) {
		if (!isLoaded()) return;
		invokeCrusherRecipe(new Class<?>[]{String.class, ItemStack.class}, new Object[]{input, output});
	}

	public static void addCrusherRecipe(Item input, ItemStack output) {
		if (!isLoaded()) return;
		invokeCrusherRecipe(new Class<?>[]{Item.class, ItemStack.class}, new Object[]{input, output});
	}

	public static void addCrusherRecipe(Block input, ItemStack output) {
		if (!isLoaded()) return;
		invokeCrusherRecipe(new Class<?>[]{Block.class, ItemStack.class}, new Object[]{input, output});
	}

	public static boolean hasCrusherRecipe(ItemStack input) {
		return getCrusherRecipeOutput(input) != null;
	}

	public static ItemStack getCrusherRecipeOutput(ItemStack input) {
		if (input == null) return null;
		if (isLoaded()) {
			ItemStack output = getRecipeOutput(getCrusherRecipe(input, ItemStack.class));
			if (output != null) return output;
		}
		return getVanillaCrusherOutput(input);
	}

	public static ItemStack getCrusherRecipeOutput(IBlockState input) {
		if (input == null) return null;
		if (isLoaded()) {
			ItemStack output = getRecipeOutput(getCrusherRecipe(input, IBlockState.class));
			if (output != null) return output;
		}
		return getCrusherRecipeOutput(new ItemStack(input.getBlock(), 1, input.getBlock().getMetaFromState(input)));
	}

	public static void registerOreDictionaryCopies(String targetName, String... sourceNames) {
		for (String sourceName : sourceNames) {
			List<ItemStack> entries = OreDictionary.getOres(sourceName);
			for (ItemStack entry : entries) {
				if (entry != null) {
					OreDictionary.registerOre(targetName, entry.copy());
				}
			}
		}
	}

	private static Class<?> getCrusherRegistryClass() {
		if (!isLoaded()) return null;
		if (registryResolved) return crusherRegistryClass;

		registryResolved = true;
		for (String className : CRUSHER_REGISTRY_CLASSES) {
			try {
				crusherRegistryClass = Class.forName(className);
				return crusherRegistryClass;
			} catch (ClassNotFoundException e) {
				// Try the next known BaseMetals package.
			}
		}

		if (!missingRegistryLogged) {
			FMLLog.warning("%s: BaseMetals is loaded, but no known crusher recipe registry was found", ElectricAdvantage.MODID);
			missingRegistryLogged = true;
		}
		return null;
	}

	private static void invokeCrusherRecipe(Class<?>[] parameterTypes, Object[] args) {
		Class<?> registry = getCrusherRegistryClass();
		if (registry == null) return;

		try {
			Method method = registry.getMethod("addNewCrusherRecipe", parameterTypes);
			method.invoke(null, args);
		} catch (ReflectiveOperationException | RuntimeException e) {
			FMLLog.log(Level.WARN, e, "%s: failed to add BaseMetals crusher recipe through %s", ElectricAdvantage.MODID, registry.getName());
		}
	}

	private static Object getCrusherRecipe(Object input, Class<?> inputType) {
		Class<?> registry = getCrusherRegistryClass();
		if (registry == null) return null;

		try {
			Object instance = registry.getMethod("getInstance").invoke(null);
			Method method = registry.getMethod("getRecipeForInputItem", inputType);
			method.setAccessible(true);
			return method.invoke(instance, input);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			FMLLog.log(Level.WARN, e, "%s: failed to look up BaseMetals crusher recipe through %s", ElectricAdvantage.MODID, registry.getName());
			return null;
		}
	}

	private static ItemStack getRecipeOutput(Object recipe) {
		if (recipe == null) return null;

		try {
			Method method = recipe.getClass().getMethod("getOutput");
			method.setAccessible(true);
			Object output = method.invoke(recipe);
			return output instanceof ItemStack ? ((ItemStack) output).copy() : null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			FMLLog.log(Level.WARN, e, "%s: failed to read BaseMetals crusher recipe output", ElectricAdvantage.MODID);
			return null;
		}
	}

	private static ItemStack getVanillaCrusherOutput(ItemStack input) {
		if (input == null || input.getItem() == null) return null;

		Block block = Block.getBlockFromItem(input.getItem());
		int meta = input.getMetadata();

		if (block == Blocks.STONE) return new ItemStack(Blocks.COBBLESTONE, 1);
		if (block == Blocks.STONEBRICK) return new ItemStack(Blocks.COBBLESTONE, 1);
		if (block == Blocks.STONE_SLAB && (meta == 0 || meta == 5)) return new ItemStack(Blocks.STONE_SLAB, 1, 3);
		if (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE) return new ItemStack(Blocks.GRAVEL, 1);
		if (block == Blocks.COBBLESTONE_WALL) return new ItemStack(Blocks.GRAVEL, 1);
		if (block == Blocks.GRAVEL) return new ItemStack(Blocks.SAND, 1);
		if (block == Blocks.SANDSTONE) return new ItemStack(Blocks.SAND, 4);
		if (block == Blocks.RED_SANDSTONE) return new ItemStack(Blocks.SAND, 4, 1);
		if (block == Blocks.STONE_SLAB && meta == 1) return new ItemStack(Blocks.SAND, 2);
		if (block == Blocks.STONE_SLAB2 && meta == 0) return new ItemStack(Blocks.SAND, 2, 1);
		if (block == Blocks.GLASS) return new ItemStack(Blocks.SAND, 1);
		if (block == Blocks.STAINED_GLASS) return new ItemStack(Blocks.SAND, 1, meta);
		if (block == Blocks.GLOWSTONE) return new ItemStack(Items.GLOWSTONE_DUST, 4);
		if (block == Blocks.LAPIS_ORE) return new ItemStack(Items.DYE, 8, 4);
		if (block == Blocks.LAPIS_BLOCK) return new ItemStack(Items.DYE, 9, 4);
		if (block == Blocks.REDSTONE_ORE || block == Blocks.LIT_REDSTONE_ORE) return new ItemStack(Items.REDSTONE, 8);
		if (block == Blocks.REDSTONE_BLOCK) return new ItemStack(Items.REDSTONE, 9);
		if (block == Blocks.QUARTZ_ORE) return new ItemStack(Items.QUARTZ, 2);
		if (block == Blocks.QUARTZ_BLOCK) return new ItemStack(Items.QUARTZ, 4);
		if (block == Blocks.PRISMARINE) {
			if (meta == 1) return new ItemStack(Items.PRISMARINE_SHARD, 9);
			if (meta == 2) return new ItemStack(Items.PRISMARINE_SHARD, 8);
			return new ItemStack(Items.PRISMARINE_SHARD, 4);
		}
		if (block == Blocks.SEA_LANTERN) return new ItemStack(Items.PRISMARINE_CRYSTALS, 3);
		if (block == Blocks.SLIME_BLOCK) return new ItemStack(Items.SLIME_BALL, 9);
		if (block == Blocks.BONE_BLOCK) return new ItemStack(Items.DYE, 9, 15);

		if (input.getItem() == Items.REEDS) return new ItemStack(Items.SUGAR, 2);
		if (input.getItem() == Items.BONE) return new ItemStack(Items.DYE, 3, 15);
		if (input.getItem() == Items.BLAZE_ROD) return new ItemStack(Items.BLAZE_POWDER, 2);

		return null;
	}
}
