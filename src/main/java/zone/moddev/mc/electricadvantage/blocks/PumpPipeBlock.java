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
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/** Hidden pump intake column. */
public class PumpPipeBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.create(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);
    public PumpPipeBlock() { super(Properties.create(Material.IRON).hardnessAndResistance(5.0F, 10.0F).variableOpacity().noDrops()); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return SHAPE; }
    @Override public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) { return false; }
    @Override public boolean isSolid(BlockState state) { return false; }
    @Override public java.util.List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return java.util.Collections.emptyList(); }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) destroyBelow(world, pos);
        super.onReplaced(state, world, pos, newState, moving);
    }

    @Override public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        destroyBelow(world, pos);
        super.onExplosionDestroy(world, pos, explosion);
    }

    private void destroyBelow(World world, BlockPos pos) {
        if (world.isRemote) return;
        BlockPos cursor = pos.down();
        while (cursor.getY() > 0 && world.getBlockState(cursor).getBlock() == this) {
            world.setBlockState(cursor, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
            cursor = cursor.down();
        }
    }
}
