package zone.moddev.mc.electricadvantage.migration;

import static org.junit.Assert.assertEquals;

import cyano.poweradvantage.api.migration.LegacyMigrationApi;
import net.minecraft.util.Direction;
import org.junit.Test;

public class ElectricLegacyMigrationsTest {
    @Test public void preservesAllSixDrillFacingsAndActiveFlag() {
        for (Direction facing : Direction.values()) {
            int metadata = facing.get3DDataValue() | 8;
            assertEquals(facing, ElectricLegacyMigrations.legacyDrillFacing(metadata));
            assertEquals(true, ElectricLegacyMigrations.legacyDrillActive(metadata));
        }
    }

    @Test public void aliasesLegacyTileAndHydroturbineIds() {
        ElectricLegacyMigrations.register();
        assertEquals("electricadvantage:led", LegacyMigrationApi.tileTarget("electricadvantage:l_e_d"));
        assertEquals("electricadvantage:led", LegacyMigrationApi.tileTarget("electricadvantage.l_e_d"));
        assertEquals("electricadvantage:electric_furnace",
                LegacyMigrationApi.tileTarget("minecraft:electricadvantage.electric_furnace"));
        assertEquals("electricadvantage:hydroturbine",
                LegacyMigrationApi.entityTarget("minecraft:electricadvantage.Hydroturbine"));
        assertEquals("electricadvantage:hydroturbine",
                LegacyMigrationApi.entityTarget("electricadvantage.electricadvantage.Hydroturbine"));
    }
}
