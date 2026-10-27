package zone.moddev.mc.electricadvantage.init;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.electricadvantage.ElectricAdvantage;
import zone.moddev.mc.electricadvantage.items.BatteryItem;
import zone.moddev.mc.poweradvantage.init.ItemGroups;

/** Stable item registry for ElectricAdvantage 1.13. */
@Mod.EventBusSubscriber(modid = ElectricAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Items {
    public static final Map<String, Item> allItems = new LinkedHashMap<>();
    public static Item blank_circuit_board;
    public static Item control_circuit;
    public static Item integrated_circuit;
    public static Item power_supply_unit;
    public static Item silicon_ingot;
    public static Item silicon_blend;
    public static Item solder_blend;
    public static Item solder;
    public static Item lead_acid_battery;
    public static Item nickel_hydride_battery;
    public static Item alkaline_battery;
    public static Item lithium_battery;
    public static Item lithium_ingot;
    public static Item lithium_powder;
    public static Item lithium_nugget;
    public static Item sulfur_powder;
    public static Item petrolplastic_ingot;
    private static boolean initialized;

    private Items() {}
    public static void init() { initWithoutBlocks(); Blocks.init(); }

    static synchronized void initWithoutBlocks() {
        if (initialized) return;
        ItemGroups.init();
        blank_circuit_board = add("blank_circuit_board", new Item(properties()));
        control_circuit = add("control_circuit", new Item(properties()));
        integrated_circuit = add("integrated_circuit", new Item(properties()));
        power_supply_unit = add("psu", new Item(properties()));
        silicon_ingot = add("silicon_ingot", new Item(properties()));
        silicon_blend = add("silicon_mix", new Item(properties()));
        solder_blend = add("solder_mix", new Item(properties()));
        solder = add("solder", new Item(properties()));
        lithium_ingot = add("li_ingot", new Item(properties()));
        lithium_powder = add("li_powder", new Item(properties()));
        lithium_nugget = add("li_nugget", new Item(properties()));
        sulfur_powder = add("sulfur_powder", new Item(properties()));
        petrolplastic_ingot = add("petrolplastic_ingot", new Item(properties()));
        lead_acid_battery = add("lead_acid_battery", new BatteryItem(Power.LEAD_ACID_BATTERY_CAPACITY, properties()));
        nickel_hydride_battery = add("nickel_hydride_battery", new BatteryItem(Power.NICKEL_HYDRIDE_BATTERY_CAPACITY, properties()));
        alkaline_battery = add("alkaline_battery", new BatteryItem(Power.ALKALINE_BATTERY_CAPACITY, properties()));
        lithium_battery = add("lithium_battery", new BatteryItem(Power.LITHIUM_BATTERY_CAPACITY, properties()));
        initialized = true;
    }

    private static Item.Properties properties() { return new Item.Properties().tab(ItemGroups.tab_powerAdvantage); }
    private static Item add(String id, Item item) {
        item.setRegistryName(ElectricAdvantage.MODID, id);
        allItems.put(id, item);
        return item;
    }

    @SubscribeEvent public static void register(RegistryEvent.Register<Item> event) {
        initWithoutBlocks();
        event.getRegistry().registerAll(allItems.values().toArray(new Item[0]));
    }

    public static Item getItemByName(String id) { return allItems.get(id); }
    public static Set<Map.Entry<String, Item>> getAllRegisteredItems() { return allItems.entrySet(); }
}
