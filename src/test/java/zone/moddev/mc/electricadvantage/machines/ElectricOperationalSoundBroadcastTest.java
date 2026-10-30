package zone.moddev.mc.electricadvantage.machines;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/** Real broadcast methods with an explicit player list and forbidden chunk-backed entity queries. */
public class ElectricOperationalSoundBroadcastTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void machineBroadcastUsesOnlyExistingIntersectingConnectedPlayers() throws Exception {
        QueryRejectingWorld testWorld = fixture();
        ElectricOvenTileEntity machine = new ElectricOvenTileEntity() {
            @Override public World getWorld() { return testWorld; }
        };
        machine.playSoundEffect(100, 80, -20, SoundEvents.BLOCK_ANVIL_LAND, 0.25f, 0.75f);
        assertRecipients(testWorld);
    }

    @Test public void generatorBroadcastUsesOnlyExistingIntersectingConnectedPlayers() throws Exception {
        QueryRejectingWorld testWorld = fixture();
        PhotovoltaicGeneratorTileEntity generator = new PhotovoltaicGeneratorTileEntity() {
            @Override public World getWorld() { return testWorld; }
        };
        generator.playSoundEffect(100, 80, -20, SoundEvents.BLOCK_ANVIL_LAND, 0.25f, 0.75f);
        assertRecipients(testWorld);
    }

    @Test public void emptyPlayerListNeverQueriesChunks() throws Exception {
        QueryRejectingWorld testWorld = withoutConstructor(QueryRejectingWorld.class);
        testWorld.existingPlayers = Collections.emptyList();
        ElectricOvenTileEntity machine = new ElectricOvenTileEntity() {
            @Override public World getWorld() { return testWorld; }
        };
        machine.playSoundEffect(50000, 82, 50000, SoundEvents.BLOCK_ANVIL_LAND, 1, 1);
        assertEquals(1, testWorld.playerListReads);
    }

    private static QueryRejectingWorld fixture() throws Exception {
        QueryRejectingWorld world = withoutConstructor(QueryRejectingWorld.class);
        world.existingPlayers = new ArrayList<>();
        world.existingPlayers.add(player(new AxisAlignedBB(99.7, 80, -20.3, 100.3, 81.8, -19.7), false, true));
        // Player center can be outside 16 blocks while its box still intersects the original cube.
        world.existingPlayers.add(player(new AxisAlignedBB(115.9, 80, -20.3, 116.5, 81.8, -19.7), false, true));
        // Cube corner is outside a spherical range but was included by the original AABB query.
        world.existingPlayers.add(player(new AxisAlignedBB(115, 80, -5, 115.6, 81.8, -4.4), false, true));
        world.existingPlayers.add(player(new AxisAlignedBB(116, 80, -20.3, 116.6, 81.8, -19.7), false, true));
        world.existingPlayers.add(player(new AxisAlignedBB(99.7, 96, -20.3, 100.3, 97.8, -19.7), false, true));
        world.existingPlayers.add(player(new AxisAlignedBB(99.7, 62.2, -20.3, 100.3, 64, -19.7), false, true));
        world.existingPlayers.add(player(new AxisAlignedBB(99.7, 80, -20.3, 100.3, 81.8, -19.7), true, true));
        world.existingPlayers.add(player(new AxisAlignedBB(99.7, 80, -20.3, 100.3, 81.8, -19.7), false, false));
        return world;
    }

    private static StubPlayer player(AxisAlignedBB bounds, boolean spectator, boolean connected) throws Exception {
        StubPlayer player = withoutConstructor(StubPlayer.class);
        player.setBoundingBox(bounds);
        player.spectator = spectator;
        if (connected) player.connection = withoutConstructor(RecordingConnection.class);
        return player;
    }

    private static void assertRecipients(QueryRejectingWorld world) {
        assertEquals("Broadcast reads the existing player list once", 1, world.playerListReads);
        for (int i = 0; i < world.existingPlayers.size(); i++) {
            ServerPlayerEntity player = world.existingPlayers.get(i);
            if (player.connection == null) continue;
            RecordingConnection connection = (RecordingConnection)player.connection;
            assertEquals("Player " + i, i < 3 ? 1 : 0, connection.sent);
            if (i < 3) assertTrue(connection.last instanceof SPlaySoundPacket);
        }
    }

    private static <T> T withoutConstructor(Class<T> type) throws Exception {
        Class<?> allocator = Class.forName("sun.misc.Unsafe");
        Field field = allocator.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }

    private static final class QueryRejectingWorld extends ServerWorld {
        List<ServerPlayerEntity> existingPlayers;
        int playerListReads;

        private QueryRejectingWorld() {
            super(null, null, null, null, null, null, null);
        }

        @Override public List<ServerPlayerEntity> getPlayers() {
            playerListReads++;
            return existingPlayers;
        }

        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> type,
                AxisAlignedBB bounds, Predicate<? super T> predicate) {
            throw new AssertionError("Sound broadcast must not use chunk-backed entity queries");
        }
    }

    private static final class StubPlayer extends ServerPlayerEntity {
        boolean spectator;
        private StubPlayer() { super(null, null, null, null); }
        @Override public boolean isSpectator() { return spectator; }
    }

    private static final class RecordingConnection extends ServerPlayNetHandler {
        int sent;
        IPacket<?> last;
        private RecordingConnection() { super(null, null, null); }
        @Override public void sendPacket(IPacket<?> packet) { sent++; last = packet; }
    }
}
