# ElectricAdvantage

**Minecraft 1.14.4 port, version 2.2.6.114041.** Native compilation and all 34 unit
tests pass, with no tests skipped. The combined-mod automated checks also cover
client resources, connected machinery, drill movement, chunk lifecycle, restart
persistence and disposable-copy migration. The
[1.14 test results](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/docs/1.14/BROADER-QUALIFICATION.md)
describe the tested scenarios and remaining limits, not a guarantee for an entire modpack.

Electric Advantage adds electric machines, generation, storage, automation, and defence to Power Advantage for Minecraft 1.14.4.

Every crafting and vanilla smelting variant has an ingredient-triggered recipe-book discovery. See the [shared material checklist and discovery tests](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/RECIPE-DISCOVERY.md) and [manufacturing progression audit](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MANUFACTURING-PROGRESSION.md). Regenerate unlock JSON after recipe changes with `node tools/generate-recipe-unlocks.js`; `check` audits recipe coverage and criteria. Custom machine and crushing recipes are not crafting-book entries.

## Building Minecraft 1.14.4

The `master-1.14.4` build targets Forge `28.2.26`, mappings `snapshot 20190719-1.14.3`,
and MCP `20190829.143755`, using ForgeGradle `7.0.34` and Gradle `9.6.1`.
Run Gradle with Java 17; production code is compiled for Java 8.

Build the matching 1.14 PowerAdvantage, Base Metals and OreSpawn deobf jars first.
Dependency versions, checksums and the exact Power source reference belong in
`gradle.properties`. Release audits reject missing or placeholder pins; do not
bypass those checks or substitute a 1.13 dependency.
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

For an all-Advantage play-test, run `prepareCombinedEclipseLaunch` in the sibling PowerAdvantage checkout and select its `runClientAllAdvantage.launch`. The [native resource probe](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.14.4/test-rig/client-resources/README.md) checks models, textures and names across all three mods. Resource checks do not replace gameplay testing.

Automated coverage is bounded to the recorded scenarios. It does not establish every
in-world GUI, rendering, sound, control or weapon interaction, multiplayer behavior,
or compatibility with arbitrary third-party mods. Unit/resource tests alone are not
runtime or world-upgrade evidence.

The local [test-coverage backport](../../PowerAdvantage/docs/1.14/TEST-COVERAGE-BACKPORT.md) records the stronger machine, restart and GUI checks for the repaired candidate.

The implementation namespace is `zone.moddev.mc.electricadvantage`. Runtime identities, registries, tile/entity IDs, NBT, recipes, tags, OreSpawn provider IDs, and assets remain stable. Electric registers its legacy state, tile, and hydroturbine aliases with PowerAdvantage's shared migration hook.

The RF- and Tech Reborn-labelled converters belong to Power Advantage and now use Forge Energy.
The historical [1.13 migration guide](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.13.2/docs/1.13/MIGRATION.md) explains the legacy conversion and backup process; the 1.14 test results above describe this port's coverage. Always make a whole-world backup before upgrading, keep the complete set of migration backups, and test only disposable copies. Check that every other mod supports the target version; these results do not qualify unrelated mods or an entire existing modpack.

## Generator Follow-Up

The [generator follow-up](../../PowerAdvantage/docs/1.14/TEST-COVERAGE-BACKPORT.md#generator-follow-up) records the older `pa114-candidate-c` inputs.
Its 126 operating jobs and 126 cold checks, 39 GUI windows, 351 caption cases,
355 rejected broken controls, thirteen frozen routes and five connected routes
passed. The corrected paired 30-minute run also passed; the original POI failure
is retained. This is a pre-final Steam-control checkpoint, not qualification of
the newer SteamAdvantage jars or harness. Those still need the final sweep.
Manual play-testing and release review remain separate.

## Final T Candidate

The [final-t checkpoint](../../PowerAdvantage/docs/1.14/TEST-COVERAGE-BACKPORT.md#final-t-candidate)
records profile `pa114-candidate-e` and its exact jar hashes. All 132 operating
jobs and 132 cold-restart comparisons pass, including an independent audit of
six Steam controls and their nine tile-hold records. GUI checks pass for 39
windows, 351 caption cases and 355 rejected broken controls.
Frozen migration passed on 13 routes and connected-source operation passed on
all five declared routes.
Fresh and migrated-copy 30-minute runs pass. Power and Electric retain their
exact-hash release and reproducibility proof; final-t Steam passes its full suite
and two independent export builds. All 18 mod/version release and reproducibility
gates pass across the six maintained Minecraft lines. Both requested broad
profiles and their independent audit pass; the reporter-only unit/replay
qualification preserves the original native proof. Survival harvest checks pass
before and after restart. The report's extra network/drill scopes are now
qualified; manual acceptance remains separate. Earlier checkpoints keep their original scope;
No release publication is included.
