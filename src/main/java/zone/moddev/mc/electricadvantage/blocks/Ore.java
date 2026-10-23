package zone.moddev.mc.electricadvantage.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
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

    @Override public ToolType getHarvestTool(IBlockState state) { return ToolType.PICKAXE; }
    @Override public int getHarvestLevel(IBlockState state) { return harvestLevel; }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        if (dropItem.isEmpty()) {
            drops.add(new ItemStack(this));
            return;
        }
        Random random = world.rand;
        ItemStack result = dropItem.copy();
        result.setCount(dropItem.getCount() + (dropRange > 1 ? random.nextInt(dropRange) : 0));
        drops.add(result);
    }
}
