package com.mcmoddev.electricadvantage.gui;

import java.lang.reflect.Method;
import org.junit.Test;
import static org.junit.Assert.*;

public class GUIHelperTest {
    @Test public void barRetainsItsEightSegmentScale() throws Exception {
        assertEquals(0, progressBarWidth(0F));
        assertEquals(17, progressBarWidth(0.5F));
        assertEquals(33, progressBarWidth(1F));
    }

    @Test public void invalidAndOversizedValuesCannotEscapeTheTexture() throws Exception {
        for (float value : new float[]{-1F, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN}) {
            assertEquals(0, progressBarWidth(value));
        }
        assertEquals(33, progressBarWidth(15F));
        assertEquals(33, progressBarWidth(Float.MAX_VALUE));
    }

    @Test public void finiteReadingsAreMonotonicAndBounded() throws Exception {
        int previous = 0;
        for (int i = -100; i <= 200; i++) {
            int width = progressBarWidth(i / 100F);
            assertTrue(width >= previous && width >= 0 && width <= 33);
            previous = width;
        }
    }

    private static int progressBarWidth(float value) throws Exception {
        Method method;
        try { method = GUIHelper.class.getDeclaredMethod("progressBarWidth", float.class); }
        catch (NoSuchMethodException missing) { throw new AssertionError("Electric progress bars need one finite, bounded eight-segment scale", missing); }
        method.setAccessible(true);
        return (Integer) method.invoke(null, value);
    }
}
