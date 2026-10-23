package zone.moddev.mc.electricadvantage.entities;

import java.lang.reflect.Field;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;
import sun.misc.Unsafe;

import static org.junit.Assert.*;

public class HydroturbinePersistenceTest {
    @Test public void preservesPendingGeneratorAcrossRepeatedReadSaveWithoutTick() throws Exception {
        NBTTagCompound source = new NBTTagCompound();
        source.setIntArray("parentPos", new int[] {164, 81, 8});
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity turbine = withoutWorld();
            turbine.readAdditional(source);
            NBTTagCompound saved = new NBTTagCompound();
            turbine.writeAdditional(saved);
            assertArrayEquals(new int[] {164, 81, 8}, saved.getIntArray("parentPos"));
            source = saved;
        }
    }

    @Test public void readingUnlinkedEntityClearsPendingLink() throws Exception {
        HydroturbineEntity turbine = withoutWorld();
        NBTTagCompound source = new NBTTagCompound();
        source.setIntArray("parentPos", new int[] {164, 81, 8});
        turbine.readAdditional(source);
        turbine.readAdditional(new NBTTagCompound());
        NBTTagCompound saved = new NBTTagCompound();
        turbine.writeAdditional(saved);
        assertFalse(saved.contains("parentPos", 11));
    }

    // Only exercise the custom NBT methods; no world, registry or entity tick is needed.
    private static HydroturbineEntity withoutWorld() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (HydroturbineEntity) ((Unsafe) field.get(null)).allocateInstance(HydroturbineEntity.class);
    }
}
