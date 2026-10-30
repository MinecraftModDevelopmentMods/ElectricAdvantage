package com.mcmoddev.electricadvantage.gui;

import cyano.poweradvantage.api.simple.SimpleMachineGUI.GUIContainer;
import com.mcmoddev.poweradvantage.gui.FluidTankGUI;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public class GUIHelper {
	private static final int INDICATOR_LED_X = 182;
	private static final int INDICATOR_LED_Y = 2;
	private static final int INDICATOR_LED_W = 7;
	private static final int INDICATOR_LED_H = 7;
	

	private static final int LED_BAR_X = 180;
	private static final int LED_BAR_Y = 27;
	private static final int LED_BAR_H = 9;
	private static final int LED_BAR_W8 = 4;
	

	private static final Map<ResourceLocation,ResourceLocation> realTextureLocationCache = new HashMap<>();
	
	public static void drawIndicatorLight(boolean on,GUIContainer gc, int xOffset, int yOffset){
		if(on){
			gc.drawTexturedModalRect(8+xOffset, 8+yOffset, INDICATOR_LED_X, INDICATOR_LED_Y, INDICATOR_LED_W, INDICATOR_LED_H);
		}
	}
	
	public static void drawProgressBar(int x, int y, float progress, GUIContainer gc){
		int w = progressBarWidth(progress);
		if(w == 0) return;
		gc.drawTexturedModalRect(x, y, LED_BAR_X, LED_BAR_Y, w, LED_BAR_H);
	}

	static int progressBarWidth(float progress) {
		progress = Float.isFinite(progress) ? Math.max(0F, Math.min(1F, progress)) : 0F;
		int n = (int)(8 * (progress + 0.0625f));
		return n <= 0 ? 0 : 1 + n * LED_BAR_W8;
	}
	

	public static void drawFluidBar(FluidStack fs, float barHeight, int xPos, int yPos, 
			GUIContainer guiContainer, int x, int y, float z,
			ResourceLocation displayImage){

		barHeight = Float.isFinite(barHeight) ? Math.max(0F, Math.min(1F, barHeight)) : 0F;
		final int w = 16;
		final int barSlotHeight = 60;
		final int h = (int)(barSlotHeight * barHeight);

		FluidTankGUI.drawFluidFilledRectangle(guiContainer, fs,x+xPos,y+yPos+barSlotHeight-h,w,h,z);
	}
}
