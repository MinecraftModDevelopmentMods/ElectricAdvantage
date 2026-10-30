package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class GrowthChamberPersistenceTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void shortArraysArePaddedAndNewNbtClearsPendingWork() {
        GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
        NBTTagCompound source = new NBTTagCompound();
        source.setByteArray("growth", new byte[] {7});
        source.setIntArray("prog", new int[] {797});
        chamber.loadFrom(source);
        NBTTagCompound saved = new NBTTagCompound();
        chamber.saveTo(saved);
        assertArrayEquals(new byte[] {7, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {797, 0, 0}, saved.getIntArray("prog"));
        chamber.loadFrom(new NBTTagCompound());
        chamber.saveTo(saved);
        assertArrayEquals(new byte[] {0, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {0, 0, 0}, saved.getIntArray("prog"));
    }

    @Test public void fullOutputStageNineSurvivesUntickedReadSave() {
        NBTTagCompound source = new NBTTagCompound();
        source.setByteArray("growth", new byte[] {9, 0, 0});
        source.setIntArray("prog", new int[] {0, 0, 0});
        for (int pass = 0; pass < 3; pass++) {
            GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
            chamber.setInputSlot(0, new ItemStack(Items.REEDS));
            for (int slot = 0; slot < chamber.numberOfOutputSlots(); slot++) {
                chamber.setOutputSlot(slot, new ItemStack(Items.REEDS, 64));
            }
            chamber.loadFrom(source);
            NBTTagCompound saved = new NBTTagCompound();
            chamber.saveTo(saved);
            assertArrayEquals("Cold read/save must retain the exact mature crop stage",
                    new byte[] {9, 0, 0}, saved.getByteArray("growth"));
            assertArrayEquals(new int[] {0, 0, 0}, saved.getIntArray("prog"));
            assertSame(Items.REEDS, chamber.getInputSlot(0).getItem());
            assertEquals(1, chamber.getInputSlot(0).stackSize);
            for (int slot = 0; slot < chamber.numberOfOutputSlots(); slot++) {
                assertSame(Items.REEDS, chamber.getOutputSlot(slot).getItem());
                assertEquals(64, chamber.getOutputSlot(slot).stackSize);
            }
            source = saved;
        }
    }
}
