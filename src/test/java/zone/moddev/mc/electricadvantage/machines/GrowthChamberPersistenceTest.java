package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.nbt.CompoundNBT;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrowthChamberPersistenceTest {
    @Test public void legacyGrowthAndProgressSurviveReadSaveBeforeWorldAttachment() {
        CompoundNBT source = new CompoundNBT();
        source.putByteArray("growth", new byte[] {2, 4, 6});
        source.putIntArray("prog", new int[] {245, 468, 627});
        for (int pass = 0; pass < 3; pass++) {
            GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
            chamber.loadFrom(source);
            CompoundNBT saved = new CompoundNBT();
            chamber.saveTo(saved);
            assertArrayEquals(new byte[] {2, 4, 6}, saved.getByteArray("growth"));
            assertArrayEquals(new int[] {245, 468, 627}, saved.getIntArray("prog"));
            source = saved;
        }
    }

    @Test public void shortArraysArePaddedAndNewNbtClearsOldPendingWork() {
        GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
        CompoundNBT source = new CompoundNBT();
        source.putByteArray("growth", new byte[] {2});
        source.putIntArray("prog", new int[] {245});
        chamber.loadFrom(source);
        CompoundNBT saved = new CompoundNBT();
        chamber.saveTo(saved);
        assertArrayEquals(new byte[] {2, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {245, 0, 0}, saved.getIntArray("prog"));
        chamber.loadFrom(new CompoundNBT());
        chamber.saveTo(saved);
        assertArrayEquals(new byte[] {0, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {0, 0, 0}, saved.getIntArray("prog"));
    }
}
