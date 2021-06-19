package com.sasnos.raven_api.containers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class EssentialsMachineBlockContainer extends EssentialsCommonBlockContainer {

  private final IIntArray machineData;

  protected EssentialsMachineBlockContainer(@Nullable ContainerType<?> type, int id, World world, BlockPos pos, PlayerInventory playerInventoryIn, PlayerEntity player, Block blocktype, IIntArray data) {
    super(type, id, world, pos, playerInventoryIn, player, blocktype);

    this.machineData = data;

    addDataSlots(machineData);
  }


  public int getCookProgressionScaled() {
    int i = machineData.get(1);
    int j = machineData.get(2);
    return j != 0 && i != 0 ? i * 24 / j : 0;
  }


  public int getBurnLeftScaled() {
    int i = machineData.get(3);
    if (i == 0) {
      i = 200;
    }

    return machineData.get(0) * 13 / i;
  }


  public boolean isBurning() {
    return machineData.get(0) > 0;
  }

}
