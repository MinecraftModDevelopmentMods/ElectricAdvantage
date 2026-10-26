package zone.moddev.mc.electricadvantage.blocks;

import java.util.Random;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

/** Legacy ElectricAdvantage ore block retaining its original drops and harvest level. */
public class Ore extends Block {
    private final ItemStack dropItem;
    private final int dropRange;
    private final int harvestLevel;

    public Ore(ItemStack oreDrop, int pickLevel, int variation) {
        super(Properties.create(Material.ROCK).sound(SoundType.STONE)
                .hardnessAndResistance(1.5F + pickLevel, 5.0F * (1 + pickLevel)));
        dropItem = oreDrop.copy();
        dropRange = variation + 1;
        harvestLevel = pickLevel;
    }

    public Ore(int pickLevel) {
        super(Properties.create(Material.ROCK).sound(SoundType.STONE)
                .hardnessAndResistance(1.5F + pickLevel, 5.0F * (1 + pickLevel)));
        dropItem = ItemStack.EMPTY;
        dropRange = 0;
        harvestLevel = pickLevel;
    }

    @Override public ToolType getHarvestTool(BlockState state) { return ToolType.PICKAXE; }
    @Override public int getHarvestLevel(BlockState state) { return harvestLevel; }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) {
        ItemStack tool = context.get(LootParameters.TOOL);
        if (dropItem.isEmpty() || (tool != null
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0)) {
            return Collections.singletonList(new ItemStack(this));
        }
        Random random = context.getWorld().rand;
        ItemStack result = dropItem.copy();
        result.setCount(dropItem.getCount() + (dropRange > 1 ? random.nextInt(dropRange) : 0));
        return Collections.singletonList(result);
    }
}
