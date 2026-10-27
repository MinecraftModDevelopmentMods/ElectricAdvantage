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
            assertTrue(source.contains("ref: a113e5d767169189f0e35ade1e173a12d550b00e"));
            assertTrue(source.contains("ref: 995f6cc4dd2d5eecc628a42e5c43fa5390c24bca"));
            assertTrue(source.contains("BaseMetals-3.1.1.116051-deobf.jar"));
            assertTrue(source.contains("ref: 2116a35a40dd150d79e7dfdcdc891637793178b7"));
            assertTrue(source.contains("F58B93DAF03CF303F32B18CCCA140E55E886A2BA82732A3F286ADF3E31194B6A"));
            assertFalse(source.contains("098d4625abc8a849bd1a11dcf697b3fee0cade7b"));
            assertFalse(source.contains("78E58DD2AB04E2B9F3267E5735C64AAE7E93EAC97571CB04485038862C853ED3"));
            assertFalse(source.contains("ce90c24c302d0ff3565db2db0c6c94a3b5f21f83"));
            assertFalse(source.contains("8BBB03DD7E4AF08A7390A161ED5AA94E292BEE56EF60E550B5512E967E0E9174"));
            assertTrue(source.indexOf("- name: Require final PowerAdvantage pin") < source.indexOf("- name: Check out pinned OreSpawn"));
            assertTrue(source.contains("branches: [master-1.16.5"));
            assertFalse(source.contains("master-1.14.4"));
        }
    }
}
