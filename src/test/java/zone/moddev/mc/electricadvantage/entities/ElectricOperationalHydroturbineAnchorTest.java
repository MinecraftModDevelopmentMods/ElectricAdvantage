package zone.moddev.mc.electricadvantage.entities;

import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;
import zone.moddev.mc.electricadvantage.testsupport.Forge113TestBootstrap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.WorldServer;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectricOperationalHydroturbineAnchorTest {
    @BeforeClass public static void bootstrap() { Forge113TestBootstrap.register(); }

    @Test public void nativeEntityLoadStillClampsOverspeedMotionAndPreservesItsBoundary() throws Exception {
        ControlEntity control = new ControlEntity(controlType(), world());
        NBTTagCompound tag = control.writeWithoutTypeId(new NBTTagCompound());
        tag.setTag("Motion", motion(16.226, -10.001, 10));
        control.read(tag);
        assertEquals(new Vec3d(0, 0, 10), motion(control));
        assertTrue(control.isPushedByWater());
    }

    @Test public void turbineRejectsLinearWaterPushingWithoutChangingRotationRate() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(turbineType(), world());
        assertFalse(turbine.isPushedByWater());
        assertEquals(Vec3d.ZERO, motion(turbine));
        assertEquals(30, HydroturbineEntity.DEGREES_PER_TICK, 0);
    }

    @Test public void legacyMotionInsideTheNativeLimitIsNotArbitrarilyCleared() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(turbineType(), world());
        NBTTagCompound saved = turbine.writeWithoutTypeId(new NBTTagCompound());
        saved.setTag("Motion", motion(5.5, -2, 0));
        turbine.read(saved);
        assertEquals(new Vec3d(5.5, -2, 0), motion(turbine));
        assertFalse(turbine.isPushedByWater());
    }

    @Test public void anchoredTurbineSurvivesNativeSaveLoadWithItsParentAndIdentity() throws Exception {
        EntityType<HydroturbineEntity> type = turbineType();
        World world = world();
        HydroturbineEntity original = new HydroturbineEntity(type, world);
        original.setPosition(56856.5, 81, 50008.5);
        NBTTagCompound saved = original.writeWithoutTypeId(new NBTTagCompound());
        int[] parent = {56856, 82, 50008};
        saved.setIntArray("parentPos", parent);
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity loaded = new HydroturbineEntity(type, world);
            loaded.read(saved);
            NBTTagCompound next = loaded.writeWithoutTypeId(new NBTTagCompound());
            assertEquals(saved.getUniqueId("UUID"), next.getUniqueId("UUID"));
            assertEquals(saved.getList("Pos", 6), next.getList("Pos", 6));
            assertEquals(motion(0, 0, 0), next.getList("Motion", 6));
            assertArrayEquals(parent, next.getIntArray("parentPos"));
            assertFalse(loaded.isPushedByWater());
            saved = next;
        }
    }

    private static Vec3d motion(Entity entity) {
        return new Vec3d(entity.motionX, entity.motionY, entity.motionZ);
    }

    private static NBTTagList motion(double x, double y, double z) {
        NBTTagList list = new NBTTagList();
        list.add(new NBTTagDouble(x)); list.add(new NBTTagDouble(y)); list.add(new NBTTagDouble(z));
        return list;
    }

    private static EntityType<HydroturbineEntity> turbineType() {
        return EntityType.Builder.create(HydroturbineEntity.class, world -> new HydroturbineEntity(null, world))
                .build("electricadvantage:test_anchored_turbine");
    }

    private static EntityType<ControlEntity> controlType() {
        return EntityType.Builder.create(ControlEntity.class, world -> new ControlEntity(null, world))
                .build("electricadvantage:test_native_motion_control");
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
        dimension.set(world, new OverworldDimension(DimensionType.OVERWORLD));
        return world;
    }

    private static final class NoTickWorld extends WorldServer {
        private NoTickWorld() {
            super(null, null, null, null, null, null);
        }
    }

    private static final class ControlEntity extends Entity {
        ControlEntity(EntityType<?> type, World world) { super(type, world); }
        @Override protected void registerData() {}
        @Override protected void readAdditional(NBTTagCompound tag) {}
        @Override protected void writeAdditional(NBTTagCompound tag) {}
    }
}
