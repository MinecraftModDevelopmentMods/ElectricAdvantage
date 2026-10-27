# Electric Advantage

**Minecraft 1.16.5 port candidate, version 2.2.8.116051.** Local compilation, all
47 Java tests, release audits, Eclipse checks and fresh ECJ compilation pass
against Power bootstrap C. Earlier A/B build evidence and the failed D actor
campaign are preserved separately. C's packaged runtime qualification and manual
acceptance remain pending. This is not a published release.
See [the native port checklist](docs/NATIVE116.md).
The [accepted 1.15 record](https://github.com/MinecraftModDevelopmentMods/PowerAdvantage/blob/master-1.15.2/docs/1.15/CURRENT-QUALIFICATION.md)
is a behavior reference, not evidence that this target passes.

Electric Advantage adds electric machines, generation, storage, automation,
and defence to Power Advantage for Minecraft 1.16.5.

## Build Target

The build targets Forge `36.2.34`, official `1.16.5` mappings and MCP
`20210115.111550`, using ForgeGradle `7.0.34`, Renamer `1.1.5` and Gradle
`9.6.1`. Run Gradle with Temurin `17.0.1+12`; production code and Minecraft
use Temurin `8.0.502+7`. Mavenizer needs Temurin `25.0.3+9.0.LTS`, including
on a cold cache. CI registers all three JDK paths and disables toolchain
inference and downloads.

Build inputs are separate, non-embedded deobfuscated jars:

- Power Advantage `2.4.7.116051`: source `e72fc3c39dab98fe2fb7b6a9105e0173585d6001`.
- Base Metals `3.1.1.116051`: source `995f6cc4dd2d5eecc628a42e5c43fa5390c24bca`.
- OreSpawn `4.1.0.116051`: source `a113e5d767169189f0e35ade1e173a12d550b00e`.

Exact checksums are recorded in `gradle.properties`. Power deobf SHA-256 is
`C97CE8428480AF34595428F439013F517CB9024092CD8475D56AFBEB8E6F497B`.
Its source is local-only; hosted reproducibility is pending upstream source
publication. Do not bypass the release audits or substitute a preliminary jar.
Use immutable copies for verification, not a sibling's output that another
build may replace. CI rejects pins that differ from this audited candidate.

With an authorized build slot, pass the immutable input paths and run:

```text
gradlew.bat classes test deobfJar -PpowerAdvantageDeobfJar=<jar> -PbaseMetalsDeobfJar=<jar> -PoreSpawnDeobfJar=<jar>
```

The complete deterministic release gates are:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath
```

These gates passed locally on 27 October 2026 against the exact immutable C
inputs, with all 47 tests rerun and no build-cache reuse. The independent ECJ
run also passed all 47 tests on Java 8. These results do not replace packaged client/server tests,
disposable-world migration checks or manual acceptance.
Do not run another build concurrently against a shared Gradle cache.

Static checks need no Gradle or game launch:

```text
node tools/verify-native-port.js --report build/native-port-static.json
node tools/generate-recipe-unlocks.js --check
```

The static contract compares resources, identity strings and numeric constants
with accepted source `74aa1cee1ddec821ebcd12b9f8b0515b7aec61d9`. Its reports are
not compilation, gameplay or migration results.

## Eclipse

Import this repository as an existing Gradle project. Once dependencies and
build access are available, run `cleanEclipse verifyEclipseProductionClasspath`.
Eclipse mod discovery uses `bin/main`; command-line launches use
`build/forge-run-mod-classes/main`. The production classpath audit checks those
paths and excludes test output.

The Eclipse resource root is `build/resources/main`, so its builder copies
expanded metadata. After resource or version edits, process resources before
launching. Do not add raw source metadata to the production classpath.

## Compatibility

The implementation namespace remains `zone.moddev.mc.electricadvantage`.
Runtime registries, legacy aliases, tile/entity IDs, NBT keys, recipe IDs,
ratios, tags, OreSpawn provider IDs and assets are retained. Electric uses
Power Advantage's shared migration hook, dimension-key network registry,
capability adapters and native lifecycle contracts. It does not create
synthetic numeric dimension IDs or a second capability implementation.

All 46 crafting variants and four smelting recipes retain ingredient-triggered
recipe-book discoveries. The eight Base Metals crushing recipes are not
crafting-book entries. Regenerate unlocks only after an intentional recipe
change; `check` audits their coverage and criteria.

Base Metals remains optional in Forge metadata; CurseForge's existing required
installation relationship is unchanged. Typed integration uses the matching
Base Metals and OreSpawn APIs without embedding either mod.

The RF- and Tech Reborn-labelled converters belong to Power Advantage and use
Forge Energy. Visuals, GUI interaction, sound, weapons, hydroturbine behavior,
multiplayer, tag reloads, dimensions and world upgrades need target-specific
verification. No 1.16 publication is authorized before manual acceptance.

Always back up a whole world before upgrading and test only disposable copies.
Keep migration backups and failed evidence. Bounded Advantage results never
qualify unrelated mods or an arbitrary existing modpack.
