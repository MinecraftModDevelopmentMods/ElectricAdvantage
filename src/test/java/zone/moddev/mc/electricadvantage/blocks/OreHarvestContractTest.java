package zone.moddev.mc.electricadvantage.blocks;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.world.EmptyBlockReader;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;
import static org.junit.Assert.*;

public class OreHarvestContractTest {
    @BeforeClass public static void bootstrapNativeRegistries() { Bootstrap.bootStrap(); }

    @Test public void lithiumRetainsStonePickaxeMinimumThroughNativeHarvestChecks() throws Exception {
        BlockState lithium = new Ore(1).defaultBlockState();
        assertTrue(lithium.requiresCorrectToolForDrops());
        assertSame(ToolType.PICKAXE, lithium.getHarvestTool());
        assertEquals(1, lithium.getHarvestLevel());
        assertFalse(canHarvest(lithium, ItemStack.EMPTY));
        assertFalse(canHarvest(lithium, new ItemStack(Items.WOODEN_PICKAXE)));
        assertFalse(canHarvest(lithium, new ItemStack(Items.STONE_AXE)));
        assertTrue(canHarvest(lithium, new ItemStack(Items.STONE_PICKAXE)));
        assertTrue(canHarvest(lithium, new ItemStack(Items.IRON_PICKAXE)));
    }

    @Test public void sulfurRetainsWoodenPickaxeMinimumThroughNativeHarvestChecks() throws Exception {
        BlockState sulfur = new Ore(new ItemStack(Items.GUNPOWDER), 0, 3).defaultBlockState();
        assertTrue(sulfur.requiresCorrectToolForDrops());
        assertSame(ToolType.PICKAXE, sulfur.getHarvestTool());
        assertEquals(0, sulfur.getHarvestLevel());
        assertFalse(canHarvest(sulfur, ItemStack.EMPTY));
        assertFalse(canHarvest(sulfur, new ItemStack(Items.WOODEN_AXE)));
        assertTrue(canHarvest(sulfur, new ItemStack(Items.WOODEN_PICKAXE)));
        assertTrue(canHarvest(sulfur, new ItemStack(Items.STONE_PICKAXE)));
    }

    private static boolean canHarvest(BlockState state, ItemStack held) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        HarvestPlayer player = (HarvestPlayer) ((Unsafe) field.get(null)).allocateInstance(HarvestPlayer.class);
        player.held = held;
        return ForgeHooks.canHarvestBlock(state, player, EmptyBlockReader.INSTANCE, BlockPos.ZERO);
    }

    // Only omit world construction. Harvest eligibility still executes Forge's native hook.
    private static final class HarvestPlayer extends PlayerEntity {
        private ItemStack held;
        private HarvestPlayer() { super(null, BlockPos.ZERO, 0, (GameProfile) null); }
        @Override public ItemStack getMainHandItem() { return held; }
        @Override public boolean hasCorrectToolForDrops(BlockState state) {
            return !state.requiresCorrectToolForDrops() || held.isCorrectToolForDrops(state);
        }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
}
