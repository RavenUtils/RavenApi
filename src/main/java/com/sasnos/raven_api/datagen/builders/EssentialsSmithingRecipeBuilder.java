package com.sasnos.raven_api.datagen.builders;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class EssentialsSmithingRecipeBuilder {
  private final Ingredient base;
  private final Ingredient addition;
  private final ItemStack output;
  private final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
  private final IRecipeSerializer<?> serializer;

  public EssentialsSmithingRecipeBuilder(IRecipeSerializer<?> serializer, Ingredient base, Ingredient addition, ItemStack output) {
    this.serializer = serializer;
    this.base = base;
    this.addition = addition;
    this.output = output;
  }

  public static EssentialsSmithingRecipeBuilder smithingRecipe(Ingredient base, Ingredient addition, Item output) {
    return new EssentialsSmithingRecipeBuilder(IRecipeSerializer.SMITHING, base, addition, new ItemStack(output));
  }

  public static EssentialsSmithingRecipeBuilder smithingRecipe(Ingredient base, Ingredient addition, ItemStack output) {
    return new EssentialsSmithingRecipeBuilder(IRecipeSerializer.SMITHING, base, addition, output);
  }

  public EssentialsSmithingRecipeBuilder addCriterion(String name, ICriterionInstance criterion) {
    this.advancementBuilder.addCriterion(name, criterion);
    return this;
  }

  public void build(Consumer<IFinishedRecipe> consumer) {
    this.build(consumer, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(output.getItem())).toString());
  }

  public void build(Consumer<IFinishedRecipe> consumer, String id) {
    this.build(consumer, new ResourceLocation(id));
  }

  public void build(Consumer<IFinishedRecipe> recipe, ResourceLocation id) {
    this.validate(id);
    this.advancementBuilder.parent(new ResourceLocation("RavenApi/src/main/java/com/sasnos/raven_api/recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(IRequirementsStrategy.OR);
    recipe.accept(new EssentialsSmithingRecipeBuilder.Result(id, this.serializer, this.base, this.addition, this.output, this.advancementBuilder, new ResourceLocation(id.getNamespace(), "RavenApi/src/main/java/com/sasnos/raven_api/recipes/" + Objects.requireNonNull(this.output.getItem().getItemCategory()).getRecipeFolderName() + "/" + id.getPath())));
  }

  private void validate(ResourceLocation id) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
  }

  public static class Result implements IFinishedRecipe {
    private final ResourceLocation id;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack output;
    private final Advancement.Builder advancementBuilder;
    private final ResourceLocation advancementId;
    private final IRecipeSerializer<?> serializer;

    public Result(ResourceLocation id, IRecipeSerializer<?> serializer, Ingredient base, Ingredient addition, ItemStack output, Advancement.Builder advancementBuilder, ResourceLocation advancementId) {
      this.id = id;
      this.serializer = serializer;
      this.base = base;
      this.addition = addition;
      this.output = output;
      this.advancementBuilder = advancementBuilder;
      this.advancementId = advancementId;
    }

    public void serializeRecipeData(JsonObject json) {
      json.add("base", this.base.toJson());
      json.add("addition", this.addition.toJson());
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.output.getItem())).toString());
      if (output.hasTag()) {
        CompoundNBT tag = output.getTag();
        assert tag != null;
        if (tag.contains("Damage")) tag.remove("Damage");
        jsonobject.addProperty("nbt", tag.toString());
      }
      json.add("result", jsonobject);
    }

    /**
     * Gets the ID for the recipe.
     */
    public @NotNull ResourceLocation getId() {
      return this.id;
    }

    public @NotNull IRecipeSerializer<?> getType() {
      return this.serializer;
    }

    /**
     * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
     */
    @Nullable
    public JsonObject serializeAdvancement() {
      return this.advancementBuilder.serializeToJson();
    }

    /**
     * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #serializeAdvancement()}
     * is non-null.
     */
    @Nullable
    public ResourceLocation getAdvancementId() {
      return this.advancementId;
    }
  }
}
