package com.sasnos.raven_api.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sasnos.raven_api.containers.EssentialsMachineBlockContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class EssentialsCommonMachineScreen<T extends EssentialsMachineBlockContainer> extends EssentialsCommonScreen<T> {

  public EssentialsCommonMachineScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super(screenContainer, inv, titleIn);
  }

  @Override
  protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
    this.minecraft.getTextureManager().bind(getGUI());
    int left = this.leftPos;
    int right = this.topPos;
    this.blit(matrixStack, left, right, 0, 0, this.imageWidth, this.imageHeight);
    if (this.menu.isBurning()) {
      int k = this.menu.getBurnLeftScaled();
      this.blit(matrixStack, left + 56, right + 36 + 12 - k, 176, 12 - k, 14, k + 1);
    }

    int l = this.menu.getCookProgressionScaled();
    this.blit(matrixStack, left + 79, right + 34, 176, 14, l + 1, 16);
  }
}
