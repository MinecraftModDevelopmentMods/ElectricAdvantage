package zone.moddev.mc.electricadvantage.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.loot.LootContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/** Hidden pump intake column. */
public class PumpPipeBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.box(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);
    public PumpPipeBlock() { super(Properties.of(Material.METAL).strength(5.0F, 10.0F).dynamicShape().noOcclusion().isRedstoneConductor((state, world, pos) -> false).noDrops()); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public java.util.List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return java.util.Collections.emptyList(); }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) destroyBelow(world, pos);
        super.onRemove(state, world, pos, newState, moving);
    }

    @Override public void wasExploded(World world, BlockPos pos, Explosion explosion) {
        destroyBelow(world, pos);
        super.wasExploded(world, pos, explosion);
    }

    private void destroyBelow(World world, BlockPos pos) {
        if (world.isClientSide) return;
        BlockPos cursor = pos.below();
        while (cursor.getY() > 0 && world.getBlockState(cursor).getBlock() == this) {
            world.setBlock(cursor, net.minecraft.block.Blocks.AIR.defaultBlockState(), 3);
            cursor = cursor.below();
        }
    }
}
