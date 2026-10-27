# ElectricAdvantage

**Minecraft 1.15.2, version 2.2.7.115021.** See the
[shared test record](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.15.2/docs/1.15/CURRENT-QUALIFICATION.md)
for exact jars, automated machine and upgrade results, and remaining manual
checks. Visuals and recipe unlocks were accepted on 27 October 2026. Historical
[1.14 test results](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/docs/1.14/BROADER-QUALIFICATION.md)
are not 1.15 qualification evidence.

Electric Advantage adds electric machines, generation, storage, automation, and defence to Power Advantage for Minecraft 1.15.2.

Every crafting and vanilla smelting variant has an ingredient-triggered recipe-book discovery. See the [shared material checklist and discovery tests](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/RECIPE-DISCOVERY.md) and [manufacturing progression audit](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MANUFACTURING-PROGRESSION.md). Regenerate unlock JSON after recipe changes with `node tools/generate-recipe-unlocks.js`; `check` audits recipe coverage and criteria. Custom machine and crushing recipes are not crafting-book entries.

## Building Minecraft 1.15.2

The `master-1.15.2` build targets Forge `31.2.57`, mappings `snapshot 20200514-1.15.1`,
and MCP `20200515.085601`, using ForgeGradle `7.0.34`, Renamer `1.1.5` and Gradle `9.6.1`.
Run Gradle with Temurin `17.0.1+12`; production code uses Temurin `8.0.502+7`.
Mavenizer also needs Temurin `25.0.3+9.0.LTS`, including on a cold cache. CI explicitly
registers all three JDK paths and disables toolchain auto-detection and downloads.

Build the matching 1.15 PowerAdvantage, Base Metals and OreSpawn deobf jars first.
Dependency versions, checksums and the exact Power source reference belong in
`gradle.properties`. Release audits reject missing or placeholder pins; do not
bypass those checks or substitute another target's dependencies. Power Advantage
`2.4.6.115021` is pinned to source `6666099a734eba16561d98c86f3f4bb1b7c742f7`.
Base Metals `3.0.2.115021` and OreSpawn `4.1.1.115021` also have exact source and SHA-256 pins.
Base Metals source `ac1781e7cade15045ffe0135077087b4d1722603` and the Power
source pin above identify the exact tested dependencies. CI builds them before
Electric Advantage; none is embedded in the Electric Advantage jar.

With the standard sibling layout:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

For local development with the matching native jars, compile and run tests:

```text
gradlew.bat classes test deobfJar
```

With the exact Power dependency pinned, run the complete release gates:

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

For an all-Advantage play-test, run `prepareCombinedEclipseLaunch` in the sibling PowerAdvantage checkout and select its `runClientAllAdvantage.launch`. Combined 1.15 client resources and gameplay require new target-specific evidence; resource checks do not replace gameplay testing.

Automated coverage is bounded to the recorded scenarios. It does not establish every
in-world GUI, rendering, sound, control or weapon interaction, multiplayer behavior,
or compatibility with arbitrary third-party mods. Unit/resource tests alone are not
runtime or world-upgrade evidence.

The implementation namespace is `zone.moddev.mc.electricadvantage`. Runtime identities, registries, tile/entity IDs, NBT, recipes, tags, OreSpawn provider IDs, and assets remain stable. Electric registers its legacy state, tile, and hydroturbine aliases with PowerAdvantage's shared migration hook.

The RF- and Tech Reborn-labelled converters belong to Power Advantage and now use Forge Energy.
The historical [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) explains the legacy conversion and backup process; the shared 1.15 test record lists the completed upgrade routes and their limits. Always make a whole-world backup before upgrading, keep the complete set of migration backups, and test a disposable copy first. Check that every other mod supports the target version; bounded Advantage tests do not qualify unrelated mods or an entire modpack.
