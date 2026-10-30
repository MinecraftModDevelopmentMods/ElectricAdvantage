package zone.moddev.mc.electricadvantage.gui;

import org.junit.Test;
import static org.junit.Assert.*;

public class GUIHelperTest {
    @Test public void barRetainsItsEightSegmentScale() {
        assertEquals(0, GUIHelper.progressBarWidth(0F));
        assertEquals(17, GUIHelper.progressBarWidth(0.5F));
        assertEquals(33, GUIHelper.progressBarWidth(1F));
    }

    @Test public void invalidAndOversizedValuesCannotEscapeTheTexture() {
        for (float value : new float[]{-1F, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN}) {
            assertEquals(0, GUIHelper.progressBarWidth(value));
        }
        assertEquals(33, GUIHelper.progressBarWidth(15F));
        assertEquals(33, GUIHelper.progressBarWidth(Float.MAX_VALUE));
    }

    @Test public void finiteReadingsAreMonotonicAndBounded() {
        int previous = 0;
        for (int i = -100; i <= 200; i++) {
            int width = GUIHelper.progressBarWidth(i / 100F);
            assertTrue(width >= previous && width >= 0 && width <= 33);
            previous = width;
        }
    }
}
