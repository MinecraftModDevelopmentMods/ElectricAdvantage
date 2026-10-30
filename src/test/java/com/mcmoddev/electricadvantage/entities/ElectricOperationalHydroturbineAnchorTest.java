package com.mcmoddev.electricadvantage.entities;

import java.lang.reflect.Field;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectricOperationalHydroturbineAnchorTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void nativeEntityLoadStillClampsOverspeedMotionAndPreservesItsBoundary() throws Exception {
        ControlEntity control = new ControlEntity(world());
        NBTTagCompound tag = control.writeToNBT(new NBTTagCompound());
        tag.setTag("Motion", motion(16.226, -10.001, 10));
        control.readFromNBT(tag);
        assertEquals(new Vec3d(0, 0, 10), motion(control));
        assertTrue(control.isPushedByWater());
    }

    @Test public void turbineRejectsLinearWaterPushingWithoutChangingRotationRate() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(world());
        assertFalse(turbine.isPushedByWater());
        assertEquals(Vec3d.ZERO, motion(turbine));
        assertEquals(30, HydroturbineEntity.DEGREES_PER_TICK, 0);
    }

    @Test public void legacyMotionInsideTheNativeLimitIsNotArbitrarilyCleared() throws Exception {
        HydroturbineEntity turbine = new HydroturbineEntity(world());
        NBTTagCompound saved = turbine.writeToNBT(new NBTTagCompound());
        saved.setTag("Motion", motion(5.5, -2, 0));
        turbine.readFromNBT(saved);
        assertEquals(new Vec3d(5.5, -2, 0), motion(turbine));
        assertFalse(turbine.isPushedByWater());
    }

    @Test public void anchoredTurbineSurvivesNativeSaveLoadWithItsParentAndIdentity() throws Exception {
        World world = world();
        HydroturbineEntity original = new HydroturbineEntity(world);
        original.setPosition(56856.5, 81, 50008.5);
        NBTTagCompound saved = original.writeToNBT(new NBTTagCompound());
        int[] parent = {56856, 82, 50008};
        saved.setIntArray("parentPos", parent);
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity loaded = new HydroturbineEntity(world);
            loaded.readFromNBT(saved);
            NBTTagCompound next = loaded.writeToNBT(new NBTTagCompound());
            assertEquals(saved.getUniqueId("UUID"), next.getUniqueId("UUID"));
            assertEquals(saved.getTagList("Pos", 6), next.getTagList("Pos", 6));
            assertEquals(motion(0, 0, 0), next.getTagList("Motion", 6));
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
        list.appendTag(new NBTTagDouble(x)); list.appendTag(new NBTTagDouble(y)); list.appendTag(new NBTTagDouble(z));
        return list;
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
        Field dimension = World.class.getField("provider");
        dimension.setAccessible(true);
        dimension.set(world, new WorldProviderSurface());
        return world;
    }

    private static final class NoTickWorld extends WorldServer {
        private NoTickWorld() {
            super(null, null, null, 0, null);
        }
    }

    private static final class ControlEntity extends Entity {
        ControlEntity(World world) { super(world); }
        @Override protected void entityInit() {}
        @Override protected void readEntityFromNBT(NBTTagCompound tag) {}
        @Override protected void writeEntityToNBT(NBTTagCompound tag) {}
    }
}
