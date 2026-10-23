# ElectricAdvantage

Electric Advantage adds electric machines, generation, storage, automation, and defence to Power Advantage for Minecraft 1.13.2.

Every crafting and vanilla smelting variant has an ingredient-triggered recipe-book discovery. See the [shared material checklist and discovery tests](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/RECIPE-DISCOVERY.md) and [manufacturing progression audit](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MANUFACTURING-PROGRESSION.md). Regenerate unlock JSON after recipe changes with `node tools/generate-recipe-unlocks.js`; `check` audits recipe coverage and criteria. Custom machine and crushing recipes are not crafting-book entries.

## Building Minecraft 1.13.2

The `master-1.13.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; production code is compiled for Java 8.

Build the pinned PowerAdvantage, Base Metals, and OreSpawn deobf jars first.
Exact source commits and checksums are recorded in `gradle.properties`; CI builds the same commits.
With the standard sibling layout:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

Then build ElectricAdvantage:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath
```

Alternative layouts may pass `-PpowerAdvantageDeobfJar=<path>`, `-PbaseMetalsDeobfJar=<path>`, and `-PoreSpawnDeobfJar=<path>`. None of those dependencies is embedded.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

Refresh the project in Eclipse, allow its Java build to finish, and launch the generated `runClient.launch`. Eclipse launches discover ElectricAdvantage from `bin/main`; command-line Gradle launches use `build/forge-run-mod-classes/main`. The Eclipse audit checks that discovery points at the same production output Eclipse compiles into.

The Eclipse resource root is `build/resources/main`, so its builder copies expanded mod metadata rather than raw source templates. Refresh the Gradle project and clean/rebuild after this setup changes. After resource/version edits, run `processResources` or refresh Gradle before launching; do not put `src/main/resources` back on the Eclipse Java build path.

## Compatibility

For an all-Advantage play-test, run `prepareCombinedEclipseLaunch` in the sibling PowerAdvantage checkout and select its `runClientAllAdvantage.launch`. The [native resource probe](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/test-rig/client-resources/README.md) checks models, textures and names across all three mods. These resource checks do not replace gameplay testing.

The implementation namespace is `zone.moddev.mc.electricadvantage`. Runtime identities, registries, tile/entity IDs, NBT, recipes, tags, OreSpawn provider IDs, and assets remain stable. Electric registers its legacy state, tile, and hydroturbine aliases with PowerAdvantage's shared migration hook.

The RF- and Tech Reborn-labelled converters belong to Power Advantage and now use Forge Energy.
Read the [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) before upgrading, and always use a backup. The [qualification notes](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/CURRENT-QUALIFICATION.md) list the tested scenarios and remaining gaps.
