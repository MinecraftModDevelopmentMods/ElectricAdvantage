# Native 1.16.5 Port Checks

This checklist separates source changes, deterministic tests, packaged runtime
checks and manual acceptance. Only checks actually executed may be marked passed.

## Inputs

- Electric source baseline: `74aa1cee1ddec821ebcd12b9f8b0515b7aec61d9`.
- Electric candidate: `2.2.8.116051`; Power candidate: `2.4.7.116051`.
- Forge `36.2.34`, official `1.16.5`, MCP `20210115.111550`.
- Base Metals `3.1.1.116051`, source `995f6cc4dd2d5eecc628a42e5c43fa5390c24bca`.
- Base deobf SHA-256: `A7B0C4F394DFAF4E11E0275B1A86CB1D47C7AF8E175759D9BB84F965C684466D`.
- OreSpawn `4.1.0.116051`, source `a113e5d767169189f0e35ade1e173a12d550b00e`.
- OreSpawn deobf SHA-256: `DCEE75480C9DD70EE58B218AAE799C5FAB0525DAF9A05455270F1570C24F7F8F`.
- Power bootstrap B source: `098d4625abc8a849bd1a11dcf697b3fee0cade7b` (local-only).
- Power bootstrap B deobf SHA-256: `78E58DD2AB04E2B9F3267E5735C64AAE7E93EAC97571CB04485038862C853ED3`.
- Verified immutable Power release SHA-256: `60E10F94F447D948244EF088A3B77134C13FBEBCCA1D8DB6210E8630F94A051F`.

Copy the final verified jars to immutable local input paths before building.
Do not compile against a sibling output that another worker may overwrite.
The release audit retains exact source/hash expectations for this candidate.
Do not relax those checks. Publish the local-only Power source upstream before
hosted build or release readiness is claimed.

## Source Contracts

- All recipe, advancement, tag, loot, model, texture and translation resources
  are retained byte-for-byte except native pack metadata and dependency ranges.
- All production string identities and numeric constants match the accepted
  baseline, apart from the candidate version string.
- The drill uses world-only conduit callbacks. Power owns `RegistryKey<World>`
  lookup and capability/NBT lifecycle; Electric adds no competing adapters.
- Tile persistence uses `load(BlockState, CompoundNBT)` and `save(CompoundNBT)`.
  Turret update tags receive the native state and retain render synchronization.
- Lithium needs a level-1 pickaxe; sulfur needs level 0. The native correct-tool
  prerequisite must be enabled, not merely the harvest-tool metadata.
- Scaffold occlusion, redstone conduction and full collision are separate checks.
  Solar redstone conduction remains explicitly true. A renamed property flag is
  not evidence of equivalent suffocation, pathfinding, light or rendering behavior.
- Rendering retains all geometry, UVs, winding normals, light/overlay values,
  timing, six drill directions, tracking settings and original test tolerances.
- Condition registration uses common setup's `enqueueWork`. Native tag loading
  and reload integration belong to Power and still need runtime proof.
- Electric's tag resources contain no legacy `optional` arrays. Any future
  optional reference must use `values: [{"id": "...", "required": false}]`.

## Evidence Stages

The initial static report is preserved at `evidence/native-port-static-red.json`;
it retains the expected failures against the unported 1.15 source. Later reports
use new names and must not overwrite failed or partial evidence.

Run the static checker and recipe-unlock check before compilation. These do not
load Minecraft or establish Java compilation. The Java suite retains the 115
tests and adds actual Forge harvest-hook eligibility checks.

### Bootstrap A Results (27 October 2026)

These results use Power bootstrap A, source `ce90c24c302d0ff3565db2db0c6c94a3b5f21f83`
and deobf SHA-256 `8BBB03DD7E4AF08A7390A161ED5AA94E292BEE56EF60E550B5512E967E0E9174`.
These results and immutable jars remain preserved. Bootstrap B adds only the
parent's constructor-time GUI matrix initialization; it does not change the API.

- Production/test compilation passed with the exact pinned dependencies.
- All 47 Java tests passed, with zero failures, errors or skips. Both earlier
  failed test reports remain in `evidence/native116-test-red-01` and `-02`.
- The native persistence tests now pass an explicit drill state to `load` before
  world attachment and bootstrap native registries before constructing blocks.
  Their progress, energy, geometry and NBT expectations are unchanged.
- `clean cleanEclipse check build javadoc verifyReleaseDependencies
  verifyReleaseArtifacts writeReleaseChecksums verifyEclipseProductionClasspath`
  passed. The clean suite reused the successful Java test result from Gradle's
  cache; the preceding successful test run executed the suite directly.
- The full-suite log is `evidence/native116-full-suite-01.log`. The failed
  launcher/compile logs and static red reports remain alongside it.
- Java 8 bytecode, exact dependency hashes, no dependency embedding, model/tag
  resource checks, release packaging and production Eclipse paths passed their
  deterministic audits. Javadoc retains one pre-existing empty-return warning;
  Gradle reports existing deprecations for its future version 10.

### Bootstrap B Revalidation

The exact B source/deobf pins above are configured. The clean full suite passed
against the immutable B jar on 27 October 2026 with `--rerun-tasks` and
`--no-build-cache`: production/test compilation, all 47 Java tests (zero failures,
errors or skips), release configuration/dependency/artifact/checksum audits and
production Eclipse generation/classpath checks. No tests were reused from cache.

The log is `evidence/native116-bootstrap-b-full-suite-01.log`; raw Java reports are
preserved in `evidence/native116-bootstrap-b-test-green-01`. The ten static source
contracts and all 50 recipe unlocks also pass. The shared-cache lease was released
after the Gradle process completed. No Minecraft client or server was launched.

Frozen B artifacts are under `evidence/native116-bootstrap-b-candidate`. The A
reports, handoff and candidate directory remain unchanged. The B handoff is
`evidence/native116-bootstrap-b-handoff.json`.

Remaining runtime and acceptance work, only under an explicit runtime lease:

1. Test the packaged candidate's recipes,
   unlocks, native tags, condition modes, resource reload and restart behavior.
2. Compare machine inventory, energy, fluids, processing ratios, redstone and
   comparator behavior against the accepted 115 behavior.
3. Test fresh and disposable upgraded worlds, all legacy aliases, repeated
   load/save without ticking, drill relocation, pending growth work, turbine
   parent links, turret ownership/target state, dimensions and chunk lifecycle.
4. Verify real player mining below/at the required level, sulfur drops and Silk
   Touch, ordinary block/inventory drops, hidden pump pipes and network removal.
5. Manually inspect all GUIs, textures, beam directions/lighting, turret controls,
   hydroturbine orientation/spin, sounds, interaction and multiplayer behavior.

No Minecraft runtime, packaged smoke, world-upgrade pass
or manual acceptance is claimed. Commits, pushes, PRs,
tags and releases are outside the current authorization.
