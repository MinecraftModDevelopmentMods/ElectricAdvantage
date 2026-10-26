package zone.moddev.mc.electricadvantage.recipe;

import java.util.Locale;
import com.google.gson.JsonObject;
import org.junit.Test;
import zone.moddev.mc.poweradvantage.PowerAdvantage;
import zone.moddev.mc.poweradvantage.RecipeMode;
import static org.junit.Assert.*;

public class RecipeConditionsTest {
    @Test public void nativeConditionsRoundTripAndEvaluateEveryRecipeMode() {
        RecipeMode original = PowerAdvantage.recipeMode;
        try {
            for (boolean inverted : new boolean[] {false, true}) {
                RecipeConditions.ModeSerializer serializer = new RecipeConditions.ModeSerializer(inverted);
                for (RecipeMode expected : RecipeMode.values()) {
                    JsonObject source = new JsonObject();
                    source.addProperty("mode", expected.name().toLowerCase(Locale.ROOT));
                    RecipeConditions.ModeCondition condition = serializer.read(source);
                    assertEquals(serializer.getID(), condition.getID());
                    JsonObject saved = new JsonObject();
                    serializer.write(saved, condition);
                    assertEquals(source, saved);
                    for (RecipeMode selected : RecipeMode.values()) {
                        PowerAdvantage.recipeMode = selected;
                        assertEquals((selected == expected) != inverted, condition.test());
                    }
                }
            }
        } finally {
            PowerAdvantage.recipeMode = original;
        }
    }

    @Test public void modeNamesRemainCaseInsensitiveUnderTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            JsonObject json = new JsonObject();
            json.addProperty("mode", "apocalyptic");
            new RecipeConditions.ModeSerializer(false).read(json);
        } finally {
            Locale.setDefault(original);
        }
    }
}
