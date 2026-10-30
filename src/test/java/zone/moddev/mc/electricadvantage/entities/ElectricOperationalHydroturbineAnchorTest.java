package zone.moddev.mc.electricadvantage.entities;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectricOperationalHydroturbineAnchorTest {
    @BeforeClass public static void bootstrap() { Bootstrap.bootStrap(); }

    @Test public void nativeEntityLoadStillClampsOverspeedMotionAndPreservesItsBoundary() throws Exception {
        ControlEntity control = new ControlEntity(controlType(), world());
        CompoundNBT tag = control.saveWithoutId(new CompoundNBT());
        tag.put("Motion", motion(16.226, -10.001, 10));
        control.load(tag);
        assertEquals(new Vector3d(0, 0, 10), control.getDeltaMovement());
        assertTrue(control.isPushedByFluid());
    }

    @Test public void turbineRejectsLinearWaterPushingWithoutChangingRotationRate() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(turbineType(), world());
        assertFalse(turbine.isPushedByFluid());
        assertEquals(Vector3d.ZERO, turbine.getDeltaMovement());
        assertEquals(30, HydroturbineEntity.DEGREES_PER_TICK, 0);
    }

    @Test public void legacyMotionInsideTheNativeLimitIsNotArbitrarilyCleared() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(turbineType(), world());
        CompoundNBT saved = turbine.saveWithoutId(new CompoundNBT());
        saved.put("Motion", motion(5.5, -2, 0));
        turbine.load(saved);
        assertEquals(new Vector3d(5.5, -2, 0), turbine.getDeltaMovement());
        assertFalse(turbine.isPushedByFluid());
    }

    @Test public void anchoredTurbineSurvivesNativeSaveLoadWithItsParentAndIdentity() throws Exception {
        EntityType<HydroturbineEntity> type = turbineType();
        World world = world();
        HydroturbineEntity original = new HydroturbineEntity(type, world);
        original.setPos(56856.5, 81, 50008.5);
        CompoundNBT saved = original.saveWithoutId(new CompoundNBT());
        int[] parent = {56856, 82, 50008};
        saved.putIntArray("parentPos", parent);
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity loaded = new HydroturbineEntity(type, world);
            loaded.load(saved);
            CompoundNBT next = loaded.saveWithoutId(new CompoundNBT());
            assertEquals(saved.getUUID("UUID"), next.getUUID("UUID"));
            assertEquals(saved.getList("Pos", 6), next.getList("Pos", 6));
            assertEquals(motion(0, 0, 0), next.getList("Motion", 6));
            assertArrayEquals(parent, next.getIntArray("parentPos"));
            assertFalse(loaded.isPushedByFluid());
            saved = next;
        }
    }

    private static ListNBT motion(double x, double y, double z) {
        ListNBT list = new ListNBT();
        list.add(DoubleNBT.valueOf(x)); list.add(DoubleNBT.valueOf(y)); list.add(DoubleNBT.valueOf(z));
        return list;
    }

    private static EntityType<HydroturbineEntity> turbineType() {
        return EntityType.Builder.<HydroturbineEntity>of(HydroturbineEntity::new, EntityClassification.MISC)
                .sized(1, 1).build("electricadvantage:test_anchored_turbine");
    }

    private static EntityType<ControlEntity> controlType() {
        return EntityType.Builder.<ControlEntity>of(ControlEntity::new, EntityClassification.MISC)
                .sized(1, 1).build("electricadvantage:test_native_motion_control");
    }

    private static World world() throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        World world = (World)allocator.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), NoTickWorld.class);
        Field random = World.class.getField("random");
        random.setAccessible(true);
        random.set(world, new Random(0));
        return world;
    }

    private static final class NoTickWorld extends ServerWorld {
        private NoTickWorld() {
            super(null, null, null, null, null, null, null, null, false, 0L, Collections.emptyList(), false);
        }
    }

    private static final class ControlEntity extends Entity {
        ControlEntity(EntityType<?> type, World world) { super(type, world); }
        @Override protected void defineSynchedData() {}
        @Override protected void readAdditionalSaveData(CompoundNBT tag) {}
        @Override protected void addAdditionalSaveData(CompoundNBT tag) {}
        @Override public IPacket<?> getAddEntityPacket() { return null; }
    }
}
