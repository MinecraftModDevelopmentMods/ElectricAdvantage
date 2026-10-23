package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LaserTurretBlock extends ElectricMachineBlock {

	private static final VoxelShape SHAPE = VoxelShapes.or(
			Block.makeCuboidShape(0, 0, 0, 16, 4, 16),
			Block.makeCuboidShape(4, 4, 4, 12, 16, 12));

	// Evil laser turrets target players and not hostile mobs
	public final boolean evil;
	public LaserTurretBlock(boolean isEvil) {
		super(Block.Properties.create(Material.PISTON)
				.hardnessAndResistance(5.0F, 100.0F).variableOpacity());
		this.evil = isEvil;
	}

	@Override
	public ElectricMachineTileEntity createNewTileEntity(net.minecraft.world.IBlockReader world) {
		return new LaserTurretTileEntity();
	}




	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos) {
		return SHAPE;
	}

	@Override
	public void onBlockPlacedBy(World w, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(w, pos, state, placer, stack);
		TileEntity te = w.getTileEntity(pos);
		if(placer instanceof EntityPlayer && te instanceof LaserTurretTileEntity){
			((LaserTurretTileEntity)te).setOwner((EntityPlayer)placer);
		}
	}

}
