package zone.moddev.mc.electricadvantage.init;

import java.util.LinkedHashMap;
import java.util.Map;

import cyano.poweradvantage.api.GUIBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.blocks.ElectricConduitBlock;
import zone.moddev.mc.electricadvantage.blocks.ElectricScaffoldBlock;
import zone.moddev.mc.electricadvantage.blocks.Ore;
import zone.moddev.mc.electricadvantage.blocks.PumpPipeBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricBatteryArrayBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricCrusherBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricDrillBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricFabricatorBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricFurnaceBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricOvenBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricPumpBlock;
import zone.moddev.mc.electricadvantage.machines.ElectricStillBlock;
import zone.moddev.mc.electricadvantage.machines.GrowthChamberBlock;
import zone.moddev.mc.electricadvantage.machines.GrowthChamberControllerBlock;
import zone.moddev.mc.electricadvantage.machines.HydroelectricGeneratorBlock;
import zone.moddev.mc.electricadvantage.machines.LEDBlock;
import zone.moddev.mc.electricadvantage.machines.LaserTurretBlock;
import zone.moddev.mc.electricadvantage.machines.PhotovoltaicGeneratorBlock;
import zone.moddev.mc.electricadvantage.machines.PlasticRefineryBlock;
import zone.moddev.mc.electricadvantage.machines.SteamPoweredElectricGeneratorBlock;
import zone.moddev.mc.poweradvantage.blocks.BlockPowerSwitch;
import zone.moddev.mc.poweradvantage.init.ItemGroups;

/** Stable block registry for ElectricAdvantage 1.13. */
@Mod.EventBusSubscriber(modid = ElectricAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Blocks {
    private static final Map<String, Block> ALL_BLOCKS = new LinkedHashMap<>();

    public static Block electric_conduit;
    public static Block lithium_ore;
    public static Block sulfur_ore;
    public static Block electric_track;
    public static Block led_bar;
    public static GUIBlock steam_powered_generator;
    public static GUIBlock photovoltaic_generator;
    public static GUIBlock hydroelectric_generator;
    public static GUIBlock arc_furnace;
    public static GUIBlock battery_array;
    public static GUIBlock rock_crusher;
    public static GUIBlock laser_drill;
    public static GUIBlock laser_turret;
    public static GUIBlock laser_turret_evil;
    public static GUIBlock fabricator;
    public static GUIBlock growth_chamber;
    public static GUIBlock growth_chamber_controller;
    public static GUIBlock oven;
    public static Block pump_pipe_electric;
    public static Block electric_switch;
    public static GUIBlock electric_still;
    public static GUIBlock electric_pump;
    public static GUIBlock plastic_refinery;

    private static boolean initialized;
    private Blocks() {}

    public static synchronized void init() {
        if (initialized) return;
        Items.initWithoutBlocks();
        ItemGroups.init();
        electric_conduit = add(new ElectricConduitBlock(), "electric_conduit");
        lithium_ore = add(new Ore(1), "li_ore");
        sulfur_ore = add(new Ore(new ItemStack(Items.sulfur_powder, 1), 0, 3), "sulfur_ore");
        electric_track = add(new ElectricScaffoldBlock(), "electric_track");
        laser_turret = (GUIBlock) add(new LaserTurretBlock(false), "laser_turret");
        laser_turret_evil = (GUIBlock) add(new LaserTurretBlock(true), "laser_turret_evil");
        led_bar = add(new LEDBlock(), "led_bar");
        photovoltaic_generator = (GUIBlock) add(new PhotovoltaicGeneratorBlock(), "photovoltaic_generator");
        hydroelectric_generator = (GUIBlock) add(new HydroelectricGeneratorBlock(), "hydroelectric_generator");
        steam_powered_generator = (GUIBlock) add(new SteamPoweredElectricGeneratorBlock(), "steam_powered_generator");
        arc_furnace = (GUIBlock) add(new ElectricFurnaceBlock(), "electric_furnace");
        battery_array = (GUIBlock) add(new ElectricBatteryArrayBlock(), "electric_battery_array");
        rock_crusher = (GUIBlock) add(new ElectricCrusherBlock(), "electric_crusher");
        laser_drill = (GUIBlock) add(new ElectricDrillBlock(), "electric_drill");
        fabricator = (GUIBlock) add(new ElectricFabricatorBlock(), "electric_fabricator");
        growth_chamber = (GUIBlock) add(new GrowthChamberBlock(), "growth_chamber");
        growth_chamber_controller = (GUIBlock) add(new GrowthChamberControllerBlock(), "growth_chamber_controller");
        oven = (GUIBlock) add(new ElectricOvenBlock(), "electric_oven");
        electric_switch = add(new BlockPowerSwitch(Power.ELECTRIC_POWER), "electric_switch");
        electric_pump = (GUIBlock) add(new ElectricPumpBlock(), "electric_pump");
        electric_still = (GUIBlock) add(new ElectricStillBlock(), "electric_still");
        plastic_refinery = (GUIBlock) add(new PlasticRefineryBlock(), "plastic_refinery");
        pump_pipe_electric = add(new PumpPipeBlock(), "pump_pipe_electric");
        initialized = true;
    }

    private static Block add(Block block, String id) {
        block.setRegistryName(ElectricAdvantage.MODID, id);
        ALL_BLOCKS.put(id, block);
        return block;
    }

    @SubscribeEvent public static void registerBlocks(RegistryEvent.Register<Block> event) {
        init();
        event.getRegistry().registerAll(ALL_BLOCKS.values().toArray(new Block[0]));
    }

    @SubscribeEvent public static void registerBlockItems(RegistryEvent.Register<Item> event) {
        init();
        for (Map.Entry<String, Block> entry : ALL_BLOCKS.entrySet()) {
            Item.Properties properties = new Item.Properties();
            if (!"pump_pipe_electric".equals(entry.getKey()) && !"laser_turret_evil".equals(entry.getKey())) {
                properties.group(ItemGroups.tab_powerAdvantage);
            }
            BlockItem item = new BlockItem(entry.getValue(), properties);
            item.setRegistryName(ElectricAdvantage.MODID, entry.getKey());
            event.getRegistry().register(item);
        }
    }
}
