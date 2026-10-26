package zone.moddev.mc.electricadvantage.entities;

import java.lang.reflect.Field;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import zone.moddev.mc.electricadvantage.machines.HydroelectricGeneratorTileEntity;
import org.junit.Test;
import sun.misc.Unsafe;

import static org.junit.Assert.*;

public class HydroturbinePersistenceTest {
    @Test public void preservesPendingGeneratorAcrossRepeatedReadSaveWithoutTick() throws Exception {
        CompoundNBT source = new CompoundNBT();
        source.putIntArray("parentPos", new int[] {164, 81, 8});
        for (int pass = 0; pass < 3; pass++) {
            HydroturbineEntity turbine = withoutWorld();
            turbine.readAdditional(source);
            CompoundNBT saved = new CompoundNBT();
            turbine.writeAdditional(saved);
            assertArrayEquals(new int[] {164, 81, 8}, saved.getIntArray("parentPos"));
            source = saved;
        }
    }

    @Test public void readingUnlinkedEntityClearsPendingLink() throws Exception {
        HydroturbineEntity turbine = withoutWorld();
        CompoundNBT source = new CompoundNBT();
        source.putIntArray("parentPos", new int[] {164, 81, 8});
        turbine.readAdditional(source);
        turbine.readAdditional(new CompoundNBT());
        CompoundNBT saved = new CompoundNBT();
        turbine.writeAdditional(saved);
        assertFalse(saved.contains("parentPos", 11));
    }

    @Test public void savesAttachedGeneratorUsingTheSameLegacyCoordinateKey() throws Exception {
        HydroturbineEntity turbine = withoutWorld();
        HydroelectricGeneratorTileEntity generator = new HydroelectricGeneratorTileEntity();
        generator.setPos(new BlockPos(31, 72, -18));
        turbine.parent = generator;
        CompoundNBT saved = new CompoundNBT();
        turbine.writeAdditional(saved);
        assertArrayEquals(new int[] {31, 72, -18}, saved.getIntArray("parentPos"));
    }

    @Test public void malformedLegacyCoordinatesDoNotCreateAPartialParentLink() throws Exception {
        HydroturbineEntity turbine = withoutWorld();
        CompoundNBT source = new CompoundNBT();
        source.putIntArray("parentPos", new int[] {31, 72});
        turbine.readAdditional(source);
        CompoundNBT saved = new CompoundNBT();
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
