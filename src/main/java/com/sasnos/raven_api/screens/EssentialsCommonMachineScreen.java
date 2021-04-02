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
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
    this.minecraft.getTextureManager().bindTexture(getGUI());
    int left = this.guiLeft;
    int right = this.guiTop;
    this.blit(matrixStack, left, right, 0, 0, this.xSize, this.ySize);
    if (this.container.isBurning()) {
      int k = this.container.getBurnLeftScaled();
      this.blit(matrixStack, left + 56, right + 36 + 12 - k, 176, 12 - k, 14, k + 1);
    }

    int l = this.container.getCookProgressionScaled();
    this.blit(matrixStack, left + 79, right + 34, 176, 14, l + 1, 16);
  }
}
