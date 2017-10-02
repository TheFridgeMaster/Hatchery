package com.gendeathrow.hatchery.block.nestpen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiNestingPen extends GuiContainer
{

	 private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");
	    /** The player inventory currently bound to this GUI instance */
	    private final IInventory playerInventory;
	    /** The hopper inventory bound to this GUI instance */
	    private final IInventory nestingInventory;

	    public GuiNestingPen(InventoryPlayer playerInv, IInventory nestingPenIn)
	    {
	        super(new ContainerNestingPen(playerInv, nestingPenIn, Minecraft.getMinecraft().player));
	        this.playerInventory = playerInv;
	        this.nestingInventory = nestingPenIn;
	        this.allowUserInput = false;
	        this.ySize = 133;
	    }

	    /**
	     * Draw the foreground layer for the GuiContainer (everything in front of the items)
	     */
	    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	    {
	        this.fontRenderer.drawString(new TextComponentTranslation("container.nestingpen").getFormattedText(), 8, 6, 4210752);
	        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
	    }

	    /**
	     * Draws the background layer of this container (behind the items).
	     */
	    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	    {
	        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	        this.mc.getTextureManager().bindTexture(HOPPER_GUI_TEXTURE);
	        int i = (this.width - this.xSize) / 2;
	        int j = (this.height - this.ySize) / 2;
	        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	    }

}
