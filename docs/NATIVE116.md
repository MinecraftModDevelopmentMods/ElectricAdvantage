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
- Power bootstrap C source: `e72fc3c39dab98fe2fb7b6a9105e0173585d6001` (local-only).
- Power bootstrap C deobf SHA-256: `C97CE8428480AF34595428F439013F517CB9024092CD8475D56AFBEB8E6F497B`.
- Verified immutable Power release SHA-256: `DBE2226C4A1FE505A94A411EA68540CAEC40186598847BAB936CE16EC8E2FA01`.

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

This historical run used Power source `098d4625abc8a849bd1a11dcf697b3fee0cade7b`
and deobf SHA-256 `78E58DD2AB04E2B9F3267E5735C64AAE7E93EAC97571CB04485038862C853ED3`.
The clean full suite passed
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

### Bootstrap C Revalidation

The active pins above select the parent's frozen local C source and immutable
artifact. C contains native registry, entity migration and capability fixes.
The C clean full suite passed on 27 October 2026 in 49 seconds with
`--rerun-tasks --no-build-cache`: all 47 Java tests in 17 suites, zero failures,
errors or skips, release configuration/dependency/artifact/checksum audits and
production Eclipse generation/classpath checks. The exclusively leased cache
was already warm; this is not a new cold-cache claim.

Fresh ECJ compilation passed for 85 main sources, 17 test sources and the strict
JUnit helper. All 47 tests passed on Java 8 with zero ignored or assumed tests;
every emitted class uses major version 52. The existing 52 main-source compiler
warnings remain visible. The ten static contracts and all 50 recipe unlocks pass.

The raw runtime, deobf and sources jars are byte-identical to frozen B. The
Javadoc jar differs in 18 HTML pages because Power C adds inherited
`areCapabilitiesValid` and changes the declaring owner of `reviveCaps`.
The Javadoc difference is recorded, not normalized away. C Javadoc SHA-256 is
`AB9EBF1BB5A79AE576AF12A25D186D2662EEAD2C79FB4F9A8A5475F776376D4D`.
Raw archive hashes and entry-level diagnostics are under
`evidence/native116-bootstrap-c-20261027-01/`; prior A/B evidence is unchanged.

The previous D actor attempt is INCOMPLETE FAIL: five direct routes retained
wolf UUID/owner and hydroturbine UUID failures; the chained113 route failed the
strict registry/async-save ERROR gate on boot 16. Its raw audit and all source
custody remain preserved under `evidence/native116_actor_d_20261027/` and the
parent's test-rig. No strict identity, item or health oracle was relaxed.
The prepared 108-boot matrix remains pending sealed E inputs and a new runtime
lease. Dedicated native114 actor-origin fixtures remain unqualified; a native115
actor extension needs explicit stopped-source custody, not an old destination PASS.

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

No C Minecraft runtime, packaged smoke, world-upgrade pass or manual acceptance
is claimed. A local-only commit of the C repin and checks is authorized; pushes,
PRs, tags and releases remain unauthorized.
