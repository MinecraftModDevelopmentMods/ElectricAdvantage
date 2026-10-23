# ElectricAdvantage

Electric Advantage adds electric machines, generators, automation, and infrastructure to Power Advantage for Minecraft 1.12.2.

## Drill Reload Fix

Version `2.2.3.112021` keeps unfinished drill work when a chunk or world reloads.
Existing inventories, energy, orientation and saved-world names are unchanged.
The regression tests cover resumed progress and full server restarts.

## Building Minecraft 1.12.2

The `master-1.12.2` build uses ForgeGradle 7.0.34 and Gradle 9.6.1. Run Gradle with Java 17; Gradle resolves the Java 8 toolchain used for compilation.

First build the pinned sibling PowerAdvantage development jar:

```text
cd ..\..\PowerAdvantage
gradlew.bat deobfJar
```

Then build and audit ElectricAdvantage:

```text
gradlew.bat clean check build javadoc verifyReleaseDependencies verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath
```

Release jars are written to `build/libs`. The deobfuscated development jar is written to `build/libs-dev` by `deobfJar` or `build`.

For Eclipse, import this repository as an existing Gradle project and run:

```text
gradlew.bat cleanEclipse verifyEclipseProductionClasspath
```

Power Advantage is required at compile time and runtime but is never bundled. Base Metals remains a required distribution dependency while its integrations stay reflection-based and absent from the compile classpath.

## Hydroturbine Persistence

Version `2.2.2.112021` retains the generator coordinate when a loaded turbine is saved before its first update. The existing `parentPos` key, entity identity and generated electricity remain unchanged. This also protects the intermediate 1.12 save during a chained upgrade to 1.13; it cannot recover a coordinate already missing from an old save.

The normal `check` task includes repeated pre-tick save tests, stale-link clearing and live-generator position checks. The Advantage Works migration harness tests the packaged jar through full 1.12 and 1.13 process restarts.
