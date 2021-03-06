package com.sasnos.raven_api.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sasnos.raven_api.containers.EssentialsCommonContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class EssentialsCommonScreen<T extends EssentialsCommonContainer> extends ContainerScreen<T> {


  public EssentialsCommonScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super(screenContainer, inv, titleIn);
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(matrixStack, mouseX, mouseY);
  }

  @Override
  protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
    this.minecraft.getTextureManager().bind(getGUI());
    this.blit(matrixStack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
  }

  protected abstract ResourceLocation getGUI();
}
