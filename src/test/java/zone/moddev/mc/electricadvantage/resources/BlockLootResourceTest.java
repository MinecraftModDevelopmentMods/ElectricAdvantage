package zone.moddev.mc.electricadvantage.resources;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlockLootResourceTest {
    @Test public void everyOrdinaryBlockKeepsItsOwnNativeItemDrop() throws Exception {
        Path root = Paths.get("src/main/resources/data/electricadvantage/loot_tables/blocks");
        try (Stream<Path> states = Files.list(Paths.get("src/main/resources/assets/electricadvantage/blockstates"))) {
            for (Path state : (Iterable<Path>) states.filter(path -> path.toString().endsWith(".json"))::iterator) {
                String id = state.getFileName().toString().replace(".json", "");
                // Two retained asset-only models are not registered blocks.
                if ("electric_pcb_printer".equals(id) || "geothermal_generator".equals(id)) continue;
                if ("li_ore".equals(id) || "sulfur_ore".equals(id) || "pump_pipe_electric".equals(id)) continue;
                Path table = root.resolve(id + ".json");
                assertTrue("Missing native block drop for " + id, Files.isRegularFile(table));
                JsonObject json = read(table);
                assertEquals("minecraft:block", json.get("type").getAsString());
                JsonObject pool = json.getAsJsonArray("pools").get(0).getAsJsonObject();
                assertEquals(1, pool.get("rolls").getAsInt());
                JsonObject entry = pool.getAsJsonArray("entries").get(0).getAsJsonObject();
                assertEquals("minecraft:item", entry.get("type").getAsString());
                assertEquals("electricadvantage:" + id, entry.get("name").getAsString());
            }
        }
        assertFalse(Files.exists(root.resolve("pump_pipe_electric.json")));
    }

    @Test public void resourcePackDeclaresTheNativeOneFourteenFormat() throws Exception {
        assertEquals(4, read(Paths.get("src/main/resources/pack.mcmeta"))
                .getAsJsonObject("pack").get("pack_format").getAsInt());
    }

    private static JsonObject read(Path path) throws Exception {
        return new JsonParser().parse(new String(Files.readAllBytes(path), StandardCharsets.UTF_8))
                .getAsJsonObject();
    }
}
