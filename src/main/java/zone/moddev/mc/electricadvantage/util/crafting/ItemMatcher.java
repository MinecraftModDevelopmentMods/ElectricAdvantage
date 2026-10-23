package zone.moddev.mc.electricadvantage.util.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

/** Matches a crafting ingredient using flattened items or Forge item tags. */
public final class ItemMatcher {
    private final List<ItemStack> choices;

    public ItemMatcher(String legacyName) {
        this.choices = new ArrayList<>();
        for (Item item : ItemTags.getCollection().getOrCreate(tagId(legacyName)).getAllElements()) {
            choices.add(new ItemStack(item));
        }
    }

    public ItemMatcher(Item item) {
        this(new ItemStack(item));
    }

    public ItemMatcher(Block block) {
        this(new ItemStack(block));
    }

    public ItemMatcher(ItemStack item) {
        this.choices = Collections.singletonList(item.copy());
    }

    public ItemMatcher(List<?> expandedList) {
        this.choices = new ArrayList<>(expandedList.size());
        for (Object value : expandedList) {
            if (!(value instanceof ItemStack)) {
                throw new ClassCastException(getClass().getName() + " does not support " + value.getClass());
            }
            choices.add(((ItemStack) value).copy());
        }
    }

    public boolean matches(ItemRecord record) {
        return matches(record.getItem());
    }

    public boolean matches(ItemStack candidate) {
        if (candidate == null || candidate.isEmpty()) return false;
        for (ItemStack choice : choices) {
            if (ItemStack.areItemsEqual(choice, candidate)) return true;
        }
        return false;
    }

    public Collection<ItemStack> getValidItems() {
        List<ItemStack> copy = new ArrayList<>(choices.size());
        for (ItemStack choice : choices) copy.add(choice.copy());
        return copy;
    }

    private static ResourceLocation tagId(String name) {
        if (name.indexOf(':') >= 0) return new ResourceLocation(name);
        String[][] forms = {
                {"ingot", "ingots"}, {"nugget", "nuggets"}, {"dust", "dusts"},
                {"ore", "ores"}, {"block", "storage_blocks"}, {"plate", "plates"},
                {"wire", "wires"}, {"gem", "gems"}
        };
        for (String[] form : forms) {
            if (name.startsWith(form[0]) && name.length() > form[0].length()) {
                String material = name.substring(form[0].length()).toLowerCase(java.util.Locale.ROOT);
                return new ResourceLocation("forge", form[1] + "/" + material);
            }
        }
        return new ResourceLocation("forge", name.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public String toString() {
        return Arrays.toString(choices.toArray());
    }
}
