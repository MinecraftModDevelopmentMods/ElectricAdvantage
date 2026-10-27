package zone.moddev.mc.electricadvantage.init;

import java.util.function.Supplier;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityClassification;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.entities.HydroturbineEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricBatteryArrayTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricCrusherTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricDrillTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricFabricatorTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricFurnaceTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricOvenTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricPumpTileEntity;
import zone.moddev.mc.electricadvantage.machines.ElectricStillTileEntity;
import zone.moddev.mc.electricadvantage.machines.GrowthChamberControllerTileEntity;
import zone.moddev.mc.electricadvantage.machines.GrowthChamberTileEntity;
import zone.moddev.mc.electricadvantage.machines.HydroelectricGeneratorTileEntity;
import zone.moddev.mc.electricadvantage.machines.LEDTileEntity;
import zone.moddev.mc.electricadvantage.machines.LaserTurretTileEntity;
import zone.moddev.mc.electricadvantage.machines.PhotovoltaicGeneratorTileEntity;
import zone.moddev.mc.electricadvantage.machines.PlasticRefineryTileEntity;
import zone.moddev.mc.electricadvantage.machines.SteamPoweredElectricGeneratorTileEntity;

/** Stable tile/entity registrations; IDs are unchanged from 1.12. */
@Mod.EventBusSubscriber(modid = ElectricAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Entities {
    public static TileEntityType<ElectricFurnaceTileEntity> ELECTRIC_FURNACE;
    public static TileEntityType<ElectricCrusherTileEntity> ELECTRIC_CRUSHER;
    public static TileEntityType<ElectricDrillTileEntity> ELECTRIC_DRILL;
    public static TileEntityType<ElectricFabricatorTileEntity> ELECTRIC_FABRICATOR;
    public static TileEntityType<GrowthChamberTileEntity> GROWTH_CHAMBER;
    public static TileEntityType<GrowthChamberControllerTileEntity> GROWTH_CHAMBER_CONTROLLER;
    public static TileEntityType<ElectricOvenTileEntity> ELECTRIC_OVEN;
    public static TileEntityType<LaserTurretTileEntity> LASER_TURRET;
    public static TileEntityType<LEDTileEntity> LED;
    public static TileEntityType<HydroelectricGeneratorTileEntity> HYDROELECTRIC_GENERATOR;
    public static TileEntityType<PhotovoltaicGeneratorTileEntity> PHOTOVOLTAIC_GENERATOR;
    public static TileEntityType<SteamPoweredElectricGeneratorTileEntity> STEAM_POWERED_ELECTRIC_GENERATOR;
    public static TileEntityType<ElectricBatteryArrayTileEntity> ELECTRIC_BATTERY_ARRAY;
    public static TileEntityType<ElectricPumpTileEntity> ELECTRIC_PUMP;
    public static TileEntityType<ElectricStillTileEntity> ELECTRIC_STILL;
    public static TileEntityType<PlasticRefineryTileEntity> PLASTIC_REFINERY;
    public static EntityType<HydroturbineEntity> HYDROTURBINE;

    private Entities() {}
    public static void init() {}

    @SubscribeEvent public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        ELECTRIC_FURNACE = tile(event, "electric_furnace", ElectricFurnaceTileEntity::new, Blocks.arc_furnace);
        ELECTRIC_CRUSHER = tile(event, "electric_crusher", ElectricCrusherTileEntity::new, Blocks.rock_crusher);
        ELECTRIC_DRILL = tile(event, "electric_drill", ElectricDrillTileEntity::new, Blocks.laser_drill);
        ELECTRIC_FABRICATOR = tile(event, "electric_fabricator", ElectricFabricatorTileEntity::new, Blocks.fabricator);
        GROWTH_CHAMBER = tile(event, "growth_chamber", GrowthChamberTileEntity::new, Blocks.growth_chamber);
        GROWTH_CHAMBER_CONTROLLER = tile(event, "growth_chamber_controller", GrowthChamberControllerTileEntity::new, Blocks.growth_chamber_controller);
        ELECTRIC_OVEN = tile(event, "electric_oven", ElectricOvenTileEntity::new, Blocks.oven);
        LASER_TURRET = tile(event, "laser_turret", LaserTurretTileEntity::new, Blocks.laser_turret, Blocks.laser_turret_evil);
        LED = tile(event, "led", LEDTileEntity::new, Blocks.led_bar);
        HYDROELECTRIC_GENERATOR = tile(event, "hydroelectric_generator", HydroelectricGeneratorTileEntity::new, Blocks.hydroelectric_generator);
        PHOTOVOLTAIC_GENERATOR = tile(event, "photovoltaic_generator", PhotovoltaicGeneratorTileEntity::new, Blocks.photovoltaic_generator);
        STEAM_POWERED_ELECTRIC_GENERATOR = tile(event, "steam_powered_electric_generator", SteamPoweredElectricGeneratorTileEntity::new, Blocks.steam_powered_generator);
        ELECTRIC_BATTERY_ARRAY = tile(event, "electric_battery_array", ElectricBatteryArrayTileEntity::new, Blocks.battery_array);
        ELECTRIC_PUMP = tile(event, "electric_pump", ElectricPumpTileEntity::new, Blocks.electric_pump);
        ELECTRIC_STILL = tile(event, "electric_still", ElectricStillTileEntity::new, Blocks.electric_still);
        PLASTIC_REFINERY = tile(event, "plastic_refinery", PlasticRefineryTileEntity::new, Blocks.plastic_refinery);
    }

    @SubscribeEvent public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        HYDROTURBINE = EntityType.Builder.<HydroturbineEntity>of(
                HydroturbineEntity::new, EntityClassification.MISC)
                .sized(0.9375F, 0.9375F)
                .setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true)
                .build(ElectricAdvantage.MODID + ":hydroturbine");
        HYDROTURBINE.setRegistryName(ElectricAdvantage.MODID, "hydroturbine");
        event.getRegistry().register(HYDROTURBINE);
    }

    private static <T extends TileEntity> TileEntityType<T> tile(
            RegistryEvent.Register<TileEntityType<?>> event, String id, Supplier<? extends T> factory,
            net.minecraft.block.Block... blocks) {
        TileEntityType<T> type = TileEntityType.Builder.<T>of(factory, blocks).build(null);
        type.setRegistryName(ElectricAdvantage.MODID, id);
        event.getRegistry().register(type);
        return type;
    }

}
