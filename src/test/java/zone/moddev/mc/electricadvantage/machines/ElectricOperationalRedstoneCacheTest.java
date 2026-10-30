package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.PoweredEntity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import zone.moddev.mc.electricadvantage.testsupport.Forge113TestBootstrap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/** Initial interlock sampling must not turn the ordinary cache into per-tick polling. */
public class ElectricOperationalRedstoneCacheTest {
    private static final BlockPos EVEN = new BlockPos(8, 80, 8);
    private static final BlockPos ODD = new BlockPos(9, 81, 9);

    @BeforeClass public static void bootstrap() { Forge113TestBootstrap.register(); }

    @Test public void signalledFirstUseDoesNotWaitForPowerUpdate() throws Exception {
        ProbeMachine machine = fixture(EVEN, true);
        assertTrue(machine.signal());
        assertEquals(1, machine.testWorld.queryTimes.size());
        machine.testWorld.signalled = false;
        assertTrue("Repeated reads must retain the sampled interlock", machine.signal());
        assertEquals(1, machine.testWorld.queryTimes.size());
    }

    @Test public void unsignalledFirstUseIsSampledAndCachedToo() throws Exception {
        ProbeMachine machine = fixture(EVEN, false);
        assertFalse(machine.signal());
        assertEquals(1, machine.testWorld.queryTimes.size());
        machine.testWorld.signalled = true;
        assertFalse("A cached false must not be mistaken for uninitialized", machine.signal());
        assertEquals(1, machine.testWorld.queryTimes.size());
    }

    @Test public void unattachedReadDoesNotInitializeTheWorldCache() throws Exception {
        ProbeMachine machine = new ProbeMachine();
        machine.setPos(EVEN);
        assertFalse(machine.signal());
        machine.testWorld = world(EVEN, true);
        assertTrue(machine.signal());
        assertEquals(1, machine.testWorld.queryTimes.size());
    }

    @Test public void clientReadsNeitherQueryRedstoneNorInitializeTheCache() throws Exception {
        ProbeMachine machine = fixture(EVEN, true);
        SignalWorld client = machine.testWorld;
        Field clientSide = World.class.getDeclaredField("isRemote");
        clientSide.setAccessible(true);
        clientSide.setBoolean(client, true);
        assertTrue(client.isRemote);
        assertFalse(machine.signal());
        client.signalled = false;
        assertFalse(machine.signal());
        assertTrue("Client reads must not query local redstone", client.queryTimes.isEmpty());
        machine.testWorld = world(EVEN, true);
        assertTrue("Client reads must not initialize the server cache", machine.signal());
        assertEquals(1, machine.testWorld.queryTimes.size());
    }

    @Test public void powerUpdateBeforeFirstUseAlreadyInitializesTheCache() throws Exception {
        ProbeMachine machine = fixture(EVEN, true);
        machine.powerUpdate();
        assertEquals(1, machine.testWorld.queryTimes.size());
        machine.testWorld.signalled = false;
        assertTrue(machine.signal());
        assertEquals("First use after powerUpdate must not resample", 1, machine.testWorld.queryTimes.size());
    }

    @Test public void realPoweredTickKeepsBothEdgesCachedUntilTheEightTickRefresh() throws Exception {
        for (BlockPos position : new BlockPos[]{EVEN, ODD}) {
            for (boolean initial : new boolean[]{false, true}) {
                ProbeMachine machine = fixture(position, initial);
                SignalWorld world = machine.testWorld;
                long refresh = position.equals(EVEN) ? 8 : 9;
                long first = refresh - 7;
                world.time = first;
                machine.tick();
                assertEquals(initial, machine.observed);
                assertEquals(1, world.queryTimes.size());
                world.signalled = !initial;
                for (long tick = first + 1; tick <= refresh + 8; tick++) {
                    world.time = tick;
                    machine.tick();
                    assertEquals("tickUpdate must precede the scheduled refresh at " + tick,
                            tick <= refresh ? initial : !initial, machine.observed);
                    assertEquals(tick < refresh ? 1 : tick < refresh + 8 ? 2 : 3, world.queryTimes.size());
                }
                assertEquals(Arrays.asList(first, refresh, refresh + 8), world.queryTimes);
                assertEquals(2, machine.powerUpdates);
            }
        }
    }

    private static ProbeMachine fixture(BlockPos position, boolean signalled) throws Exception {
        ProbeMachine machine = new ProbeMachine();
        machine.setPos(position);
        machine.testWorld = world(position, signalled);
        // Isolate conduit discovery, not PoweredEntity's real power-update cadence.
        Field delay = PoweredEntity.class.getDeclaredField("conduitRefreshDelay");
        delay.setAccessible(true);
        delay.setInt(machine, -1);
        return machine;
    }

    private static SignalWorld world(BlockPos position, boolean signalled) throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        SignalWorld world = (SignalWorld)allocator.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), SignalWorld.class);
        world.position = position;
        world.signalled = signalled;
        world.queryTimes = new ArrayList<>();
        return world;
    }

    private static final class ProbeMachine extends ElectricOvenTileEntity {
        SignalWorld testWorld;
        boolean observed;
        int powerUpdates;
        boolean signal() { return hasRedstoneSignal(); }
        @Override public World getWorld() { return testWorld; }
        @Override public void tickUpdate(boolean server) { observed = hasRedstoneSignal(); }
        @Override public void powerUpdate() {
            powerUpdates++;
            super.powerUpdate();
        }
        @Override protected void setPowerState(boolean powered) { }
    }

    @Test public void generatorSamplesSignalledFirstUseBeforeItsFirstPulse() throws Exception {
        ProbeGenerator generator = generatorFixture(true);
        assertTrue("A newly loaded generator must observe redstone before generating", generator.signal());
        assertEquals(1, generator.testWorld.queryTimes.size());
        generator.testWorld.signalled = false;
        assertTrue("First-use sampling must retain the normal cached value", generator.signal());
        assertEquals(1, generator.testWorld.queryTimes.size());
    }

    @Test public void generatorCachesAnUnsignalledFirstReadToo() throws Exception {
        ProbeGenerator generator = generatorFixture(false);
        assertFalse(generator.signal());
        assertEquals(1, generator.testWorld.queryTimes.size());
        generator.testWorld.signalled = true;
        assertFalse("A false interlock sample is initialized, not missing", generator.signal());
        assertEquals(1, generator.testWorld.queryTimes.size());
    }

    @Test public void unattachedGeneratorReadDoesNotInitializeItsCache() throws Exception {
        ProbeGenerator generator = new ProbeGenerator();
        generator.setPos(EVEN);
        assertFalse(generator.signal());
        generator.testWorld = world(EVEN, true);
        assertTrue(generator.signal());
        assertEquals(1, generator.testWorld.queryTimes.size());
    }

    @Test public void clientGeneratorReadCannotInitializeTheServerCache() throws Exception {
        ProbeGenerator generator = generatorFixture(true);
        Field clientSide = World.class.getDeclaredField("isRemote");
        clientSide.setAccessible(true);
        clientSide.setBoolean(generator.testWorld, true);
        assertFalse(generator.signal());
        assertTrue(generator.testWorld.queryTimes.isEmpty());
        generator.testWorld = world(EVEN, true);
        assertTrue("A client-only read must leave the server interlock unsampled", generator.signal());
        assertEquals(1, generator.testWorld.queryTimes.size());
    }

    @Test public void inhibitedSolarGeneratorCannotGenerateItsFirstPulse() throws Exception {
        ProbeGenerator generator = generatorFixture(true);
        generator.powerUpdate();
        assertEquals("Redstone is sampled before generation, then on the ordinary refresh", 2,
                generator.testWorld.queryTimes.size());
        assertEquals(0, generator.getEnergy(), 0);
        assertEquals(0, generator.getPowerOutput(), 0);
    }

    private static ProbeGenerator generatorFixture(boolean signalled) throws Exception {
        ProbeGenerator generator = new ProbeGenerator();
        generator.setPos(EVEN);
        generator.testWorld = world(EVEN, signalled);
        return generator;
    }

    private static final class ProbeGenerator extends PhotovoltaicGeneratorTileEntity {
        SignalWorld testWorld;
        boolean signal() { return hasRedstoneSignal(); }
        @Override public World getWorld() { return testWorld; }
    }

    private static final class SignalWorld extends WorldServer {
        @Override public boolean canSeeSky(BlockPos position) {
            throw new AssertionError("An inhibited panel must not enter light sampling");
        }
        BlockPos position;
        boolean signalled;
        long time;
        List<Long> queryTimes;
        private SignalWorld() {
            super(null, null, null, null, null, null);
        }
        @Override public boolean isBlockPowered(BlockPos position) {
            assertEquals(this.position, position);
            queryTimes.add(time);
            return signalled;
        }
        @Override public long getGameTime() { return time; }
    }
}
