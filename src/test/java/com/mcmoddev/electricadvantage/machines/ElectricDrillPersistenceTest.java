package com.mcmoddev.electricadvantage.machines;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectricDrillPersistenceTest {
    @Test public void retainsLegacyProgressBeforeWorldAttachment() {
        ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
        drill.loadFrom(target(512));
        drill.prepareDataFieldsForSync();
        assertEquals(512, drill.getDataFieldArray()[1]);
    }

    @Test public void preservesPendingTargetThroughRepeatedSerialization() {
        NBTTagCompound source = target(512);
        for (int i = 0; i < 3; i++) {
            ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
            drill.loadFrom(source);
            source = new NBTTagCompound();
            drill.saveTo(source);
            assertEquals(512, source.getShort("progress"));
            assertEquals(2049, source.getInteger("targetZ"));
        }
    }

    @Test public void untargetedDataClearsPendingWork() {
        ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
        drill.loadFrom(target(512));
        drill.loadFrom(new NBTTagCompound());
        NBTTagCompound saved = new NBTTagCompound();
        drill.saveTo(saved);
        assertEquals(0, saved.getShort("progress"));
        assertFalse(saved.hasKey("targetX"));
    }

    private static NBTTagCompound target(int progress) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort("progress", (short) progress);
        tag.setInteger("targetX", 2048);
        tag.setInteger("targetY", 80);
        tag.setInteger("targetZ", 2049);
        return tag;
    }
}
