package recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public interface ICommonRecipe extends IRecipe<RecipeWrapper> {

  @Override
  default boolean canFit(int width, int height) {
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
  default ItemStack getRecipeOutput() {
    return getOutput().get(0);
  }

  @Deprecated
  @Override
  default ItemStack getCraftingResult(RecipeWrapper inv) {
    return getOutput().get(0).copy();
  }

}
