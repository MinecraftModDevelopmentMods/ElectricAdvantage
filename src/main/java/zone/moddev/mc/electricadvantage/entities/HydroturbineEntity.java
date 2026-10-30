package zone.moddev.mc.electricadvantage.entities;

import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.electricadvantage.machines.HydroelectricGeneratorTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
		this.preventEntitySpawning = true;
		roffset = w.rand.nextInt(8);
	}
	public HydroturbineEntity(World w, BlockPos parentTileEntity) {
		this(w, w.getTileEntity(parentTileEntity));
	}

	public HydroturbineEntity(World w, TileEntity parentTileEntity) {
		this(zone.moddev.mc.electricadvantage.init.Entities.HYDROTURBINE, w);
		this.parent = parentTileEntity;
		BlockPos p = parentTileEntity.getPos();
		this.setPosition(p.getX()+0.5, p.getY()-1, p.getZ()+0.5);
	}

	@Override
	protected void registerData() {
		// do nothing
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public boolean isPushedByWater() {
		// Anchored to the generator: flow drives rotation, not linear movement.
		return false;
	}

	@Override
	public void tick(){
		super.tick();
		IFluidState fluidState = this.getEntityWorld().getFluidState(getPosition());
		Vec3d directionVector;
		if(!fluidState.isEmpty()){
			directionVector = fluidState.getFlow(getEntityWorld(), getPosition());
			isSpinning = !(directionVector.x == 0 && directionVector.z == 0);
		} else {
			isSpinning = false;
			directionVector = new Vec3d(0,0,0);
		}
		if(this.getEntityWorld().isRemote){
			// client-side only
			if(isSpinning){
				rotation += DEGREES_PER_TICK;
				this.rotationYaw = RADIANS_TO_DEGREES * (float)Math.atan2(-directionVector.z, directionVector.x) -90;
			} else {
				// do nothing
			}
		} else {
			// server-side only
			if(parent == null && parentCoord != null){
				parent = this.getEntityWorld().getTileEntity(parentCoord);
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
			if(isSpinning && getEntityWorld().getGameTime() % 8 == roffset){
				// slice-n-dice unfortunate souls caught in the blades
				List<LivingEntity> victims = getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox());
				for(Entity e : victims){
					e.attackEntityFrom(Power.propeller_damage, PROPELLER_DAMAGE);
				}
			}
		}
	}
	
	@Override
	protected void readAdditional(CompoundNBT root) {
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
	protected void writeAdditional(CompoundNBT root) {
		BlockPos p = parent != null ? parent.getPos() : parentCoord;
		if(p != null){
			int[] coords = new int[3];
			coords[0] = p.getX();
			coords[1] = p.getY();
			coords[2] = p.getZ();
			root.putIntArray("parentPos", coords);
		}
	}
	
}
