package com.lewolfyt.wolfmod.server.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TestItem extends Item {
	
	protected int MAX_STACK_SIZE = 1;

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		NBTTagCompound nbt = new NBTTagCompound();
		
		if(stack.getTagCompound() == null) {
    		stack.setTagCompound(new NBTTagCompound());
    	}
    
		if(!playerIn.isSneaking()) {
           	nbt.setFloat("dim", playerIn.dimension);
        	nbt.setFloat("posX", pos.getX());
        	nbt.setFloat("posY", pos.getY());
        	nbt.setFloat("posZ", pos.getZ());
        	stack.getTagCompound().setTag("coords", nbt);
        	stack.setStackDisplayName(EnumChatFormatting.AQUA + "Test Item with ntb i mean nbt");
        	
        }
		return false;
    }
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		if(playerIn.isSneaking()) {
			if(stack.getTagCompound() != null) {
				stack.getTagCompound().removeTag("coords");
				stack.clearCustomName();
			}
		}

        return stack;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if(stack.getTagCompound() != null) {
			if(stack.getTagCompound().hasKey("coords")) {
				NBTTagCompound nbt = (NBTTagCompound) stack.getTagCompound().getTag("coords");
				float dim = nbt.getFloat("dim");
				float posX = nbt.getFloat("posX");
				float posY = nbt.getFloat("posY");
				float posZ = nbt.getFloat("posZ");
				tooltip.add("Dim: " + dim + "X: " + posX + "Y: " + posY + "Z: " + posZ);
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
		if(stack.getTagCompound() != null) {
			return stack.getTagCompound().hasKey("coords");
		}
        return false;
    }
	
public Item setMaxStackSize(int maxStackSize)
  {
    this.maxStackSize = MAX_STACK_SIZE;
     return this;
   }
}
