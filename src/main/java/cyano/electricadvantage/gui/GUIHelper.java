package cyano.electricadvantage.gui;

import com.mcmoddev.poweradvantage.api.simple.SimpleMachineGUI.GUIContainer;
import com.mcmoddev.poweradvantage.gui.FluidTankGUI;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class GUIHelper {
	private static final int INDICATOR_LED_X = 182;
	private static final int INDICATOR_LED_Y = 2;
	private static final int INDICATOR_LED_W = 7;
	private static final int INDICATOR_LED_H = 7;
	

	private static final int LED_BAR_X = 180;
	private static final int LED_BAR_Y = 27;
	private static final int LED_BAR_H = 9;
	private static final int LED_BAR_W8 = 4;
	

	public static void drawIndicatorLight(boolean on,GUIContainer gc, int xOffset, int yOffset){
		if(on){
			gc.drawTexturedModalRect(8+xOffset, 8+yOffset, INDICATOR_LED_X, INDICATOR_LED_Y, INDICATOR_LED_W, INDICATOR_LED_H);
		}
	}
	
	public static void drawProgressBar(int x, int y, float progress, GUIContainer gc){
		int n = (int)(8 * (progress + 0.0625f) );
		if(n <= 0) return;
		int w = 1 + (n * LED_BAR_W8);
		gc.drawTexturedModalRect(x, y, LED_BAR_X, LED_BAR_Y, w, LED_BAR_H);
	}
	

	public static void drawFluidBar(FluidStack fs, float barHeight, int xPos, int yPos, 
			GUIContainer guiContainer, int x, int y, float z,
			ResourceLocation displayImage){

		final int w = 16;
		final int barSlotHeight = 60;
		final int h = (int)(barSlotHeight * barHeight);

		FluidTankGUI.drawFluidFilledRectangle(guiContainer, fs,x+xPos,y+yPos+barSlotHeight-h,w,h,z);
	}
}
