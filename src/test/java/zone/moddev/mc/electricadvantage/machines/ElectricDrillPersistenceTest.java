package zone.moddev.mc.electricadvantage.machines;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.BeforeClass;

import static org.junit.Assert.assertEquals;

public class ElectricDrillPersistenceTest {
    @BeforeClass public static void bootstrapNativeRegistries() { Bootstrap.bootStrap(); }

    @Test public void retainsLegacyProgressUntilWorldIsAttached() {
        ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
        drill.load(new ElectricDrillBlock().defaultBlockState(), target(94));
        drill.prepareDataFieldsForSync();
        assertEquals(94, drill.getDataFieldArray()[1]);
    }

    @Test public void clearsPreviousProgressOnReadingAnUntargetedTile() {
        ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
        drill.load(new ElectricDrillBlock().defaultBlockState(), target(94));
        drill.load(new ElectricDrillBlock().defaultBlockState(), new CompoundNBT());
        drill.prepareDataFieldsForSync();
        assertEquals(0, drill.getDataFieldArray()[1]);
    }

    @Test public void preservesPendingTargetSnapshotAcrossRepeatedReadSave() {
        CompoundNBT source = target(94);
        CompoundNBT state = new CompoundNBT();
        state.putString("Name", "minecraft:stone");
        ListNBT states = new ListNBT();
        for (int i = 0; i < 9; i++) states.add(state.copy());
        source.put("TargetStates", states);
        for (int pass = 0; pass < 3; pass++) {
            ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
            drill.loadFrom(source);
            CompoundNBT saved = new CompoundNBT();
            drill.saveTo(saved);
            assertEquals(source, saved);
            source = saved;
        }
    }

    @Test public void targetPlaneRemainsNineUniqueBlocksForEveryDrillDirection() throws Exception {
        ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
        Field facing = ElectricDrillTileEntity.class.getDeclaredField("facingCache");
        facing.setAccessible(true);
        Method area = ElectricDrillTileEntity.class.getDeclaredMethod("getArea", BlockPos.class);
        area.setAccessible(true);
        BlockPos center = new BlockPos(17, 80, 21);
        for (Direction direction : Direction.values()) {
            facing.set(drill, direction);
            BlockPos[] targets = (BlockPos[]) area.invoke(drill, center);
            Set<BlockPos> unique = new HashSet<>();
            assertEquals(center, targets[0]);
            for (BlockPos target : targets) {
                unique.add(target);
                assertEquals(0, (target.getX() - center.getX()) * direction.getStepX()
                        + (target.getY() - center.getY()) * direction.getStepY()
                        + (target.getZ() - center.getZ()) * direction.getStepZ());
            }
            assertEquals(direction.toString(), 9, unique.size());
        }
    }

    @Test public void nativeDataFieldsRetainEnergyAndMiningProgress() {
        ElectricDrillTileEntity drill = new ElectricDrillTileEntity();
        drill.load(new ElectricDrillBlock().defaultBlockState(), target(94));
        drill.setEnergy(416.0F, drill.getEnergyType());
        drill.prepareDataFieldsForSync();
        assertEquals(416.0F, Float.intBitsToFloat(drill.getDataFieldArray()[0]), 0.0F);
        assertEquals(94, drill.getDataFieldArray()[1]);
        assertEquals(250.0F, ElectricDrillTileEntity.ENERGY_COST_MOVE, 0.0F);
        assertEquals(24.0F, ElectricDrillTileEntity.ENERGY_COST_PROGRESS_TICK, 0.0F);
        assertEquals(6, drill.numberOfOutputSlots());
    }

    private static CompoundNBT target(int progress) {
        CompoundNBT tag = new CompoundNBT();
        tag.putShort("progress", (short) progress);
        tag.putInt("targetX", 2048);
        tag.putInt("targetY", 80);
        tag.putInt("targetZ", 2049);
        return tag;
    }
}
