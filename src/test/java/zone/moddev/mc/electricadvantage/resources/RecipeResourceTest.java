package zone.moddev.mc.electricadvantage.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

public class RecipeResourceTest {
    @Test public void allRecipesUseTheNativeOneFourteenDataPackLocationAndSerializers() throws IOException {
        Path recipes = Paths.get("src/main/resources/data/electricadvantage/recipes");
        List<Path> files;
        try (Stream<Path> stream = Files.list(recipes)) {
            files = stream.filter(path -> path.toString().endsWith(".json"))
                    .sorted().collect(Collectors.toList());
        }
        assertEquals(58, files.size());
        int crafting = 0, smelting = 0, crushing = 0;
        for (Path file : files) {
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            JsonObject json = new JsonParser().parse(text).getAsJsonObject();
            String type = json.get("type").getAsString();
            assertTrue(file + " has unsupported serializer " + type,
                    "minecraft:crafting_shaped".equals(type)
                            || "minecraft:crafting_shapeless".equals(type)
                            || "minecraft:smelting".equals(type)
                            || "basemetals:crushing".equals(type));
            if (type.startsWith("minecraft:crafting_")) crafting++;
            else if ("minecraft:smelting".equals(type)) smelting++;
            else crushing++;
            assertFalse(file + " still contains an ore dictionary ingredient", text.contains("ore_dict"));
        }
        assertEquals(46, crafting);
        assertEquals(4, smelting);
        assertEquals(8, crushing);
        assertFalse(Files.exists(Paths.get("src/main/resources/assets/electricadvantage/recipes")));
    }
}
