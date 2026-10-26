package zone.moddev.mc.electricadvantage.compat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import zone.moddev.mc.basemetals.recipe.CrushingRecipe;

/** Accesses Base Metals' data-driven 1.13 crushing recipes without embedding it. */
public final class BaseMetalsCompat {
    private BaseMetalsCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("basemetals");
    }

    /** 1.13 additions are supplied as JSON crushing recipes and item tags. */
    public static void addCrusherRecipe(String input, ItemStack output) {}
    public static void addCrusherRecipe(Item input, ItemStack output) {}
    public static void addCrusherRecipe(Block input, ItemStack output) {}
    public static void registerOreDictionaryCopies(String target, String... sources) {}

    public static boolean hasCrusherRecipe(World world, ItemStack input) {
        return getCrusherRecipeOutput(world, input) != null;
    }

    public static ItemStack getCrusherRecipeOutput(World world, ItemStack input) {
        if (world == null || input == null || input.isEmpty()) return null;
        if (isLoaded()) {
            Inventory inventory = new Inventory(1);
            inventory.setInventorySlotContents(0, input.copy());
            for (IRecipe<?> candidate : world.getRecipeManager().getRecipes()) {
                if (candidate instanceof CrushingRecipe && ((CrushingRecipe) candidate).matches(inventory, world)) {
                    return candidate.getRecipeOutput().copy();
                }
            }
        }
        return getVanillaCrusherOutput(input);
    }

    public static ItemStack getCrusherRecipeOutput(World world, BlockState input) {
        return input == null ? null : getCrusherRecipeOutput(world, new ItemStack(input.getBlock()));
    }

    private static ItemStack getVanillaCrusherOutput(ItemStack input) {
        String key = String.valueOf(input.getItem().getRegistryName());
        if ("minecraft:stone".equals(key) || "minecraft:stone_bricks".equals(key)) {
            return stack("minecraft:cobblestone", 1);
        }
        if ("minecraft:cobblestone".equals(key) || "minecraft:mossy_cobblestone".equals(key)
                || "minecraft:cobblestone_wall".equals(key)) {
            return stack("minecraft:gravel", 1);
        }
        if ("minecraft:gravel".equals(key) || "minecraft:glass".equals(key)) {
            return stack("minecraft:sand", 1);
        }
        if ("minecraft:sandstone".equals(key)) return stack("minecraft:sand", 4);
        if ("minecraft:red_sandstone".equals(key)) return stack("minecraft:red_sand", 4);
        if ("minecraft:glowstone".equals(key)) return stack("minecraft:glowstone_dust", 4);
        if ("minecraft:sugar_cane".equals(key)) return stack("minecraft:sugar", 2);
        if ("minecraft:bone".equals(key)) return stack("minecraft:bone_meal", 3);
        if ("minecraft:blaze_rod".equals(key)) return stack("minecraft:blaze_powder", 2);
        if ("minecraft:redstone_ore".equals(key)) return stack("minecraft:redstone", 8);
        if ("minecraft:redstone_block".equals(key)) return stack("minecraft:redstone", 9);
        if ("minecraft:nether_quartz_ore".equals(key)) return stack("minecraft:quartz", 2);
        if ("minecraft:quartz_block".equals(key)) return stack("minecraft:quartz", 4);
        if ("minecraft:slime_block".equals(key)) return stack("minecraft:slime_ball", 9);
        if ("minecraft:bone_block".equals(key)) return stack("minecraft:bone_meal", 9);
        return null;
    }

    private static ItemStack stack(String id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? null : new ItemStack(item, count);
    }
}
