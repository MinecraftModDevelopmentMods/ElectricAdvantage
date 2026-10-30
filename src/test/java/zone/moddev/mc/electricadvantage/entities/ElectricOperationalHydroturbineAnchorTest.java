package zone.moddev.mc.electricadvantage.entities;

import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.server.ServerWorld;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectricOperationalHydroturbineAnchorTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void nativeEntityLoadStillClampsOverspeedMotionAndPreservesItsBoundary() throws Exception {
        ControlEntity control = new ControlEntity(controlType(), world());
        CompoundNBT tag = control.writeWithoutTypeId(new CompoundNBT());
        tag.put("Motion", motion(16.226, -10.001, 10));
        control.read(tag);
        assertEquals(new Vec3d(0, 0, 10), control.getMotion());
        assertTrue(control.isPushedByWater());
    }

    @Test public void turbineRejectsLinearWaterPushingWithoutChangingRotationRate() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(turbineType(), world());
        assertFalse(turbine.isPushedByWater());
        assertEquals(Vec3d.ZERO, turbine.getMotion());
        assertEquals(30, HydroturbineEntity.DEGREES_PER_TICK, 0);
    }

    @Test public void legacyMotionInsideTheNativeLimitIsNotArbitrarilyCleared() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(turbineType(), world());
        CompoundNBT saved = turbine.writeWithoutTypeId(new CompoundNBT());
        saved.put("Motion", motion(5.5, -2, 0));
        turbine.read(saved);
        assertEquals(new Vec3d(5.5, -2, 0), turbine.getMotion());
        assertFalse(turbine.isPushedByWater());
    }

    @Test public void anchoredTurbineSurvivesNativeSaveLoadWithItsParentAndIdentity() throws Exception {
        EntityType<HydroturbineEntity> type = turbineType();
        World world = world();
        HydroturbineEntity original = new HydroturbineEntity(type, world);
        original.setPosition(56856.5, 81, 50008.5);
        CompoundNBT saved = original.writeWithoutTypeId(new CompoundNBT());
        int[] parent = {56856, 82, 50008};
        saved.putIntArray("parentPos", parent);
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity loaded = new HydroturbineEntity(type, world);
            loaded.read(saved);
            CompoundNBT next = loaded.writeWithoutTypeId(new CompoundNBT());
            assertEquals(saved.getUniqueId("UUID"), next.getUniqueId("UUID"));
            assertEquals(saved.getList("Pos", 6), next.getList("Pos", 6));
            assertEquals(motion(0, 0, 0), next.getList("Motion", 6));
            assertArrayEquals(parent, next.getIntArray("parentPos"));
            assertFalse(loaded.isPushedByWater());
            saved = next;
        }
    }

    private static ListNBT motion(double x, double y, double z) {
        ListNBT list = new ListNBT();
        list.add(new DoubleNBT(x)); list.add(new DoubleNBT(y)); list.add(new DoubleNBT(z));
        return list;
    }

    private static EntityType<HydroturbineEntity> turbineType() {
        return EntityType.Builder.<HydroturbineEntity>create(HydroturbineEntity::new, EntityClassification.MISC)
                .size(1, 1).build("electricadvantage:test_anchored_turbine");
    }

    private static EntityType<ControlEntity> controlType() {
        return EntityType.Builder.<ControlEntity>create(ControlEntity::new, EntityClassification.MISC)
                .size(1, 1).build("electricadvantage:test_native_motion_control");
    }

    private static World world() throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        World world = (World)allocator.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), NoTickWorld.class);
        Field random = World.class.getField("rand");
        random.setAccessible(true);
        random.set(world, new Random(0));
        Field dimension = World.class.getField("dimension");
        dimension.setAccessible(true);
        dimension.set(world, new OverworldDimension(world, DimensionType.OVERWORLD));
        return world;
    }

    private static final class NoTickWorld extends ServerWorld {
        private NoTickWorld() {
            super(null, null, null, null, null, null, null);
        }
    }

    private static final class ControlEntity extends Entity {
        ControlEntity(EntityType<?> type, World world) { super(type, world); }
        @Override protected void registerData() {}
        @Override protected void readAdditional(CompoundNBT tag) {}
        @Override protected void writeAdditional(CompoundNBT tag) {}
        @Override public IPacket<?> createSpawnPacket() { return null; }
    }
}
