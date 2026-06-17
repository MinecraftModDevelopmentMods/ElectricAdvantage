package com.mcmoddev.electricadvantage.init;

import com.mcmoddev.electricadvantage.ElectricAdvantage;
import com.mcmoddev.electricadvantage.entities.*;
import com.mcmoddev.electricadvantage.graphics.*;
import com.mcmoddev.electricadvantage.machines.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ElectricAdvantage.MODID)
public abstract class Entities {

	private static final int DATA_FIXER_VERSION = 2;
	private static final String HYDROTURBINE_ENTITY_ID = ElectricAdvantage.MODID + ":hydroturbine";
	private static final String LEGACY_HYDROTURBINE_ENTITY_ID = "minecraft:electricadvantage.Hydroturbine";
	private static final String LEGACY_DOUBLE_MODID_HYDROTURBINE_ENTITY_ID = "electricadvantage.electricadvantage.Hydroturbine";
	private static final String LEGACY_AUTO_DOMAIN_HYDROTURBINE_ENTITY_ID = "minecraft:electricadvantage.electricadvantage.hydroturbine";

	private static boolean initDone = false;
	private static boolean dataFixersRegistered = false;

	public static void registerDataFixers(){
		if(dataFixersRegistered) return;
		ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(ElectricAdvantage.MODID, DATA_FIXER_VERSION);
		fixes.registerFix(FixTypes.ENTITY, new LegacyHydroturbineEntityFix());
		fixes.registerFix(FixTypes.BLOCK_ENTITY, new LegacyTileEntityIdFix(ElectricAdvantage.MODID, DATA_FIXER_VERSION));
		dataFixersRegistered = true;
	}

	public static void init(){
		if(initDone) return;
		
		Blocks.init();

		registerTileEntity(ElectricFurnaceTileEntity.class);
		registerTileEntity(ElectricCrusherTileEntity.class);
		registerTileEntity(ElectricDrillTileEntity.class);
		registerTileEntity(ElectricFabricatorTileEntity.class);
		registerTileEntity(GrowthChamberTileEntity.class);
		registerTileEntity(GrowthChamberControllerTileEntity.class);
		registerTileEntity(ElectricOvenTileEntity.class);
		registerTileEntity(LaserTurretTileEntity.class);
		registerTileEntity(LEDTileEntity.class);

		registerTileEntity(HydroelectricGeneratorTileEntity.class);
		registerTileEntity(PhotovoltaicGeneratorTileEntity.class);
		registerTileEntity(SteamPoweredElectricGeneratorTileEntity.class);
		registerTileEntity(ElectricBatteryArrayTileEntity.class);
		

		registerTileEntity(ElectricPumpTileEntity.class);
		registerTileEntity(ElectricStillTileEntity.class);
		registerTileEntity(PlasticRefineryTileEntity.class);
		
		registerEntity(HydroturbineEntity.class);
		
		initDone = true;
	}
	
	

	private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass){
		String name = tileEntityClass.getSimpleName();
		if(name.endsWith("TileEntity")){
			name = name.substring(0, name.lastIndexOf("TileEntity"));
		}
		GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(ElectricAdvantage.MODID, toUnderscoreStyle(name)));
	}

	private static int entityIndex = 0;
	private static void registerEntity(Class<? extends Entity> entityClass) {
		String name=ElectricAdvantage.MODID+"."+entityClass.getSimpleName();
		if(name.endsWith("Entity")){
			name = name.substring(0, name.lastIndexOf("Entity"));
		} else if(name.startsWith("Entity")){
			name = name.substring("Entity".length(),name.length());
		}
		ResourceLocation registryName = new ResourceLocation(ElectricAdvantage.MODID, name.substring(ElectricAdvantage.MODID.length() + 1).toLowerCase());
		EntityRegistry.registerModEntity(registryName, entityClass, registryName.toString(), entityIndex++, ElectricAdvantage.INSTANCE, 64, 1, true);
	}

	@SubscribeEvent
	public static void remapMissingEntities(RegistryEvent.MissingMappings<EntityEntry> event) {
		EntityEntry hydroturbine = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(ElectricAdvantage.MODID, "hydroturbine"));
		if(hydroturbine == null) return;
		for(RegistryEvent.MissingMappings.Mapping<EntityEntry> mapping : event.getAllMappings()){
			if(isLegacyHydroturbineId(mapping.key.toString())){
				mapping.remap(hydroturbine);
			}
		}
	}

	private static boolean isLegacyHydroturbineId(String id){
		if(id == null) return false;
		return LEGACY_HYDROTURBINE_ENTITY_ID.equalsIgnoreCase(id)
				|| LEGACY_DOUBLE_MODID_HYDROTURBINE_ENTITY_ID.equalsIgnoreCase(id)
				|| LEGACY_AUTO_DOMAIN_HYDROTURBINE_ENTITY_ID.equalsIgnoreCase(id);
	}

	private static final class LegacyHydroturbineEntityFix implements IFixableData {
		@Override
		public int getFixVersion() {
			return DATA_FIXER_VERSION;
		}

		@Override
		public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
			if(compound.hasKey("id", 8) && isLegacyHydroturbineId(compound.getString("id"))){
				compound.setString("id", HYDROTURBINE_ENTITY_ID);
			}
			return compound;
		}
	}

	private static final class LegacyTileEntityIdFix implements IFixableData {
		private final String modid;
		private final int version;

		private LegacyTileEntityIdFix(String modid, int version) {
			this.modid = modid;
			this.version = version;
		}

		@Override
		public int getFixVersion() {
			return version;
		}

		@Override
		public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
			if (compound.hasKey("id", 8)) {
				String id = compound.getString("id");
				String legacyPrefix = modid + ".";
				String legacyMinecraftPrefix = "minecraft:" + legacyPrefix;
				if (id.startsWith(legacyMinecraftPrefix)) {
					compound.setString("id", modid + ":" + id.substring(legacyMinecraftPrefix.length()));
				} else if (id.startsWith(legacyPrefix)) {
					compound.setString("id", modid + ":" + id.substring(legacyPrefix.length()));
				}
			}
			return compound;
		}
	}
	
	private static String toUnderscoreStyle(String camelCase){
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(camelCase.charAt(0)));
		for(int i = 1; i < camelCase.length(); i++){
			char c = camelCase.charAt(i);
			if(Character.isUpperCase(c)){
				sb.append('_').append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerRenderers(){
		RenderManager rm = Minecraft.getMinecraft().getRenderManager();
		
		ClientRegistry.bindTileEntitySpecialRenderer(LaserTurretTileEntity.class, new com.mcmoddev.electricadvantage.graphics.LaserTurretRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ElectricDrillTileEntity.class, new com.mcmoddev.electricadvantage.graphics.LaserDrillRenderer());
		
		RenderingRegistry.registerEntityRenderingHandler(HydroturbineEntity.class,new HydroturbineRenderer(rm));
	}
}
