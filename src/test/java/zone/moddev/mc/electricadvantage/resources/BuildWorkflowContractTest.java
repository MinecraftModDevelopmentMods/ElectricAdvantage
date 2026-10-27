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
            assertTrue(source.contains("-Porg.gradle.java.installations.paths=$JAVA_HOME,$JAVA_HOME_8_X64,$JAVA_HOME_25_X64"));
            assertTrue(source.contains("-Porg.gradle.java.installations.auto-detect=false"));
            assertTrue(source.contains("-Porg.gradle.java.installations.auto-download=false"));
            assertFalse(source.contains("-Dorg.gradle.java.installations."));
            for (String line : source.split("\\R")) {
                if (line.contains("./gradlew ") && !line.contains("chmod")) {
                    assertTrue(workflow + ": " + line, line.contains("./gradlew $MMD_GRADLE_TOOLCHAINS "));
                }
            }
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
            assertTrue(source.contains("ref: ac1781e7cade15045ffe0135077087b4d1722603"));
            assertTrue(source.contains("BaseMetals-3.0.2.115021-deobf.jar"));
            assertTrue(source.contains("ref: 6666099a734eba16561d98c86f3f4bb1b7c742f7"));
            assertTrue(source.contains("branches: [master-1.15.2"));
            assertFalse(source.contains("master-1.14.4"));
        }
    }
}
