package zone.moddev.mc.electricadvantage.machines;

import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.init.Power;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class LaserTurretTileEntity extends ElectricMachineTileEntity implements ITickableTileEntity{

	private final SoundEvent LASER_SOUND;

	private static final float RADIANS_TO_DEGREES = (float)(180 / Math.PI);
	private static final float DEGREES_TO_RADIANS = (float)(Math.PI / 180);
	public final static float IDLE_ROTATION_PER_TICK = 2f; // in degrees per tick
	public static final byte LASER_CHARGEUP_TIME = 31;
	private static final byte BLAST_TIME = 10;
	private static final byte HIT_TIME = 7;
	private final static int BURN_TIME = 3;
	public static float ATTACK_DAMAGE = 7f;
	
	public final static float ENERGY_PER_SHOT = 1000;
	public final static float ENERGY_PER_TICK = 1;
	public final static float MAX_BUFFER = ENERGY_PER_SHOT * 2;
	
	private String teamIdentity = null;
	private String playerOwner = null;

	public static final int BLAME_RANGE = 4;
	public static final int TARGET_RANGE = 16;
	public static final int ATTACK_RANGE = 32;
	/** target rotation around Y axis */
	public float rotTargetYaw=0;
	/** target rotation up and down */
	public float rotTargetPitch=0;
	/** rotation around Y axis */
	public float rotYaw=0;
	/** rotation up and down */
	public float rotPitch=0;
	/** previous rotation around Y axis */
	public float rotOldYaw=0;
	/** previous rotation up and down */
	public float rotOldPitch=0;
	/** true when tracking a target, alse otherwise */
	public boolean targetLocked = false;
	/** entityID of target */
	private int targetID = Integer.MIN_VALUE;
	/** laser attack charge-up */
	private int laserAttack = 0;
	/** used by renderer to draw laser line */
	public double laserBlastLength = 0;
	
	private Vec3d opticPosition = null;
	
	/** rotation speed (non-idle), in degrees per tick */
	public final float speed = 9f;


	public LaserTurretTileEntity(){
		super(zone.moddev.mc.electricadvantage.init.Entities.LASER_TURRET, "tile.electricadvantage.laser_turret.name",MAX_BUFFER,0,0,0);
		SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(ElectricAdvantage.INSTANCE.LASER_SOUND));
		if(soundEvent == null){
			LASER_SOUND = SoundEvents.ENTITY_ENDERMAN_TELEPORT;
		} else {
			LASER_SOUND = soundEvent;
		}
	}

	@Override
	public boolean isActive(){
		return  (this.getEnergy() >= ENERGY_PER_SHOT) && (!this.hasRedstoneSignal());
	}
	
	@Override
	public void tickUpdate(boolean b) {
		World w = getWorld();
		if(laserAttack > 0){
			laserAttack--;
		}
		boolean isActive = isActive();
		if(w.isRemote){
			// client-side only
			if(isActive){
				if(targetLocked == false){
					storeOld();
					rotPitch = 0;
					rotYaw += IDLE_ROTATION_PER_TICK;
					if(rotYaw > 180 ){
						rotYaw -= 360;
						rotOldYaw -= 360;
					}
				} else {
					storeOld();
					Entity e = w.getEntityByID(targetID);
					if(e != null){
						lookAt(e);
					}
					if((rotTargetYaw - rotYaw) > 180){
						rotYaw += 360;
						rotOldYaw += 360;
					} else if((rotTargetYaw - rotYaw) < -180){
						rotYaw -= 360;
						rotOldYaw -= 360;
					}
					rotYaw = clampDelta(rotYaw,rotTargetYaw,speed);
					rotPitch = clampDelta(rotPitch,rotTargetPitch,speed);
					
					// Laser attack animation
					if(laserAttack > 0 && laserAttack <= BLAST_TIME){
						drawLaserLine();
					}
				}
			} else {
				storeOld();
				if(rotPitch > -30){
					rotPitch -= IDLE_ROTATION_PER_TICK;
				}
			}
			
		} else {
			// Server side only
			if(isActive){
				if(targetLocked == false){
					final Block thisBlock = getWorld().getBlockState(getPos()).getBlock();
					final boolean isEvil;
					if(thisBlock instanceof LaserTurretBlock
							&& ((LaserTurretBlock)thisBlock).evil){
						isEvil = true;
					} else {
						isEvil = false;
					}
					double x = this.getOpticPosition().x;
					double y = this.getOpticPosition().y;
					double z = this.getOpticPosition().z;
					List<LivingEntity> entities = w.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(x-TARGET_RANGE, y-TARGET_RANGE, z-TARGET_RANGE, x+TARGET_RANGE, y+TARGET_RANGE, z+TARGET_RANGE));
					final double maxRangeSquared = TARGET_RANGE * TARGET_RANGE;
					if(isEvil){
						for (Entity e : entities) {
							if (!(
									e.getType().getClassification() == EntityClassification.MONSTER
									|| e.getType().getClassification() == EntityClassification.AMBIENT
								)
							) {
								if (canSeeEntity(e)) {
									setTarget(e);
									break;
								}
							}
						}
					} else {
						for (Entity e : entities) {
							if (e.getPositionVector().squareDistanceTo(getOpticPosition()) > maxRangeSquared) continue;
							if (e instanceof PlayerEntity) {
								if (isEnemy((PlayerEntity) e)) {
									setTarget(e);
									break;
								}
							}
							if (e.getType().getClassification() == EntityClassification.MONSTER) {
								if (canSeeEntity(e)) {
									setTarget(e);
									break;
								}
							}
						}
					}
				} 

				validateTarget();
				if(targetLocked ){
					if(laserAttack == HIT_TIME){
						// dish out the laser damage
						playSoundEffect(getOpticPosition().x, getOpticPosition().y, getOpticPosition().z,
								LASER_SOUND,1F,1F);
						List<Entity> victims = getLaserAttackVictims();
						Entity e = w.getEntityByID(targetID);
						for(Entity v : victims){
							hurtVictim(v);
						}
						this.subtractEnergy(ENERGY_PER_SHOT, getEnergyType());
						if(e == null || !e.isAlive()){
							setTarget(null);
						}
						this.sync();
					} else if(laserAttack == 1){
						laserAttack = LASER_CHARGEUP_TIME;
						this.sync();
					}
				}
				this.subtractEnergy(ENERGY_PER_TICK, getEnergyType());
			} else {
				if(targetLocked){
					setTarget(null);
				}
			}

		}
	}
	
	private boolean isEnemy(PlayerEntity player) {
		if(player.isCreative()) return false;
		if(this.teamIdentity == null || this.teamIdentity.isEmpty()){
			return false;
		} else if(player.getTeam() == null){
			return true;
		} else{
			return !teamIdentity.equals(player.getTeam().getName());
		}
	}

	
	public boolean showLaserLine(){
		return targetLocked && laserAttack > 0 && laserAttack <= BLAST_TIME;
	}
	
	
	private List<Entity> getLaserAttackVictims(){
		final Vec3d origin = getOpticPosition();
		final Vec3d dir;
		final World w = getWorld();
		final Entity e = w.getEntityByID(targetID);
		if(e != null){
			dir = e.getPositionVector().add(0, 0.5*e.getHeight(), 0).subtract(origin).normalize();
		} else {
			dir = (new Vec3d(Math.cos(DEGREES_TO_RADIANS * rotYaw),Math.sin(rotPitch),Math.sin(rotYaw))).normalize();
		}
		final Vec3d terminus = followRayToSolidBlock(origin,dir,ATTACK_RANGE);
		final double maxDistSqr = origin.squareDistanceTo(terminus);
		final double maxDist = Math.sqrt(maxDistSqr);
		List<LivingEntity> potentialVictims = w.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(
				origin.x - maxDist, origin.y - maxDist, origin.z - maxDist,
				origin.x + maxDist, origin.y + maxDist, origin.z + maxDist));
		List<Entity> victims = new ArrayList<>();
		for(Entity v : potentialVictims){
			if(origin.squareDistanceTo(v.getPositionVector()) < maxDistSqr
					&& rayIntersectsBoundingBox(origin,dir,v.getBoundingBox())){
				victims.add(v);
			}
		}
		return victims;
	}
	
	

	private void hurtVictim(Entity e){
		e.setFire(BURN_TIME);
		if(e instanceof LivingEntity){
			((LivingEntity)e).attackEntityFrom(Power.laser_damage, ATTACK_DAMAGE);
			BlockPos bp = e.getPosition();
			if(getWorld().isAirBlock(bp) && !getWorld().isAirBlock(bp.down())){
				getWorld().setBlockState(bp, Blocks.FIRE.getDefaultState());
			}
			if(playerOwner != null && !playerOwner.isEmpty()){
				for (PlayerEntity player : getWorld().getPlayers()) {
					if (playerOwner.equals(player.getName().getString())) {
						((LivingEntity) e).setRevengeTarget(player);
						break;
					}
				}
			}
		}
	}
	
	private Vec3d calculateLaserTerminus(){
		Vec3d dir;
		World w = getWorld();
		Entity e = w.getEntityByID(targetID);
		if(e != null){
			dir = e.getPositionVector().add(0, 0.5*e.getHeight(), 0).subtract(this.getOpticPosition()).normalize();
		} else {
			dir = (new Vec3d(Math.cos(DEGREES_TO_RADIANS * rotYaw),Math.sin(rotPitch),Math.sin(rotYaw))).normalize();
		}
		return followRayToSolidBlock(getOpticPosition(),dir,ATTACK_RANGE);
	}
	private Vec3d followRayToSolidBlock(Vec3d origin, Vec3d dir, double maxRange){
		Vec3d max = origin.add(dir).add(maxRange * dir.x, maxRange * dir.y, maxRange * dir.z);
		BlockRayTraceResult impact = getWorld().rayTraceBlocks(new RayTraceContext(origin, max,
				RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.SOURCE_ONLY, null));
		if(impact.getType() == RayTraceResult.Type.BLOCK){
			return impact.getHitVec();
		} else {
			return max;
		}
	}
	
	public static boolean rayIntersectsBoundingBox(Vec3d rayOrigin, Vec3d rayDirection, AxisAlignedBB box){
		if(box == null) {
			return false;
		}
		// algorithm from http://gamedev.stackexchange.com/questions/18436/most-efficient-aabb-vs-ray-collision-algorithms
		Vec3d inverse = new Vec3d(1.0 / rayDirection.x, 1.0 / rayDirection.y, 1.0 / rayDirection.z);
		double t1 = (box.minX - rayOrigin.x)*inverse.x;
		double t2 = (box.maxX- rayOrigin.x)*inverse.x;
		double t3 = (box.minY - rayOrigin.y)*inverse.y;
		double t4 = (box.maxY - rayOrigin.y)*inverse.y;
		double t5 = (box.minZ - rayOrigin.z)*inverse.z;
		double t6 = (box.maxZ - rayOrigin.z)*inverse.z;

		double tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
		double tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behind us
		if (tmax < 0)
		{
			return false;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax)
		{
			return false;
		}

		return true;
	}

	
	private static Vec3d mul(Vec3d a, double b){
		return new Vec3d(a.x * b, a.y * b, a.z * b);
	}
	private static double max(double a, double b){
		return Math.max(a, b);
	}
	private static double min(double a, double b){
		return Math.min(a, b);
	}
	
	
	private void drawLaserLine() {
		// client-side only
		World w = getWorld();
		if(w.isRemote){
			this.laserBlastLength = getOpticPosition().distanceTo(calculateLaserTerminus());
		}
	}

	private void validateTarget() {
		if(targetLocked){
			Entity e = getWorld().getEntityByID(targetID);
			if(e == null || !e.isAlive()){
				setTarget(null);
				return;
			}
			
			if(getOpticPosition().distanceTo(e.getPositionVector()) > TARGET_RANGE){
				setTarget(null);
				return;
			}
			
			if(!canSeeEntity(e)){
				setTarget(null);
				return;
			}
		}
	}
	
	public boolean canSeeEntity(Entity e){
		if(e == null) return false;
		Vec3d offsetOrigin = getOpticPosition().add(e.getPositionVector().subtract(getOpticPosition()).normalize());
		BlockRayTraceResult collision = getWorld().rayTraceBlocks(new RayTraceContext(offsetOrigin,
				e.getPositionVector().add(0, e.getHeight()*0.5, 0), RayTraceContext.BlockMode.COLLIDER,
				RayTraceContext.FluidMode.SOURCE_ONLY, null));
		if(collision.getType() == RayTraceResult.Type.BLOCK){
			return false;
		}
		return true;
	}

	public void setTarget(Entity e){
		if(e == null){
			this.targetID = Integer.MIN_VALUE;
			this.targetLocked = false;
			laserAttack = 0;
		} else {
			this.targetID = e.getEntityId();
			this.targetLocked = true;
			laserAttack = LASER_CHARGEUP_TIME;
			lookAt(e);
		}
		this.sync();
	}

	private void lookAt(Entity e) {
		Vec3d pos = e.getPositionVector().add(0, e.getHeight()*0.5, 0);
		targetPosition(pos);
	}
	
	private Vec3d getOpticPosition(){
		if(opticPosition == null)setOpticPosition();
		return opticPosition;
	}
	
	public void setOwner(PlayerEntity owner){
		this.setTeam(owner.getTeam());
		playerOwner = owner.getName().getString();
	}
	
	public void setTeam(Team t){
		if(t == null) {
			teamIdentity = null;
		} else {
			teamIdentity = t.getName();
		}
	}
	
	@Override 
	public void setPos(BlockPos p){
		super.setPos(p);
		setOpticPosition();
	}
	public final void setOpticPosition(){
		BlockPos p = getPos();
		opticPosition = new Vec3d(p.getX()+0.5,p.getY()+0.75,p.getZ()+0.5);
	}
	
	private void targetPosition(Vec3d coord) {
		Vec3d pos = getOpticPosition();
		double x = pos.x;
		double y = pos.y;
		double z = pos.z;
		double dist = distance(x,y,z,coord.x, coord.y, coord.z);
		this.rotTargetYaw = RADIANS_TO_DEGREES * atan2(coord.z - z, coord.x - x);
		this.rotTargetPitch = RADIANS_TO_DEGREES * asin((coord.y - y)/dist);
	}
	
	private double distance(double x1,double y1,double z1,double x2,double y2,double z2){
		double dx = x2-x1;
		double dy = y2-y1;
		double dz = z2-z1;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	private float atan2(double dy, double dx){
		return (float)Math.atan2(dy, dx);
	}
	
	private float asin(double d){
		return (float)Math.asin(d);
	}

	private static float clampDelta(float old, float goal, float maxDelta){
		float delta = goal - old;
		if(Math.abs(delta) > delta){
			if(delta < 0){
				delta = -1*maxDelta;
			} else {
				delta = maxDelta;
			}
			return old + delta;
		}else{
			return goal;
		}
	}
	
	private void storeOld(){
		rotOldYaw = rotYaw;
		rotOldPitch = rotPitch;
	}
	
	
	
	

	/**
	 * Turns the data field NBT into a network packet
	 */
	@Override 
	public SUpdateTileEntityPacket getUpdatePacket(){
		CompoundNBT nbtTag = new CompoundNBT();
		writeRenderSync(nbtTag);
		return new SUpdateTileEntityPacket(this.getPos(), 0, nbtTag);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtTag = write(new CompoundNBT());
		writeRenderSync(nbtTag);
		return nbtTag;
	}

	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		read(tag);
		readRenderSync(tag);
	}

	private void writeRenderSync(CompoundNBT nbtTag) {
		nbtTag.putFloat("energy", getEnergy());
		nbtTag.putBoolean("lock", targetLocked);
		if(targetLocked){
			if(targetID != Integer.MIN_VALUE && getWorld().getEntityByID(targetID) != null){
				Entity e = getWorld().getEntityByID(targetID);
				Vec3d pos = e.getPositionVector().add(0, e.getHeight()*0.5, 0);
				CompoundNBT target = new CompoundNBT();
				target.putInt("id", this.targetID);
				target.putFloat("yaw", rotTargetYaw);
				target.putFloat("pitch", rotTargetPitch);
				target.putByte("attack", (byte)laserAttack);
				nbtTag.put("target", target);
			}
		}
	}

	/**
	 * Receives the network packet made by <code>getDescriptionPacket()</code>
	 */
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		readRenderSync(packet.getNbtCompound());
	}

	private void readRenderSync(CompoundNBT tag) {
		if(tag.contains("lock", 1)){
			this.targetLocked = tag.getBoolean("lock");
			if(tag.contains("target", 10)){
				CompoundNBT target = tag.getCompound("target");
				this.targetID = target.getInt("id");
				Entity e = getWorld().getEntityByID(targetID);
				if(e != null){
					this.lookAt(e);
				} else {
					this.rotTargetYaw = target.getFloat("yaw");
					this.rotTargetPitch = target.getFloat("pitch");
				}
				this.laserAttack = target.getByte("attack");
			}
		}
		if(tag.contains("energy", 99)){
			this.setEnergy(tag.getFloat("energy"),getEnergyType());
		}
	}

	@Override
	public float[] getProgress() {
		// not used
		return new float[0];
	}

	@Override
	protected void saveTo(CompoundNBT root) {
		root.putFloat("pitch", rotPitch);
		root.putFloat("yaw", rotYaw);
		root.putBoolean("lock", targetLocked);
		if(this.teamIdentity == null){
			root.putString("team", "");
		} else {
			root.putString("team", teamIdentity);
		}
		if(this.playerOwner == null){
			root.putString("player", "");
		} else {
			root.putString("player", playerOwner);
		}
	}

	@Override
	protected void loadFrom(CompoundNBT root) {
		if(root.contains("pitch", 99)){
			this.rotPitch = root.getFloat("pitch");
			this.rotOldPitch = rotPitch;
			this.rotTargetPitch = rotPitch;
		}
		if(root.contains("yaw", 99)){
			this.rotYaw = root.getFloat("yaw");
			this.rotOldYaw = rotYaw;
			this.rotTargetYaw = rotYaw;
		}
		if(root.contains("lock", 1)){
			targetLocked = root.getBoolean("lock");
		}
		if(root.contains("team", 8)){
			String teamName = root.getString("team");
			if(teamName.isEmpty()){
				this.teamIdentity = null;
			}else{
				this.teamIdentity = teamName;
			}
		}

		if(root.contains("player", 8)){
			playerOwner = root.getString("player");
		}
	}

	@Override
	protected boolean isValidInputItem(ItemStack item) {
		return false;
	}

	@Override
	public int[] getDataFieldArray() {
		// not used
		return new int[0];
	}

	@Override
	public void onDataFieldUpdate() {
		// not used
	}

	@Override
	public void prepareDataFieldsForSync() {
		// not used
	}

	@Override
	public boolean isPowered() {
		return getEnergy() > ENERGY_PER_TICK;
	}
	
	@Override
	public int getComparatorOutput(){
		if(this.targetLocked){
			return 15;
		} else {
			return (int)(8 * (this.getEnergy() / this.getEnergyCapacity()));
		}
	}
	
	// Helps with laser rendering
	final private int renderRange = ATTACK_RANGE * 2;
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(getPos().add(-renderRange, -renderRange, -renderRange), getPos().add(renderRange, renderRange, renderRange));
	}
}
