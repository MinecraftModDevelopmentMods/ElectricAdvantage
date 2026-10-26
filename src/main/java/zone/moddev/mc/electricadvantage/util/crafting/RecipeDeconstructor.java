package zone.moddev.mc.electricadvantage.util.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;

/** Recursive crafting solver used by the Electric Fabricator. */
public final class RecipeDeconstructor {
    private static final RecipeDeconstructor INSTANCE = new RecipeDeconstructor();
    public static int RECURSION_LIMIT = 5;

    private final Map<ItemRecord, List<IRecipe<?>>> recipeCache = new HashMap<>();
    private int cachedRecipeCount = -1;

    private RecipeDeconstructor() {}

    public static RecipeDeconstructor getInstance() {
        return INSTANCE;
    }

    public void invalidateRecipeCache() {
        recipeCache.clear();
        cachedRecipeCount = -1;
    }

    private void initializeRecipeCache(World world) {
        Collection<IRecipe<?>> recipes = world.getRecipeManager().getRecipes();
        if (cachedRecipeCount == recipes.size() && !recipeCache.isEmpty()) return;
        recipeCache.clear();
        for (IRecipe<?> recipe : recipes) {
            ItemStack output = recipe.getRecipeOutput();
            if (output == null || output.isEmpty()) continue;
            recipeCache.computeIfAbsent(new ItemRecord(output), ignored -> new ArrayList<>()).add(recipe);
        }
        cachedRecipeCount = recipes.size();
    }

    public SerializedInventory attemptToCraft(World world, ItemStack target,
            SerializedInventory inventory, AtomicReference<ItemStack> output) {
        initializeRecipeCache(world);
        return attemptToCraft(world, target, inventory, output, 0);
    }

    private SerializedInventory attemptToCraft(World world, ItemStack target,
            SerializedInventory inventory, AtomicReference<ItemStack> output, int depth) {
        if (depth > RECURSION_LIMIT || target == null || target.isEmpty()) return null;
        List<IRecipe<?>> recipes = recipeCache.get(new ItemRecord(target));
        if (recipes == null || recipes.isEmpty()) return null;

        for (IRecipe<?> recipe : recipes) {
            SerializedInventory candidate = inventory.copy();
            boolean failed = false;
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient == null || ingredient == Ingredient.EMPTY) continue;
                ItemStack[] matching = ingredient.getMatchingStacks();
                if (matching == null || matching.length == 0) {
                    failed = true;
                    break;
                }
                ItemMatcher matcher = new ItemMatcher(Arrays.asList(matching));
                if (candidate.decrement(matcher)) continue;

                boolean craftedIngredient = false;
                for (ItemStack choice : matcher.getValidItems()) {
                    AtomicReference<ItemStack> nestedOutput = new AtomicReference<>();
                    SerializedInventory nested = attemptToCraft(
                            world, choice, candidate, nestedOutput, depth + 1);
                    if (nested != null) {
                        ItemStack surplus = nestedOutput.get();
                        if (surplus != null && surplus.getCount() > 1) {
                            surplus = surplus.copy();
                            surplus.shrink(1);
                            nested.add(surplus);
                        }
                        candidate = nested;
                        craftedIngredient = true;
                        break;
                    }
                }
                if (!craftedIngredient) {
                    failed = true;
                    break;
                }
            }
            if (!failed) {
                output.set(recipe.getRecipeOutput().copy());
                return candidate;
            }
        }
        return null;
    }
}
