package com.gendeathrow.hatchery.client.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ConfigGuiFactory implements IModGuiFactory
{

	@Override
	public void initialize(Minecraft minecraftInstance) 
	{
		
	}

	//TODO: this is just so we can get on the way to compiling...
	@Override
	public GuiScreen createConfigGui(GuiScreen var1){
		return var1;
	}

	//TODO: Temporary implementation, just so we can compile.
	@Override
	public boolean hasConfigGui(){
		return false;
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() 
	{
		return GuiHatcheryConfig.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
	{
		return null;
	}

	//TODO: Fix my depcrcated function, I will be removed in mc 1.12
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
	{
		return null;
	}

}
