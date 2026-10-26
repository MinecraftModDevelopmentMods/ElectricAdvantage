package zone.moddev.mc.electricadvantage.blocks;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import zone.moddev.mc.electricadvantage.init.Power;

/** Full-block electric track/scaffold with conduit behavior. */
public class ElectricScaffoldBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit {
    public ElectricScaffoldBlock() { super(Material.PISTON, 0.75F, 2.0F / 16.0F, Power.ELECTRIC_POWER); }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return VoxelShapes.fullCube(); }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return VoxelShapes.fullCube(); }
    @Override public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) { return false; }
    @Override public boolean isSolid(BlockState state) { return false; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
