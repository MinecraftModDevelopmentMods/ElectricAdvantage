const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const root = path.resolve(__dirname, '..');
const baseline = '74aa1cee1ddec821ebcd12b9f8b0515b7aec61d9';
const javaRoot = 'src/main/java/zone/moddev/mc/electricadvantage/';
const read = name => fs.readFileSync(path.join(root, name), 'utf8');
const git = (...args) => execFileSync('git', args, { cwd: root });
const tracked = git('ls-tree', '-r', '--name-only', baseline).toString().trim().split('\n');
const results = [];
function check(name, run) {
    try { run(); results.push({ name, passed: true }); }
    catch (error) { results.push({ name, passed: false, error: error.message }); }
}
function strings(source) {
    const tokens = source.match(/"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\/[^\n]*|\/\*[\s\S]*?\*\//g) || [];
    return tokens.filter(token => token.startsWith('"'))
        .filter(token => token !== '"2.2.7.115021"' && token !== '"2.2.8.116051"').sort();
}
function numbers(source) {
    const tokens = source.match(/"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\/[^\n]*|\/\*[\s\S]*?\*\/|[A-Za-z_$][A-Za-z0-9_$]*|(?:0[xX][\da-fA-F_]+|(?:\d[\d_]*\.?[\d_]*|\.\d[\d_]*)(?:[eE][+-]?[\d_]+)?)[dDfFlL]?/g) || [];
    return tokens.filter(token => /^\d|^\.\d/.test(token));
}

check('Exact native target metadata', () => {
    const properties = read('gradle.properties');
    for (const pair of ['minecraft_version=1.16.5', 'forge_version=36.2.34',
        'mapping_channel=official', 'mapping_version=1.16.5', 'mcp_version=20210115.111550',
        'mod_version=2.2.8.116051', 'poweradvantage_version=2.4.7.116051',
        'poweradvantage_source_ref=e72fc3c39dab98fe2fb7b6a9105e0173585d6001',
        'poweradvantage_deobf_sha256=C97CE8428480AF34595428F439013F517CB9024092CD8475D56AFBEB8E6F497B',
        'basemetals_version=3.1.1.116051', 'orespawn_version=4.1.0.116051',
        'java_version=8', 'gradle_java_version=17']) {
        assert(properties.split(/\r?\n/).includes(pair), pair);
    }
    assert.equal(JSON.parse(read('src/main/resources/pack.mcmeta')).pack.pack_format, 6);
});
check('115 assets, recipes, tags, loot, and unlock bytes are unchanged', () => {
    for (const name of tracked.filter(name => name.startsWith('src/main/resources/')
        && !['src/main/resources/pack.mcmeta', 'src/main/resources/META-INF/mods.toml'].includes(name))) {
        assert.deepEqual(fs.readFileSync(path.join(root, name)), git('show', `${baseline}:${name}`), name);
    }
});
check('Production string identities and NBT keys match the accepted 115 source', () => {
    for (const name of tracked.filter(name => name.startsWith(javaRoot) && name.endsWith('.java'))) {
        assert.deepEqual(strings(read(name)), strings(git('show', `${baseline}:${name}`).toString()), name);
    }
});
check('Production numeric constants and rendering test tolerances match 115', () => {
    const rendererTest = 'src/test/java/zone/moddev/mc/electricadvantage/graphics/RendererBufferContractTest.java';
    for (const name of tracked.filter(name => name.startsWith(javaRoot) && name.endsWith('.java') || name === rendererTest)) {
        assert.deepEqual(numbers(read(name)), numbers(git('show', `${baseline}:${name}`).toString()), name);
    }
});
check('Tag entries use native values rather than a legacy optional array', () => {
    for (const name of tracked.filter(name => name.includes('/tags/') && name.endsWith('.json'))) {
        const tag = JSON.parse(read(name));
        assert(!Object.hasOwn(tag, 'optional'), name);
        assert(Array.isArray(tag.values), name);
        for (const value of tag.values) {
            assert(typeof value === 'string' || value && typeof value.id === 'string'
                && (!Object.hasOwn(value, 'required') || typeof value.required === 'boolean'), name);
        }
    }
});
check('World-only conduit callbacks and native solar dimension type', () => {
    const drill = read(javaRoot + 'machines/ElectricDrillBlock.java');
    const tile = read(javaRoot + 'machines/ElectricDrillTileEntity.java');
    assert(!/dimension\.getType|\.getId\(\)/.test(drill + tile));
    assert.match(drill, /conduitBlockPlacedEvent\(\s*world, pos, Power\.ELECTRIC_POWER\)/);
    assert.match(tile, /conduitBlockRemovedEvent\(getLevel\(\), getBlockPos\(\), getEnergyType\(\)\)/);
    assert(read(javaRoot + 'machines/PhotovoltaicGeneratorTileEntity.java').includes('dimensionType().hasSkyLight()'));
});
check('Native persistence overrides and state-aware turret updates', () => {
    for (const name of ['ElectricMachineTileEntity', 'ElectricGeneratorTileEntity', 'GrowthChamberControllerTileEntity']) {
        const source = read(javaRoot + `machines/${name}.java`);
        assert.match(source, /void load\((?:net\.minecraft\.block\.)?BlockState state, (?:final )?CompoundNBT tagRoot\)/);
        assert(source.includes('super.load(state, tagRoot)'));
        assert.match(source, /CompoundNBT save\(/);
        assert(source.includes('super.save(tagRoot)'));
    }
    assert.match(read(javaRoot + 'machines/LaserTurretTileEntity.java'), /handleUpdateTag\(BlockState state, CompoundNBT tag\)/);
});
check('Native mining prerequisite and serialized recipe registration', () => {
    assert.equal((read(javaRoot + 'blocks/Ore.java').match(/requiresCorrectToolForDrops\(\)/g) || []).length, 2);
    assert(read(javaRoot + 'ElectricAdvantage.java').includes('event.enqueueWork(RecipeConditions::register)'));
    assert(!read(javaRoot + 'ElectricAdvantage.java').includes('DeferredWorkQueue.runLater'));
});
check('Custom fluid API and parent inventory names are preserved', () => {
    assert(read(javaRoot + 'util/LegacyFluidHandler.java').includes('extends IAdvantageFluidHandler'));
    assert(read(javaRoot + 'machines/ElectricPumpTileEntity.java').includes('getTank().getFluid().getFluid()'));
    assert(read(javaRoot + 'machines/ElectricMachineTileEntity.java').includes('ItemStack[] getInventory()'));
    assert(!tracked.filter(name => name.startsWith(javaRoot) && name.endsWith('.java'))
        .some(name => /invalidateCaps\(|reviveCaps\(|LazyOptional/.test(read(name))));
});
check('Native renderer format and inherited tracking settings', () => {
    const render = read(javaRoot + 'graphics/ElectricRenderTypes.java');
    assert(render.includes('DefaultVertexFormats.NEW_ENTITY'));
    assert(render.includes('NO_DIFFUSE_LIGHTING'));
    const entities = read(javaRoot + 'init/Entities.java');
    assert(entities.includes('.setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)'));
});

const report = { baseline, scope: 'Static source/resource contracts only; no compilation or Minecraft runtime', results };
const reportFlag = process.argv.indexOf('--report');
if (reportFlag !== -1) {
    const output = path.resolve(root, process.argv[reportFlag + 1]);
    assert(output.startsWith(root + path.sep), 'Report must remain inside this checkout');
    fs.mkdirSync(path.dirname(output), { recursive: true });
    fs.writeFileSync(output, JSON.stringify(report, null, 2) + '\n');
}
for (const result of results) console.log(`${result.passed ? 'PASS' : 'FAIL'} ${result.name}${result.error ? ': ' + result.error : ''}`);
process.exitCode = results.every(result => result.passed) ? 0 : 1;
