package com.sasnos.raven_api.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public interface ICommonRecipe extends IRecipe<RecipeWrapper> {

  @Override
  default boolean canCraftInDimensions(int width, int height) {
    return false;
  }

  int getTimer();

  NonNullList<ItemStack> getOutput();

  float getXp();

  /**
   * Use the NonNullList Version {@link ICommonRecipe#getOutput()}
   *
   * @return
   */
  @Deprecated
  @Override
  default ItemStack getResultItem() {
    return getOutput().get(0);
  }

  @Deprecated
  @Override
  default ItemStack assemble(RecipeWrapper inv) {
    return getOutput().get(0).copy();
  }

}
