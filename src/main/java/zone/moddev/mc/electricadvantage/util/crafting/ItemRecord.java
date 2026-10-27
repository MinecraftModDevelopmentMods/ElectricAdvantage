package zone.moddev.mc.electricadvantage.util.crafting;

import net.minecraft.item.ItemStack;

/** Flattened-item key used by the fabricator's serialized inventory. */
public final class ItemRecord {
    private final ItemStack item;
    private final int hashCode;

    public ItemRecord(ItemStack stack) {
        this.item = stack.copy();
        this.item.setCount(1);
        this.hashCode = item.getItem().hashCode();
    }

    public ItemStack getItem() {
        return item;
    }

    @Override public int hashCode() { return hashCode; }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ItemRecord
                && ItemStack.isSame(item, ((ItemRecord) other).item);
    }
}
