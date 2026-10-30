package zone.moddev.mc.electricadvantage.machines;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PhotovoltaicSolidityTest {
    @BeforeClass public static void bootstrap() {
        zone.moddev.mc.electricadvantage.testsupport.Forge113TestBootstrap.register();
    }
    @Test public void panelKeepsItsLegacyFullBlockProperty() {
        PhotovoltaicGeneratorBlock panel = new PhotovoltaicGeneratorBlock();
        assertTrue(panel.isSolid(panel.getDefaultState()));
        assertTrue(panel.getDefaultState().isSolid());
    }
}
