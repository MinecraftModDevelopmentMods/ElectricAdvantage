package zone.moddev.mc.electricadvantage.entities;

import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.electricadvantage.machines.HydroelectricGeneratorTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;

public class HydroturbineEntity extends net.minecraft.entity.Entity{

	public static final float DEGREES_PER_TICK = 30;
	private static final float RADIANS_TO_DEGREES = (float)(180 / Math.PI);
	private static final float DEGREES_TO_RADIANS = (float)(Math.PI / 180);
	public static final float PROPELLER_DAMAGE = 5f;
	public float rotation = 0;
	public boolean isSpinning = false;
	public TileEntity parent = null;
	private BlockPos parentCoord = null;
	private final long roffset;
	
	public HydroturbineEntity(EntityType<?> type, World w) {
		super(type, w);
		this.blocksBuilding = true;
		roffset = w.random.nextInt(8);
	}
	public HydroturbineEntity(World w, BlockPos parentTileEntity) {
		this(w, w.getBlockEntity(parentTileEntity));
	}

	public HydroturbineEntity(World w, TileEntity parentTileEntity) {
		this(zone.moddev.mc.electricadvantage.init.Entities.HYDROTURBINE, w);
		this.parent = parentTileEntity;
		BlockPos p = parentTileEntity.getBlockPos();
		this.setPos(p.getX()+0.5, p.getY()-1, p.getZ()+0.5);
	}

	@Override
	protected void defineSynchedData() {
		// do nothing
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public boolean isPushedByFluid() {
		// This is anchored to its generator; flow drives rotation, not linear movement.
		return false;
	}

	@Override
	public void tick(){
		super.tick();
		FluidState fluidState = this.getCommandSenderWorld().getFluidState(blockPosition());
		Vector3d directionVector;
		if(!fluidState.isEmpty()){
			directionVector = fluidState.getFlow(getCommandSenderWorld(), blockPosition());
			isSpinning = !(directionVector.x == 0 && directionVector.z == 0);
		} else {
			isSpinning = false;
			directionVector = new Vector3d(0,0,0);
		}
		if(this.getCommandSenderWorld().isClientSide){
			// client-side only
			if(isSpinning){
				rotation += DEGREES_PER_TICK;
				this.yRot = RADIANS_TO_DEGREES * (float)Math.atan2(-directionVector.z, directionVector.x) -90;
			} else {
				// do nothing
			}
		} else {
			// server-side only
			if(parent == null && parentCoord != null){
				parent = this.getCommandSenderWorld().getBlockEntity(parentCoord);
				parentCoord = null;
			}
			if(parent == null || parent.isRemoved()){
				this.remove();
			} else if(parent instanceof HydroelectricGeneratorTileEntity){
				HydroelectricGeneratorTileEntity generator = (HydroelectricGeneratorTileEntity)parent;
				if(isSpinning){
					generator.addEnergy(HydroelectricGeneratorTileEntity.ENERGY_PER_TICK, Power.ELECTRIC_POWER);
				}
				generator.setActive(isSpinning);
			}
			if(isSpinning && getCommandSenderWorld().getGameTime() % 8 == roffset){
				// slice-n-dice unfortunate souls caught in the blades
				List<LivingEntity> victims = getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
				for(Entity e : victims){
					e.hurt(Power.propeller_damage, PROPELLER_DAMAGE);
				}
			}
		}
	}
	
	@Override
	protected void readAdditionalSaveData(CompoundNBT root) {
		parent = null;
		parentCoord = null;
		if(root.contains("parentPos", 11)){
			int[] coords = root.getIntArray("parentPos");
			if(coords.length >= 3){
				parentCoord = new BlockPos(coords[0],coords[1],coords[2]);
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT root) {
		BlockPos p = parent != null ? parent.getBlockPos() : parentCoord;
		if(p != null){
			int[] coords = new int[3];
			coords[0] = p.getX();
			coords[1] = p.getY();
			coords[2] = p.getZ();
			root.putIntArray("parentPos", coords);
		}
	}
	
}
