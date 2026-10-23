package com.mcmoddev.electricadvantage.entities;

import java.lang.reflect.Field;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import sun.misc.Unsafe;

import static org.junit.Assert.*;

public class HydroturbinePersistenceTest {
    @Test public void preservesPendingGeneratorAcrossRepeatedReadSaveWithoutTick() throws Exception {
        NBTTagCompound source = new NBTTagCompound();
        source.setIntArray("parentPos", new int[] {164, 81, 8});
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity turbine = withoutWorld();
            turbine.readEntityFromNBT(source);
            NBTTagCompound saved = new NBTTagCompound();
            turbine.writeEntityToNBT(saved);
            assertArrayEquals(new int[] {164, 81, 8}, saved.getIntArray("parentPos"));
            source = saved;
        }
    }

    @Test public void readingUnlinkedEntityClearsPendingAndLiveLinks() throws Exception {
        HydroturbineEntity turbine = withoutWorld();
        NBTTagCompound source = new NBTTagCompound();
        source.setIntArray("parentPos", new int[] {164, 81, 8});
        turbine.readEntityFromNBT(source);
        turbine.parent = new TileEntity() {};
        turbine.readEntityFromNBT(new NBTTagCompound());
        NBTTagCompound saved = new NBTTagCompound();
        turbine.writeEntityToNBT(saved);
        assertNull(turbine.parent);
        assertFalse(saved.hasKey("parentPos", 11));
    }

    @Test public void savesLiveGeneratorPositionRatherThanAnOlderPendingLink() throws Exception {
        HydroturbineEntity turbine = withoutWorld();
        NBTTagCompound source = new NBTTagCompound();
        source.setIntArray("parentPos", new int[] {164, 81, 8});
        turbine.readEntityFromNBT(source);
        TileEntity parent = new TileEntity() {};
        parent.setPos(new BlockPos(32, 70, -16));
        turbine.parent = parent;
        NBTTagCompound saved = new NBTTagCompound();
        turbine.writeEntityToNBT(saved);
        assertArrayEquals(new int[] {32, 70, -16}, saved.getIntArray("parentPos"));
    }

    // Exercise the custom NBT methods without constructing or ticking a world.
    private static HydroturbineEntity withoutWorld() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (HydroturbineEntity) ((Unsafe) field.get(null)).allocateInstance(HydroturbineEntity.class);
    }
}
