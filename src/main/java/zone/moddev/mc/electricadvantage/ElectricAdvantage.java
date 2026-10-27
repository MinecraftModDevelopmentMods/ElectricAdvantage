package zone.moddev.mc.electricadvantage;

import java.util.HashSet;
import java.util.Set;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import cyano.poweradvantage.api.migration.LegacyConfigMigration;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.Mapping;
import cyano.poweradvantage.api.migration.LegacyConfigMigration.ValueType;
import cyano.poweradvantage.api.migration.LegacyOreSpawnConfigBridge;
import zone.moddev.mc.electricadvantage.config.ElectricAdvantageConfig;
import zone.moddev.mc.electricadvantage.init.Blocks;
import zone.moddev.mc.electricadvantage.init.Enchantments;
import zone.moddev.mc.electricadvantage.init.Entities;
import zone.moddev.mc.electricadvantage.init.GUI;
import zone.moddev.mc.electricadvantage.init.Items;
import zone.moddev.mc.electricadvantage.init.OreSpawnWorldGen;
import zone.moddev.mc.electricadvantage.init.Recipes;
import zone.moddev.mc.electricadvantage.init.TreasureChests;
import zone.moddev.mc.electricadvantage.init.Villages;
import zone.moddev.mc.electricadvantage.recipe.RecipeConditions;
import zone.moddev.mc.electricadvantage.migration.ElectricLegacyMigrations;

/** Forge 1.16 entry point for Electric Advantage. */
@Mod(ElectricAdvantage.MODID)
public final class ElectricAdvantage {
    public static final String MODID = "electricadvantage";
    public static final String NAME = "Electric Advantage";
    public static final String VERSION = "2.2.8.116051";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ElectricAdvantage INSTANCE;

    public final Set<String> PLASTIC_FLUID_MATERIALS = new HashSet<>();
    public String LASER_SOUND = "minecraft:block.note_block.bass";

    public ElectricAdvantage() {
        INSTANCE = this;
        Blocks.init();
        Items.init();
        Entities.init();
        Enchantments.init();
        GUI.init();
        ElectricLegacyMigrations.register();

        migrateLegacyOreSpawnConfiguration();
        migrateLegacyConfiguration();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ElectricAdvantageConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ElectricAdvantageConfig::onConfigLoading);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueInterModCommunication);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                FMLJavaModLoadingContext.get().getModEventBus().addListener(
                        zone.moddev.mc.electricadvantage.graphics.ClientRegistration::onClientSetup));
    }

    private static void migrateLegacyOreSpawnConfiguration() {
        try {
            LegacyOreSpawnConfigBridge.Result result =
                    LegacyOreSpawnConfigBridge.migrate(FMLPaths.CONFIGDIR.get(), MODID);
            if (result.wasMigrated()) {
                LOGGER.info("Imported {} legacy Electric Advantage OreSpawn rule(s) from '{}' into '{}'",
                        result.getRuleCount(), result.getSource(), result.getTarget());
            }
        } catch (IOException exception) {
            LOGGER.error("Could not migrate legacy Electric Advantage OreSpawn configuration", exception);
        }
    }

    private static void migrateLegacyConfiguration() {
        try {
            LegacyConfigMigration.Result result = LegacyConfigMigration.migrate(FMLPaths.CONFIGDIR.get(), MODID,
                    Mapping.of("fabricator", "recursion_limit", "fabricator", "recursionLimit", ValueType.INTEGER),
                    Mapping.of("options", "laser_sound", "options", "laserSound", ValueType.STRING),
                    Mapping.of("options", "petrolplastic_fluids", "options", "petrolplasticFluids", ValueType.STRING));
            if (result.wasMigrated()) {
                LOGGER.info("Imported {} legacy Electric Advantage settings; report: {}",
                        result.getImportedValues(), result.getReportFile());
            }
        } catch (IOException exception) {
            LOGGER.error("Could not migrate legacy Electric Advantage configuration", exception);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // CraftingHelper's shared condition map is not safe during parallel loading.
        event.enqueueWork(RecipeConditions::register);
        ElectricAdvantageConfig.apply();
        Recipes.init();
        TreasureChests.init(FMLPaths.CONFIGDIR.get());
        Villages.init();
        LOGGER.info("Electric Advantage {} common setup complete", VERSION);
    }

    private void enqueueInterModCommunication(InterModEnqueueEvent event) {
        OreSpawnWorldGen.register(FMLPaths.CONFIGDIR.get());
    }
}
