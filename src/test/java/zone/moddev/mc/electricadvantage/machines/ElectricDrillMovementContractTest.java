package zone.moddev.mc.electricadvantage.machines;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

import static org.junit.Assert.*;

/** Source guard only; Advantage Works supplies the native six-facing movement proof. */
public class ElectricDrillMovementContractTest {
    @Test public void replacementPaysExactlyOnceBeforeBeingMarkedDirty() throws Exception {
        String source = new String(Files.readAllBytes(Paths.get("src/main/java",
                ElectricDrillTileEntity.class.getName().replace('.', '/') + ".java")), StandardCharsets.UTF_8);
        int start = source.indexOf("private boolean followTrack()");
        assertTrue("Locate the actual track-movement method", start >= 0);
        int end = source.indexOf("@Override", start);
        assertTrue("Bound the guard to the track-movement method", end > start);
        String movement = source.substring(start, end).replaceAll("\\s+", "");
        String debit = "te.subtractEnergy(ENERGY_COST_MOVE,te.getType());";
        int debitIndex = movement.indexOf(debit);
        int transferIndex = movement.indexOf("te.readFromNBT(dataTransfer);");
        assertTrue("The movement must copy the saved state into the replacement", transferIndex >= 0);
        assertTrue("The replacement must pay after receiving the saved state",
                debitIndex > transferIndex);
        assertTrue("Persist the replacement only after charging its move",
                debitIndex < movement.indexOf("te.markDirty();"));
        assertTrue("Charge before clearing or replacing the source",
                debitIndex < movement.indexOf("Arrays.fill(this.getInventory(),null);"));
        assertEquals("There must be only one movement debit", debitIndex, movement.lastIndexOf(debit));
        assertEquals("Do not debit the old drill or charge twice", 1,
                movement.split("subtractEnergy\\(", -1).length - 1);
    }

    @Test public void trackMovementStillCostsTwoHundredAndFiftyUnits() {
        assertEquals(250.0f, ElectricDrillTileEntity.ENERGY_COST_MOVE, 0.0f);
    }
}
