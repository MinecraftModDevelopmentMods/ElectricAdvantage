package zone.moddev.mc.electricadvantage.blocks;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import zone.moddev.mc.electricadvantage.init.Power;

/** Full-block electric track/scaffold with conduit behavior. */
public class ElectricScaffoldBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit {
    public ElectricScaffoldBlock() { super(Material.PISTON, 0.75F, 2.0F / 16.0F, Power.ELECTRIC_POWER); }
    @Override public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) { return VoxelShapes.fullCube(); }
    @Override public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) { return VoxelShapes.fullCube(); }
    @Override public boolean isNormalCube(IBlockState state) { return false; }
    @Override public boolean isSolid(IBlockState state) { return false; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
