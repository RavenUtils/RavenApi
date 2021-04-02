package com.sasnos.raven_api.datagen.recipes;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EssentialsCookingRecipeBuilder {

  private final ItemStack result;
  private final Ingredient ingredient;
  private final float experience;
  private final int cookingTime;
  private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
  private String group;
  private final CookingRecipeSerializer<?> recipeSerializer;

  private EssentialsCookingRecipeBuilder(ItemStack resultIn, Ingredient ingredientIn, float experienceIn, int cookingTimeIn, CookingRecipeSerializer<?> serializer) {
    this.result = resultIn;
    this.ingredient = ingredientIn;
    this.experience = experienceIn;
    this.cookingTime = cookingTimeIn;
    this.recipeSerializer = serializer;
  }

  public static EssentialsCookingRecipeBuilder cookingRecipe(Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookingTimeIn, CookingRecipeSerializer<?> serializer) {
    return new EssentialsCookingRecipeBuilder(resultIn, ingredientIn, experienceIn, cookingTimeIn, serializer);
  }

  public static EssentialsCookingRecipeBuilder blastingRecipe(Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookingTimeIn) {
    return cookingRecipe(ingredientIn, resultIn, experienceIn, cookingTimeIn, IRecipeSerializer.BLASTING);
  }

  public static EssentialsCookingRecipeBuilder smeltingRecipe(Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookingTimeIn) {
    return cookingRecipe(ingredientIn, resultIn, experienceIn, cookingTimeIn, IRecipeSerializer.SMELTING);
  }

  public EssentialsCookingRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn) {
    this.advancementBuilder.withCriterion(name, criterionIn);
    return this;
  }

  public void build(Consumer<IFinishedRecipe> consumerIn) {
    this.build(consumerIn, ForgeRegistries.ITEMS.getKey(this.result.getItem()));
  }

  public void build(Consumer<IFinishedRecipe> consumerIn, String save) {
    ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result.getItem());
    ResourceLocation resourcelocation1 = new ResourceLocation(save);
    if (resourcelocation1.equals(resourcelocation)) {
      throw new IllegalStateException("Recipe " + resourcelocation1 + " should remove its 'save' argument");
    } else {
      this.build(consumerIn, resourcelocation1);
    }
  }

  public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
    this.validate(id);
    this.advancementBuilder.withParentId(new ResourceLocation("RavenApi/src/main/java/com/sasnos/raven_api/recipes/root")).withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id)).withRewards(AdvancementRewards.Builder.recipe(id)).withRequirementsStrategy(IRequirementsStrategy.OR);
    consumerIn.accept(new EssentialsCookingRecipeBuilder.Result(id, this.group == null ? "" : this.group, this.ingredient, this.result, this.experience, this.cookingTime, this.advancementBuilder, new ResourceLocation(id.getNamespace(), "RavenApi/src/main/java/com/sasnos/raven_api/recipes/" + this.result.getItem().getGroup().getPath() + "/" + id.getPath()), this.recipeSerializer));
  }

  /**
   * Makes sure that this obtainable.
   */
  private void validate(ResourceLocation id) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    if (id == null) {
      throw new IllegalArgumentException("Item cannot be null or ItemStack.EMPTY");
    }
  }

  public static class Result implements IFinishedRecipe {
    private final ResourceLocation id;
    private final String group;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final float experience;
    private final int cookingTime;
    private final Advancement.Builder advancementBuilder;
    private final ResourceLocation advancementId;
    private final IRecipeSerializer<? extends AbstractCookingRecipe> serializer;

    public Result(ResourceLocation idIn, String groupIn, Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookingTimeIn, Advancement.Builder advancementBuilderIn, ResourceLocation advancementIdIn, IRecipeSerializer<? extends AbstractCookingRecipe> serializerIn) {
      this.id = idIn;
      this.group = groupIn;
      this.ingredient = ingredientIn;
      this.result = resultIn;
      this.experience = experienceIn;
      this.cookingTime = cookingTimeIn;
      this.advancementBuilder = advancementBuilderIn;
      this.advancementId = advancementIdIn;
      this.serializer = serializerIn;
    }

    public void serialize(JsonObject json) {
      if (!this.group.isEmpty()) {
        json.addProperty("group", this.group);
      }

      json.add("ingredient", this.ingredient.serialize());

      if (this.result.getCount() > 1) {
        json.add("result", serializeItem(this.result));
      } else {
        json.addProperty("result", ForgeRegistries.ITEMS.getKey(this.result.getItem()).toString());
      }
      json.addProperty("experience", this.experience);
      json.addProperty("cookingtime", this.cookingTime);
    }

    private JsonObject serializeItem(ItemStack result) {

      JsonObject json = new JsonObject();
      json.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());
      json.addProperty("count", result.getCount());
      return json;
    }

    public IRecipeSerializer<?> getSerializer() {
      return this.serializer;
    }

    /**
     * Gets the ID for the recipe.
     */
    public ResourceLocation getID() {
      return this.id;
    }

    /**
     * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
     */
    @Nullable
    public JsonObject getAdvancementJson() {
      return this.advancementBuilder.serialize();
    }

    /**
     * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson}
     * is non-null.
     */
    @Nullable
    public ResourceLocation getAdvancementID() {
      return this.advancementId;
    }
  }
}
