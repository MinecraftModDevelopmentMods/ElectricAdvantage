package zone.moddev.mc.electricadvantage.init;

import java.util.List;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillagerTradesEvent;

/** Adds the historical librarian offers through the native profession trade event. */
public final class Villages {
    private static boolean initialized;

    private Villages() {}

    public static synchronized void init() {
        if (initialized) return;
        MinecraftForge.EVENT_BUS.addListener(Villages::addTrades);
        initialized = true;
    }

    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LIBRARIAN) return;
        List<VillagerTrades.ITrade> novice = event.getTrades().get(1);
        List<VillagerTrades.ITrade> apprentice = event.getTrades().get(2);
        novice.add(sell(Blocks.electric_conduit.asItem(), -6, -3));
        novice.add(sell(Items.solder, -10, -5));
        novice.add(sell(Items.integrated_circuit, -6, -3));
        novice.add(sell(Items.blank_circuit_board, -6, -3));
        apprentice.add(sell(Items.petrolplastic_ingot, -8, -4));
        apprentice.add(sell(Items.power_supply_unit, 1, 3));
    }

    static VillagerTrades.ITrade sell(Item item, int minimumPrice, int maximumPrice) {
        return (villager, random) -> {
            int price = minimumPrice + random.nextInt(maximumPrice - minimumPrice + 1);
            int emeralds = price < 0 ? 1 : price;
            int count = price < 0 ? -price : 1;
            return new MerchantOffer(new ItemStack(net.minecraft.item.Items.EMERALD, emeralds),
                    new ItemStack(item, count), 7, 1, 0.0F);
        };
    }
}
