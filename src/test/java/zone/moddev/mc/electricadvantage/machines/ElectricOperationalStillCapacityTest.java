package zone.moddev.mc.electricadvantage.machines;

import cyano.poweradvantage.api.fluid.FluidStack;
import java.lang.reflect.Method;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.registry.Bootstrap;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.poweradvantage.registry.still.recipe.DistillationRecipe;
import zone.moddev.mc.poweradvantage.registry.still.recipe.ExplicitFluidReference;

import static org.junit.Assert.*;

/** Capacity preflight tests; the shared rig separately tests natural machine ticks. */
public class ElectricOperationalStillCapacityTest {
    @BeforeClass public static void bootstrap() { Bootstrap.bootStrap(); }

    @Test public void aFullTankCannotConsumeInput() throws Exception {
        ElectricStillTileEntity still = fixture(1000);
        assertFalse(preview(still));
        assertEquals(4, still.getInputTank().getFluidAmount());
        assertEquals(1000, still.getOutputTank().getFluidAmount());
    }

    @Test public void aPartialOutputMustFitTheWholeRecipe() throws Exception {
        ElectricStillTileEntity still = fixture(998);
        assertFalse(preview(still));
        assertEquals(4, still.getInputTank().getFluidAmount());
        assertEquals(998, still.getOutputTank().getFluidAmount());
    }

    @Test public void exactSpaceIsAcceptedWithoutMutatingEitherTank() throws Exception {
        ElectricStillTileEntity still = fixture(997);
        assertTrue(preview(still));
        assertEquals(4, still.getInputTank().getFluidAmount());
        assertEquals(997, still.getOutputTank().getFluidAmount());
    }

    @Test public void incompatibleOutputCannotBeOverwritten() throws Exception {
        ElectricStillTileEntity still = fixture(0);
        still.getOutputTank().setFluid(new FluidStack(Fluids.WATER, 1));
        assertFalse(preview(still));
        assertEquals(Fluids.WATER, still.getOutputTank().getFluid().getFluid());
        assertEquals(4, still.getInputTank().getFluidAmount());
    }

    @Test public void anIncompleteInputDoesNotInventAnOutput() throws Exception {
        ElectricStillTileEntity still = fixture(0);
        still.getInputTank().setFluid(new FluidStack(Fluids.WATER, 1));
        assertFalse(preview(still));
        assertEquals(1, still.getInputTank().getFluidAmount());
    }

    private static ElectricStillTileEntity fixture(int output) {
        ElectricStillTileEntity still = new ElectricStillTileEntity();
        still.getInputTank().setFluid(new FluidStack(Fluids.WATER, 4));
        if (output > 0) still.getOutputTank().setFluid(new FluidStack(Fluids.LAVA, output));
        return still;
    }

    private static boolean preview(ElectricStillTileEntity still) throws Exception {
        DistillationRecipe recipe = new DistillationRecipe(new ExplicitFluidReference(Fluids.WATER), 2,
                new ExplicitFluidReference(Fluids.LAVA), 3);
        Method method = ElectricStillTileEntity.class.getDeclaredMethod("hasSpaceForDistillation", DistillationRecipe.class);
        method.setAccessible(true);
        return (Boolean)method.invoke(still, recipe);
    }
}
