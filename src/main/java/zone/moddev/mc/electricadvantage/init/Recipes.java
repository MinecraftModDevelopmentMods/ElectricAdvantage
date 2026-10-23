package zone.moddev.mc.electricadvantage.init;

/**
 * Electric Advantage 1.13 recipes are data driven. This hook remains so the
 * historical lifecycle call has a stable home while JSON conditions apply the
 * selected Power Advantage recipe mode.
 */
public final class Recipes {
    private Recipes() {}

    public static void init() {
        Blocks.init();
        Items.init();
    }
}
