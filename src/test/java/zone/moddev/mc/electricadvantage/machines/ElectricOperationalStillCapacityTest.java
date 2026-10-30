package zone.moddev.mc.electricadvantage.machines;

import net.minecraftforge.fluids.FluidStack;
import java.lang.reflect.Field;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.init.Bootstrap;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import zone.moddev.mc.electricadvantage.init.Power;
import zone.moddev.mc.poweradvantage.registry.still.recipe.DistillationRecipe;
import zone.moddev.mc.poweradvantage.registry.still.recipe.DistillationRecipeRegistry;
import zone.moddev.mc.poweradvantage.registry.still.recipe.ExplicitFluidReference;

import static org.junit.Assert.*;

/** Real tick conservation checks; the shared rig separately supplies a live server world. */
public class ElectricOperationalStillCapacityTest {
    private Field singleton;
    private Object previousRegistry;

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Before public void installIsolatedRecipe() throws Exception {
        singleton = DistillationRecipeRegistry.class.getDeclaredField("instance");
        singleton.setAccessible(true);
        previousRegistry = singleton.get(null);
        DistillationRecipeRegistry registry = new DistillationRecipeRegistry();
        registry.addRecipe(new DistillationRecipe(new ExplicitFluidReference(FluidRegistry.WATER), 2,
                new ExplicitFluidReference(FluidRegistry.LAVA), 3));
        singleton.set(null, registry);
    }

    @After public void restoreRecipeRegistry() throws Exception {
        singleton.set(null, previousRegistry);
    }

    @Test public void aFullTankCannotConsumeInput() throws Exception {
        ElectricStillTileEntity still = fixture(1000);
        for (int tick = 0; tick < 4; tick++) still.tickUpdate(true);
        assertEquals(4, still.getInputTank().getFluidAmount());
        assertEquals(1000, still.getOutputTank().getFluidAmount());
        assertEquals(64, still.getEnergy(Power.ELECTRIC_POWER), 0);
    }

    @Test public void aPartialOutputMustFitTheWholeRecipe() throws Exception {
        ElectricStillTileEntity still = fixture(998);
        still.tickUpdate(true);
        assertEquals(4, still.getInputTank().getFluidAmount());
        assertEquals(998, still.getOutputTank().getFluidAmount());
        assertEquals(64, still.getEnergy(Power.ELECTRIC_POWER), 0);
    }

    @Test public void exactSpaceProducesTheWholeBatchAndDebitsOnlyOneCycle() throws Exception {
        ElectricStillTileEntity still = fixture(997);
        still.tickUpdate(true);
        assertEquals(2, still.getInputTank().getFluidAmount());
        assertEquals(1000, still.getOutputTank().getFluidAmount());
        assertSame(FluidRegistry.LAVA, still.getOutputTank().getFluid().getFluid());
        assertEquals(48, still.getEnergy(Power.ELECTRIC_POWER), 0);
        still.tickUpdate(true);
        assertEquals(2, still.getInputTank().getFluidAmount());
        assertEquals(1000, still.getOutputTank().getFluidAmount());
        assertEquals(48, still.getEnergy(Power.ELECTRIC_POWER), 0);
    }

    @Test public void incompatibleOutputCannotBeOverwritten() throws Exception {
        ElectricStillTileEntity still = fixture(0);
        still.getOutputTank().setFluid(new FluidStack(FluidRegistry.WATER, 1));
        still.tickUpdate(true);
        assertEquals(FluidRegistry.WATER, still.getOutputTank().getFluid().getFluid());
        assertEquals(4, still.getInputTank().getFluidAmount());
        assertEquals(64, still.getEnergy(Power.ELECTRIC_POWER), 0);
    }

    @Test public void anIncompleteInputDoesNotInventAnOutput() throws Exception {
        ElectricStillTileEntity still = fixture(0);
        still.getInputTank().setFluid(new FluidStack(FluidRegistry.WATER, 1));
        still.tickUpdate(true);
        assertEquals(1, still.getInputTank().getFluidAmount());
        assertEquals(0, still.getOutputTank().getFluidAmount());
        assertEquals(64, still.getEnergy(Power.ELECTRIC_POWER), 0);
    }

    private static ElectricStillTileEntity fixture(int output) {
        ElectricStillTileEntity still = new ElectricStillTileEntity() {
            @Override protected boolean hasRedstoneSignal() { return false; }
            @Override protected void setActiveState(boolean active) { }
        };
        still.setEnergy(64, Power.ELECTRIC_POWER);
        still.getInputTank().setFluid(new FluidStack(FluidRegistry.WATER, 4));
        if (output > 0) still.getOutputTank().setFluid(new FluidStack(FluidRegistry.LAVA, output));
        return still;
    }

}
