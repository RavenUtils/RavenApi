package com.sasnos.raven_api.blocks;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class EssentialsCommonMachineBlock extends Block {

  public EssentialsCommonMachineBlock(Properties properties) {
    super(properties);
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @SuppressWarnings("deprecation")
  @Override
  public abstract ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit);

  @Nullable
  @Override
  public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

  protected static void dropInventoryItems(World worldIn, BlockPos pos, IItemHandler inventory) {
    for (int i = 0; i < inventory.getSlots(); ++i) {
      ItemStack itemstack = inventory.getStackInSlot(i);

      if (itemstack.getCount() > 0) {
        InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
      }
    }
  }
}
