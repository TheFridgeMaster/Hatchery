package com.gendeathrow.hatchery.block.fertilizermixer;

import javax.annotation.Nullable;

import com.gendeathrow.hatchery.block.TileUpgradable;
import com.gendeathrow.hatchery.core.init.ModBlocks;
import com.gendeathrow.hatchery.core.init.ModFluids;
import com.gendeathrow.hatchery.core.init.ModItems;
import com.gendeathrow.hatchery.inventory.InventoryStorage;
import com.gendeathrow.hatchery.item.upgrades.BaseUpgrade;
import com.gendeathrow.hatchery.item.upgrades.RFEfficiencyUpgrade;
import com.gendeathrow.hatchery.storage.EnergyStorageRF;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class FertilizerMixerTileEntity extends TileUpgradable implements IInventory, ITickable, IEnergyReceiver
{
	int baseTankSize = 12000;
	int baseRFStorage = 20000;
	int baseRFTick = 20;
	double rfEffencyMultpyler = 1;
	double upgradeSpeedMulipyler = 1;
	
	private FluidTank waterTank = new FluidTank(new FluidStack(FluidRegistry.WATER, 0), 12000){
		@Override
	    public boolean canDrain()
	    {
			return false;
		}
	};
	
	
	private FluidTank fertilizerTank = new FluidTank(new FluidStack(ModFluids.liquidfertilizer, 0), 12000){
		@Override
		public boolean canFill()
		{
			return false;
		}
	};
	
	  //TODO: This is here for compilation purposes - i need logic!!!
	public boolean isEmpty(){
		return true;
	}

	private FluidHandlerFluidMap fluidMap = new FluidHandlerFluidMap().addHandler(ModFluids.liquidfertilizer, fertilizerTank).addHandler(FluidRegistry.WATER, waterTank);
	
	protected InventoryStorage inventory = new InventoryStorage(this, 5);
	
	protected EnergyStorageRF energy = new EnergyStorageRF(20000).setMaxReceive(100);
	
	public FertilizerMixerTileEntity() 
	{
		super(2);
	}

	
	public FluidTank getWaterTank()
	{
		return waterTank;
	}
	
	public FluidTank getFertilizerTank()
	{
		return fertilizerTank;
	}
	
	public ItemStack[] getItemInventory()
	{
		return inventory.getInventory();
	}

    public boolean isMixing()
    {
        return this.fertlizerMixTime > 0;
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean isMixing(IInventory inventory)
    {
        return inventory.getField(2) > 0;
    }
    
    public boolean canMix()
    {
   	  	return waterTank.getFluidAmount() > 0 && this.fertilizerTank.getFluidAmount() < this.fertilizerTank.getCapacity();
    }
    
    public static int getItemMixTime(ItemStack stack)
    {
    	if(stack == null)
    	{
    		return 0;
    	}
    	else
    	{
    		Item item = stack.getItem();
    		
    		if(item == ModItems.manure)
    			return 100;
    		else if(item == Item.getItemFromBlock(ModBlocks.manureBlock))
    			return 1120;
    	}
    	return 0;
    }

	private int fertlizerMixTime;
    private int currentItemMixTime;
    private int mixTime;
    private int totalMixTime;

    
    
	@Override
	public void update() 
	{

		boolean flag = this.isMixing();
		boolean flag1 = false;
			
		if(this.isMixing())
		{
			this.fertlizerMixTime--;
		}
		
		if(this.world != null && !this.world.isRemote)
		{
			updateUpgrades();
			
			
			if (this.isMixing() || this.inventory.getStackInSlot(0) != null)
			{
                if (!this.isMixing() && this.canMix())
                {
                	this.fertlizerMixTime = getItemMixTime(this.inventory.getStackInSlot(0));
                	this.currentItemMixTime = this.fertlizerMixTime;
                			
                	if(this.isMixing())
                	{
                		flag1 = true;
                		 if(this.inventory.getStackInSlot(0) != null)
                		 {
							 this.inventory.getStackInSlot(0).setCount(this.inventory.getStackInSlot(0).getCount() - 1);
                    		 
                             if (this.inventory.getStackInSlot(0).getCount() <= 0)
                             {
                                 this.inventory.setInventorySlotContents(0,null);
                             }
                		 }

                	}
                }
                
    			int rfNeeded = (int)((10 * this.upgradeSpeedMulipyler) * this.rfEffencyMultpyler);
    			
    			if(this.isMixing() && this.canMix() && fertlizerMixTime >= 5 && this.energy.getEnergyStored() >= rfNeeded)
    			{
    					this.fertlizerMixTime -= 5 * this.upgradeSpeedMulipyler;
    					this.energy.extractEnergy(rfNeeded, false);
    				
    					this.fertilizerTank.fillInternal(new FluidStack(ModFluids.liquidfertilizer,(int)(5 * this.upgradeSpeedMulipyler)), true);
    					this.waterTank.drainInternal((int)(5 * this.upgradeSpeedMulipyler), true);
    					
    					
    					System.out.println("remove/add tank:"+ (5 * this.upgradeSpeedMulipyler) +" rfneeded:"+  rfNeeded);
    					flag1 = true;
    			}
    			else if(this.isMixing() && this.canMix() && fertlizerMixTime >= 1)
    			{
					this.fertlizerMixTime -= 1;
    				
					this.fertilizerTank.fillInternal(new FluidStack(ModFluids.liquidfertilizer, 1), true);
					this.waterTank.drainInternal(1, true);
					
					flag1 = true;
    			}
    			else
    			{
    				this.mixTime = 0;
    			}
			}
			else if(!this.isMixing() && this.mixTime > 0)
			{
				 this.mixTime = MathHelper.clamp(this.mixTime - 2, 0, this.totalMixTime);
			}
			

			if(this.getStackInSlot(1) != null && this.waterTank.getFluidAmount() < this.waterTank.getCapacity())
			{
				ItemStack stack = this.getStackInSlot(1);
				
				ItemStack newStack = this.getStackInSlot(1).copy();
				newStack.setCount(1);
				
				IFluidHandler handler = FluidUtil.getFluidHandler(newStack);

				if(handler != null)
				{
		            if (this.getStackInSlot(2) == null)
		            {
		            	if(FluidUtil.tryFluidTransfer(this.waterTank, handler, this.waterTank.getCapacity(), true) != null)
		            	{
		            		if(newStack.getCount() > 0)
		            			this.inventory.setInventorySlotContents(2, newStack);
						
		            		this.decrStackSize(1, 1);
		            	}
		            }
				}
			}
			
			if(this.inventory.getStackInSlot(3) != null && this.inventory.getStackInSlot(4) == null && this.fertilizerTank.getFluidAmount() > 0)
			{
				ItemStack oldStack = this.inventory.getStackInSlot(3);
				ItemStack newStack = this.inventory.getStackInSlot(3).copy();
				
				if(newStack.getCount() > 1)
					newStack.setCount(1);
				
				IFluidHandler handler = FluidUtil.getFluidHandler(newStack);
				
				if(handler != null)
				{

					if(FluidUtil.tryFluidTransfer(handler, this.fertilizerTank, this.fertilizerTank.getCapacity(), true) != null)
					{
						this.setInventorySlotContents(4, newStack);
						
						if(oldStack.getCount() > 1)
							this.decrStackSize(3, 1);
						else
							this.setInventorySlotContents(3, null);
					}
				}
			}
			

		}

		if(flag != this.isMixing())
		{
			flag1 = true;

			//  BlockFurnace.setState(this.isBurning(), this.world, this.pos);
		}
		
		
        if (flag1)
        {
            this.markDirty();
        }
	}
	
	
	
	
	protected void updateUpgrades()
	{
		boolean rfupgrade = false;
		boolean rfcapacity = false;
		boolean tankcapacity = false;
		boolean speedupgrade = false;
		
		for(ItemStack upgrade : this.getUpgrades())
		{
			if(upgrade == null) continue;
		
			
			if(upgrade.getItem() instanceof RFEfficiencyUpgrade)
			{
				rfupgrade = true;
				this.rfEffencyMultpyler = 1 - ((BaseUpgrade)upgrade.getItem()).getUpgradeTier(upgrade, "") * 0.10;
			}
			if(upgrade.getItem() == ModItems.speedUpgradeTier && speedupgrade == false)
			{
				speedupgrade = true;
				this.upgradeSpeedMulipyler = 1 + ((BaseUpgrade)upgrade.getItem()).getUpgradeTier(upgrade, "") * 1 ;
			}
			
			if(upgrade.getItem() == ModItems.tankUpgradeTier1 && tankcapacity == false)
			{
				tankcapacity = true;
				
				int tier = upgrade.getMetadata()+1;
				int newTank = this.baseTankSize;
				
				if(tier == 1) {
					newTank += 2000;
				}
				else if(tier == 2) {
					newTank += 4000; 
				}
				else if(tier == 3) {
					newTank += 6000; 
				}

				if(newTank != this.fertilizerTank.getCapacity())
				{
					this.fertilizerTank.setCapacity(newTank);
				}
				
				if(newTank != this.waterTank.getCapacity())
				{
					this.waterTank.setCapacity(newTank);
				}
				
			}
			
			if(upgrade.getItem() == ModItems.rfCapacityUpgradeTier1 && rfcapacity == false)
			{
				int tier = upgrade.getMetadata()+1;
				
				int newEnergy = this.baseRFStorage;
				
				rfcapacity = true;

				if(tier == 1) {
					newEnergy += (int)(newEnergy * 0.5); 
				}
				else if(tier == 2) {
					newEnergy += (int)(newEnergy * 0.75); 
				}
				else if(tier == 3) {
					newEnergy += (int)(newEnergy * 1); 
				}

				if(newEnergy != this.energy.getMaxEnergyStored())
				{
					this.energy.setCapacity(newEnergy);
				}
			}
		}

		
		if(!tankcapacity && this.fertilizerTank.getCapacity() != this.baseTankSize)
		{
			this.fertilizerTank.setCapacity(this.baseTankSize);
		}
		
		if(!tankcapacity && this.waterTank.getCapacity() != this.baseTankSize)
		{
			this.waterTank.setCapacity(this.baseTankSize);
		}
		
		if(!rfcapacity && this.energy.getMaxEnergyStored() != this.baseRFStorage)
		{
			this.energy.setCapacity(this.baseRFStorage);
		}
		
		if(!rfupgrade && this.rfEffencyMultpyler != 1)
		{
			this.rfEffencyMultpyler = 1;
		}
		
		
		if(!speedupgrade && this.upgradeSpeedMulipyler != 1)
		{
			this.upgradeSpeedMulipyler = 1;
		}

	}

	public int getMixTime(@Nullable ItemStack stack)
	{
		return 200;
	}
	
	/// INVENTORY
	@Override
	public String getName() { return null; }

	@Override
	public boolean hasCustomName() { return false; }

	@Override
	public int getSizeInventory() 
	{
		return 5;
	}

	@Override
	public ItemStack getStackInSlot(int slot) 
	{
		return this.inventory.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) 
	{
		return this.inventory.decrStackSize(slot, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) 
	{
		return this.inventory.removeStackFromSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) 
	{
		this.inventory.setInventorySlotContents(slot, stack);
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	public boolean isUsableByPlayer(EntityPlayer player) { return true; }

	@Override
	public void openInventory(EntityPlayer player) { this.inventory.openInventory(player); }

	@Override
	public void closeInventory(EntityPlayer player) { this.inventory.closeInventory(player); }

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) 
	{
		if(index == 0 && (stack.getItem() == ModItems.manure || stack.getItem() == Item.getItemFromBlock(ModBlocks.manureBlock)))
				return true;
		else if(index == 1 && stack.getItem() == Items.WATER_BUCKET)
			return true;
		else if(index == 3 && stack.getItem() == ModFluids.getFertilizerBucket().getItem())
			return true;

		return false; 
	}

	@Override
	public int getField(int id) 
	{ 
        switch (id)
        {
            case 0:
            	return this.waterTank.getFluidAmount();
            case 1:
            	return this.fertilizerTank.getFluidAmount();	
            case 2:
            	return this.fertlizerMixTime;
            case 3:
            	return this.energy.getEnergyStored();
            case 4:
            	return this.waterTank.getCapacity();
            case 5:
            	return this.fertilizerTank.getCapacity();
            case 6: 
            	return this.energy.getMaxEnergyStored();
            default:
                return 0;
        }

	}
	
	public int waterLevel = 0;
	public int fertilizerLevel = 0;
	public int storedEnergyLevel = 0;
	@Override
	public void setField(int id, int value) 
	{ 
		
        switch (id)
        {
            case 0:
            	waterLevel = value;
                break;
            case 1:
            	fertilizerLevel = value;
                break;
            case 2:
                this.fertlizerMixTime  = value;
                break;
            case 3:
            	this.energy.setEnergyStored(value);
            	break;
            case 4:
            	this.waterTank.setCapacity(value);
            	break;
            case 5:
            	this.fertilizerTank.setCapacity(value);
                break;
            case 6:
            	this.energy.setCapacity(value);
            	break;

        }
	}

	@Override
	public int getFieldCount() { return 4; }

	@Override
	public void clear() { 
		this.inventory.clear();
	}
	
	

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
       	this.waterTank.readFromNBT(tag.getCompoundTag("waterTank"));
        this.fertilizerTank.readFromNBT(tag.getCompoundTag("fertilizerTank"));
        
        this.energy.readFromNBT(tag);
        
        this.inventory.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
    	tag = super.writeToNBT(tag);
    	
    	NBTTagCompound waterTTag = new NBTTagCompound();
        NBTTagCompound fertTTag = new NBTTagCompound();
    		this.waterTank.writeToNBT(waterTTag);
    		this.fertilizerTank.writeToNBT(fertTTag);
        tag.setTag("waterTank", waterTTag);
        tag.setTag("fertilizerTank", fertTTag);
        
        this.inventory.writeToNBT(tag);
        
        this.energy.writeToNBT(tag);
        return tag;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
    	if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
    		return (T) this.fluidMap;
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) 
            return (T) new InvWrapper(this);
		else if (capability == CapabilityEnergy.ENERGY) 
			return (T) this.energy;
        return super.getCapability(capability, facing);
    }

    // ENERGY
	@Override
	public int getEnergyStored(EnumFacing from) {
		return this.energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return this.energy.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return this.energy.receiveEnergy(maxReceive, simulate);
	}


    
}
