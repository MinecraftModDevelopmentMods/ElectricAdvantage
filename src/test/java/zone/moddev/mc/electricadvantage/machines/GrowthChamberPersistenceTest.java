package zone.moddev.mc.electricadvantage.machines;

import java.lang.reflect.Field;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.electricadvantage.testsupport.Forge113TestBootstrap;

import static org.junit.Assert.*;

public class GrowthChamberPersistenceTest {
    @BeforeClass public static void bootstrap() { Forge113TestBootstrap.register(); }

    @Test public void legacyGrowthAndProgressSurviveReadSaveBeforeWorldAttachment() {
        NBTTagCompound source = work(new byte[] {7, 9, 6}, new int[] {797, 945, 627});
        for (int pass = 0; pass < 3; pass++) {
            ProbeChamber chamber = new ProbeChamber();
            chamber.loadFrom(source);
            assertEquals("NBT loading must not need a world or construct crops yet", 0, chamber.worldReads);
            NBTTagCompound saved = saved(chamber);
            assertArrayEquals(new byte[] {7, 9, 6}, saved.getByteArray("growth"));
            assertArrayEquals(new int[] {797, 945, 627}, saved.getIntArray("prog"));
            source = saved;
        }
    }

    @Test public void shortArraysArePaddedAndNewNbtClearsOldPendingWork() {
        GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
        chamber.loadFrom(work(new byte[] {2}, new int[] {245}));
        NBTTagCompound saved = saved(chamber);
        assertArrayEquals(new byte[] {2, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {245, 0, 0}, saved.getIntArray("prog"));
        chamber.loadFrom(new NBTTagCompound());
        saved = saved(chamber);
        assertArrayEquals(new byte[] {0, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {0, 0, 0}, saved.getIntArray("prog"));
    }

    @Test public void oversizedArraysAreLimitedToTheThreeCropSlots() {
        GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
        chamber.loadFrom(work(new byte[] {1, 3, 7, 9}, new int[] {135, 357, 799, 945}));
        NBTTagCompound saved = saved(chamber);
        assertArrayEquals(new byte[] {1, 3, 7}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {135, 357, 799}, saved.getIntArray("prog"));
    }

    @Test public void numericTagsDoNotStandInForTheLegacyArrays() {
        GrowthChamberTileEntity chamber = new GrowthChamberTileEntity();
        chamber.loadFrom(work(new byte[] {7}, new int[] {797}));
        NBTTagCompound invalid = new NBTTagCompound();
        invalid.setByte("growth", (byte) 7);
        invalid.setInt("prog", 797);
        chamber.loadFrom(invalid);
        NBTTagCompound saved = saved(chamber);
        assertArrayEquals(new byte[] {0, 0, 0}, saved.getByteArray("growth"));
        assertArrayEquals(new int[] {0, 0, 0}, saved.getIntArray("prog"));
    }

    @Test public void attachedFirstTickResumesNearlyGrownCropWithoutResettingItsCache() throws Exception {
        ProbeChamber chamber = new ProbeChamber();
        chamber.setPos(new BlockPos(8, 82, 8));
        chamber.setInputSlot(0, new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.SUGAR_CANE)));
        chamber.loadFrom(work(new byte[] {7}, new int[] {797}));
        assertEquals(0, chamber.worldReads);
        chamber.attachedWorld = emptyWorld();
        chamber.setWorld(chamber.attachedWorld);
        chamber.setEnergy(3, Power.GROWTHCHAMBER_POWER);

        chamber.tickUpdate(true);
        assertTrue("Virtual crops are reconstructed with the attached native world", chamber.worldReads > 0);
        NBTTagCompound first = saved(chamber);
        assertArrayEquals(new byte[] {7, 0, 0}, first.getByteArray("growth"));
        assertArrayEquals(new int[] {798, 0, 0}, first.getIntArray("prog"));
        assertEquals(2, chamber.getEnergy(Power.GROWTHCHAMBER_POWER), 0);
        assertEquals(1, chamber.getInputSlot(0).getCount());
        assertEquals(0, productCount(chamber));

        chamber.tickUpdate(true);
        assertEquals(799, saved(chamber).getIntArray("prog")[0]);
        assertEquals(0, productCount(chamber));
        chamber.tickUpdate(true);
        assertEquals("Only the three remaining work ticks are charged", 0,
                chamber.getEnergy(Power.GROWTHCHAMBER_POWER), 0);
        assertTrue(chamber.getInputSlot(0) == null || chamber.getInputSlot(0).isEmpty());
        assertEquals("The actual crop harvest is produced, not only its NBT", 2, productCount(chamber));
        assertEquals(1, chamber.harvestSounds);
        assertArrayEquals(new byte[] {0, 0, 0}, saved(chamber).getByteArray("growth"));
        assertArrayEquals(new int[] {0, 0, 0}, saved(chamber).getIntArray("prog"));
    }

    @Test public void unpoweredFirstTickRetainsCompletedCropStages() throws Exception {
        ProbeChamber chamber = new ProbeChamber();
        chamber.setInputSlot(0, new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.SUGAR_CANE)));
        chamber.loadFrom(work(new byte[] {7}, new int[] {0}));
        chamber.attachedWorld = emptyWorld();
        chamber.setWorld(chamber.attachedWorld);
        chamber.tickUpdate(true);
        assertArrayEquals(new byte[] {7, 0, 0}, saved(chamber).getByteArray("growth"));
        assertArrayEquals(new int[] {0, 0, 0}, saved(chamber).getIntArray("prog"));
        assertEquals(1, chamber.getInputSlot(0).getCount());
        assertEquals(0, productCount(chamber));
        assertEquals(0, chamber.getEnergy(Power.GROWTHCHAMBER_POWER), 0);
    }

    private static NBTTagCompound work(byte[] stages, int[] progress) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByteArray("growth", stages);
        tag.setIntArray("prog", progress);
        return tag;
    }

    private static NBTTagCompound saved(GrowthChamberTileEntity chamber) {
        NBTTagCompound tag = new NBTTagCompound();
        chamber.saveTo(tag);
        return tag;
    }

    private static int productCount(GrowthChamberTileEntity chamber) {
        int count = 0;
        for (int slot = 0; slot < chamber.numberOfOutputSlots(); slot++) {
            ItemStack stack = chamber.getOutputSlot(slot);
            if (stack == null || stack.isEmpty()) continue;
            assertSame(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.SUGAR_CANE), stack.getItem());
            count += stack.getCount();
        }
        return count;
    }

    private static WorldServer emptyWorld() throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (WorldServer)allocator.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), WorldServer.class);
    }

    private static final class ProbeChamber extends GrowthChamberTileEntity {
        World attachedWorld;
        int worldReads;
        int harvestSounds;
        @Override public World getWorld() {
            worldReads++;
            assertNotNull("Crop construction must wait until the world is attached", attachedWorld);
            return attachedWorld;
        }
        @Override protected boolean hasRedstoneSignal() { return false; }
        @Override protected void setActiveState(boolean active) { }
        @Override protected void playSoundEffect(double x, double y, double z, SoundEvent sound, float volume, float pitch) {
            harvestSounds++;
        }
        @Override public void markDirty() { }
        @Override public SPacketUpdateTileEntity getUpdatePacket() { return null; }
    }
}
