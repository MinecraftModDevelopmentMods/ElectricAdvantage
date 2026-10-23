package zone.moddev.mc.electricadvantage.init;

import java.util.function.Supplier;

import net.minecraft.entity.EntityType;
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
        ELECTRIC_FURNACE = tile(event, "electric_furnace", ElectricFurnaceTileEntity::new);
        ELECTRIC_CRUSHER = tile(event, "electric_crusher", ElectricCrusherTileEntity::new);
        ELECTRIC_DRILL = tile(event, "electric_drill", ElectricDrillTileEntity::new);
        ELECTRIC_FABRICATOR = tile(event, "electric_fabricator", ElectricFabricatorTileEntity::new);
        GROWTH_CHAMBER = tile(event, "growth_chamber", GrowthChamberTileEntity::new);
        GROWTH_CHAMBER_CONTROLLER = tile(event, "growth_chamber_controller", GrowthChamberControllerTileEntity::new);
        ELECTRIC_OVEN = tile(event, "electric_oven", ElectricOvenTileEntity::new);
        LASER_TURRET = tile(event, "laser_turret", LaserTurretTileEntity::new);
        LED = tile(event, "led", LEDTileEntity::new);
        HYDROELECTRIC_GENERATOR = tile(event, "hydroelectric_generator", HydroelectricGeneratorTileEntity::new);
        PHOTOVOLTAIC_GENERATOR = tile(event, "photovoltaic_generator", PhotovoltaicGeneratorTileEntity::new);
        STEAM_POWERED_ELECTRIC_GENERATOR = tile(event, "steam_powered_electric_generator", SteamPoweredElectricGeneratorTileEntity::new);
        ELECTRIC_BATTERY_ARRAY = tile(event, "electric_battery_array", ElectricBatteryArrayTileEntity::new);
        ELECTRIC_PUMP = tile(event, "electric_pump", ElectricPumpTileEntity::new);
        ELECTRIC_STILL = tile(event, "electric_still", ElectricStillTileEntity::new);
        PLASTIC_REFINERY = tile(event, "plastic_refinery", PlasticRefineryTileEntity::new);
    }

    @SubscribeEvent public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        HYDROTURBINE = EntityType.Builder.create(HydroturbineEntity.class,
                world -> new HydroturbineEntity(HYDROTURBINE, world))
                .tracker(64, 1, true).build(ElectricAdvantage.MODID + ":hydroturbine");
        HYDROTURBINE.setRegistryName(ElectricAdvantage.MODID, "hydroturbine");
        event.getRegistry().register(HYDROTURBINE);
    }

    private static <T extends TileEntity> TileEntityType<T> tile(
            RegistryEvent.Register<TileEntityType<?>> event, String id, Supplier<? extends T> factory) {
        TileEntityType<T> type = TileEntityType.Builder.<T>create(factory).build(null);
        type.setRegistryName(ElectricAdvantage.MODID, id);
        event.getRegistry().register(type);
        return type;
    }
}
