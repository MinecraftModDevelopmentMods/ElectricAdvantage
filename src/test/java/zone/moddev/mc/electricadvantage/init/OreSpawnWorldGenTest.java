package zone.moddev.mc.electricadvantage.init;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonObject;
import org.junit.Test;

public class OreSpawnWorldGenTest {
    @Test public void electricOwnsBothMaterialsWhenNoPreferredProviderIsLoaded() {
        assertDefaults(false, false, true, true);
    }

    @Test public void mineralogyOwnsSulfurButElectricStillOwnsLithium() {
        assertDefaults(true, false, false, true);
    }

    @Test public void baseMineralsOwnsBothFallbackMaterials() {
        assertDefaults(false, true, false, false);
    }

    @Test public void preferredProvidersDisableBothElectricAlternatives() {
        assertDefaults(true, true, false, false);
    }

    private static void assertDefaults(boolean mineralogy, boolean baseMinerals,
            boolean sulfurEnabled, boolean lithiumEnabled) {
        JsonObject root = OreSpawnWorldGen.createProvider(mineralogy, baseMinerals).toJson();
        JsonObject ores = root.getAsJsonObject("ores");
        assertEquals(sulfurEnabled,
                ores.getAsJsonObject("electricadvantage:ore/sulfur").get("enabled").getAsBoolean());
        assertEquals(lithiumEnabled,
                ores.getAsJsonObject("electricadvantage:ore/lithium").get("enabled").getAsBoolean());
        assertEquals(2, ores.size());
    }
}
