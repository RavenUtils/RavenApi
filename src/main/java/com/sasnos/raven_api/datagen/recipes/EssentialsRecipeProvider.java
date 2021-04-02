package com.sasnos.raven_api.datagen.recipes;

import net.minecraft.data.IFinishedRecipe;

import java.util.function.Consumer;

public abstract class EssentialsRecipeProvider implements IEssentialRecipeProvider {

  protected Consumer<IFinishedRecipe> consumer;

  public EssentialsRecipeProvider(Consumer<IFinishedRecipe> consumer) {
    this.consumer = consumer;
    init();
  }
}
