package zone.moddev.mc.electricadvantage.migration;

import cyano.poweradvantage.api.migration.LegacyMigrationApi;
import cyano.poweradvantage.api.simple.BlockSimplePowerMachine;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.machines.ElectricDrillBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricGeneratorBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricMachineBlock;
import zone.moddev.mc.electricadvantage.machines.LEDBlock;
import zone.moddev.mc.poweradvantage.blocks.BlockPowerSwitch;

/** Registers ElectricAdvantage's 1.10 and 1.12 identities with the shared migration hook. */
public final class ElectricLegacyMigrations {
    private static boolean registered;

    private ElectricLegacyMigrations() {}

    public static synchronized void register() {
        if (registered) return;

        LegacyMigrationApi.registerNamespace(ElectricAdvantage.MODID);
        for (String path : new String[] {
                "electric_conduit", "li_ore", "sulfur_ore", "electric_track",
                "laser_turret", "laser_turret_evil", "led_bar", "photovoltaic_generator",
                "hydroelectric_generator", "steam_powered_generator", "electric_furnace",
                "electric_battery_array", "electric_crusher", "electric_drill",
                "electric_fabricator", "growth_chamber", "growth_chamber_controller",
                "electric_oven", "electric_switch", "electric_pump", "electric_still",
                "plastic_refinery", "pump_pipe_electric" }) {
            LegacyMigrationApi.registerBlockStateCodec(id(path), ElectricLegacyMigrations::decodeState);
        }

        for (String path : new String[] {
                "electric_furnace", "electric_crusher", "electric_drill", "electric_fabricator",
                "growth_chamber", "growth_chamber_controller", "electric_oven", "laser_turret",
                "led", "hydroelectric_generator", "photovoltaic_generator",
                "steam_powered_electric_generator", "electric_battery_array", "electric_pump",
                "electric_still", "plastic_refinery" }) {
            registerTile(path);
        }
        LegacyMigrationApi.registerTileEntityAlias("electricadvantage.l_e_d", id("led").toString());
        LegacyMigrationApi.registerTileEntityAlias("electricadvantage:l_e_d", id("led").toString());
        LegacyMigrationApi.registerTileEntityAlias("minecraft:electricadvantage.l_e_d", id("led").toString());

        String hydroturbine = id("hydroturbine").toString();
        LegacyMigrationApi.registerEntityAlias(hydroturbine, hydroturbine);
        LegacyMigrationApi.registerEntityAlias("electricadvantage.hydroturbine", hydroturbine);
        LegacyMigrationApi.registerEntityAlias("minecraft:electricadvantage.hydroturbine", hydroturbine);
        LegacyMigrationApi.registerEntityAlias("minecraft:electricadvantage.Hydroturbine", hydroturbine);
        LegacyMigrationApi.registerEntityAlias("electricadvantage.electricadvantage.Hydroturbine", hydroturbine);
        LegacyMigrationApi.registerEntityAlias(
                "minecraft:electricadvantage.electricadvantage.hydroturbine", hydroturbine);
        registered = true;
    }

    private static void registerTile(String path) {
        String current = id(path).toString();
        LegacyMigrationApi.registerTileEntityAlias(current, current);
        LegacyMigrationApi.registerTileEntityAlias(ElectricAdvantage.MODID + "." + path, current);
        LegacyMigrationApi.registerTileEntityAlias(
                "minecraft:" + ElectricAdvantage.MODID + "." + path, current);
    }

    private static IBlockState decodeState(Block block, int metadata) {
        if (block instanceof ElectricDrillBlock) {
            return block.getDefaultState()
                    .with(ElectricDrillBlock.FACING, legacyDrillFacing(metadata))
                    .with(ElectricDrillBlock.ACTIVE, legacyDrillActive(metadata));
        }
        if (block instanceof LEDBlock) {
            return ((LEDBlock) block).getStateFromLegacyMeta(metadata);
        }
        if (block instanceof ElectricGeneratorBlock) {
            return ((ElectricGeneratorBlock) block).getStateFromLegacyMeta(metadata);
        }
        if (block instanceof ElectricMachineBlock) {
            return ((ElectricMachineBlock) block).getStateFromLegacyMeta(metadata);
        }
        if (block instanceof BlockPowerSwitch) {
            return ((BlockPowerSwitch) block).getStateFromMeta(metadata);
        }
        if (block instanceof BlockSimplePowerMachine) {
            return ((BlockSimplePowerMachine) block).getStateFromMeta(metadata);
        }
        return block.getDefaultState();
    }

    static EnumFacing legacyDrillFacing(int metadata) {
        return EnumFacing.byIndex(metadata & 0x7);
    }

    static boolean legacyDrillActive(int metadata) {
        return (metadata & 0x8) != 0;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ElectricAdvantage.MODID, path);
    }
}
