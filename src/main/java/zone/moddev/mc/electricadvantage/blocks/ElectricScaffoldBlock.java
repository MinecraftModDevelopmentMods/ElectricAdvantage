package zone.moddev.mc.electricadvantage.blocks;

import cyano.poweradvantage.api.simple.BlockSimplePowerConduit;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import zone.moddev.mc.electricadvantage.init.Power;

/** Full-block electric track/scaffold with conduit behavior. */
public class ElectricScaffoldBlock extends BlockSimplePowerConduit {
    public ElectricScaffoldBlock() {
        super(Properties.of(Material.PISTON).strength(0.75F).noOcclusion()
                .isRedstoneConductor((state, world, pos) -> false),
                2.0F / 16.0F, Power.ELECTRIC_POWER);
    }
    @Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return VoxelShapes.block(); }
    @Override public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) { return VoxelShapes.block(); }
}
