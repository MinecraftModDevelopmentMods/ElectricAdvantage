package zone.moddev.mc.electricadvantage.resources;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void hostedBuildsProvisionAllThreePinnedToolchainsBeforeGradle() throws Exception {
        for (String workflow : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", workflow)), StandardCharsets.UTF_8);
            int setup = source.indexOf("- name: Set up Gradle");
            for (String version : new String[] {"25.0.3+9.0.LTS", "8.0.502+7", "17.0.1+12"}) {
                int pin = source.indexOf("java-version: '" + version + "'");
                assertTrue(workflow + " missing pinned JDK " + version, pin >= 0 && pin < setup);
            }
            assertTrue(source.contains("installations.paths=$JAVA_HOME,$JAVA_HOME_8_X64,$JAVA_HOME_25_X64"));
            assertTrue(source.contains("installations.auto-detect=false"));
            assertTrue(source.contains("installations.auto-download=false"));
            assertTrue(source.indexOf("- name: Use pinned build toolchains") < setup);
            assertFalse(source.contains("distribution: microsoft"));
        }
    }

    @Test public void dependencyPolicyPrecedesEveryPinnedCheckout() throws Exception {
        for (String workflow : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", workflow)), StandardCharsets.UTF_8);
            int policy = source.indexOf("git config --global core.autocrlf false");
            assertTrue(policy > 0);
            assertTrue(policy < source.indexOf("repository: MinecraftModDevelopmentMods/OreSpawn"));
            assertTrue(policy < source.indexOf("repository: MinecraftModDevelopmentMods/BaseMetals"));
            assertTrue(policy < source.indexOf("repository: MinecraftModDevelopmentMods/PowerAdvantage"));
            assertTrue(source.contains("ref: 5613baf876747ed071cb7ad7e2aff4e0ce4f0a03"));
            assertTrue(source.contains("ref: 1d166d917663d4b4a5a88a86440adc0ae761e393"));
            assertTrue(source.contains("ref: b15e2091e9647860bd90284aef6135a311692864"));
            assertTrue(source.contains("branches: [master-1.15.2"));
            assertFalse(source.contains("master-1.14.4"));
        }
    }
}
