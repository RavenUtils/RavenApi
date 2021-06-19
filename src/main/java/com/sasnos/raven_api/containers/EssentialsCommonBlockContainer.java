package com.sasnos.raven_api.containers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class EssentialsCommonBlockContainer extends EssentialsCommonContainer {
  protected TileEntity tileEntity;
  protected Block blocktype;
  protected PlayerEntity playerEntity;

  protected EssentialsCommonBlockContainer(@Nullable ContainerType<?> type,
                                           int id, World world, BlockPos pos,
                                           PlayerInventory playerInventoryIn,
                                           PlayerEntity player,
                                           Block blocktype) {
    super(type, id, playerInventoryIn);
    tileEntity = world.getBlockEntity(pos);
    this.blocktype = blocktype;
    this.playerEntity = player;
    if (tileEntity != null) {
      addSlots();
    }
  }

  protected abstract void addSlots();


  @Override
  public boolean stillValid(PlayerEntity playerIn) {
    return stillValid(
        IWorldPosCallable.create(tileEntity.getLevel(), tileEntity.getBlockPos()),
        playerEntity,
        blocktype);
  }

  @Override
  public abstract ItemStack quickMoveStack(PlayerEntity playerIn, int index);
}
