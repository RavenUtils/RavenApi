package datagen.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sasnos.ravenutils.init.ModRecipes;
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
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class HandMillRecipeBuilder {
  private final IItemProvider output;
  private final Ingredient input;
  private final int count;
  private final float change;
  private final int timer;
  private ItemStack additionalResult;
  private float additionalChance;
  private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
  private String group;

  private HandMillRecipeBuilder(Ingredient input, int count, int timer, IItemProvider output, float additionalDropChance) {
    this.input = input;
    this.count = count;
    this.timer = timer;
    this.output = output;
    this.change = additionalDropChance;
  }

  public static HandMillRecipeBuilder millRecipe(Ingredient input, int count, int timer, IItemProvider output, float additionalDropChance) {
    return new HandMillRecipeBuilder(input, count, timer, output, additionalDropChance);
  }

  public HandMillRecipeBuilder setAdditionalOutput(ItemStack additionalOutput, float additionalChance) {
    this.additionalResult = additionalOutput;
    this.additionalChance = additionalChance;
    return this;
  }

  public HandMillRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn) {
    this.advancementBuilder.withCriterion(name, criterionIn);
    return this;
  }

  public HandMillRecipeBuilder setGroup(String groupIn) {
    this.group = groupIn;
    return this;
  }

  public void build(Consumer<IFinishedRecipe> consumerIn) {
    this.build(consumerIn, ForgeRegistries.ITEMS.getKey(this.output.asItem()));
  }

  public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
    this.validate(id);
    this.advancementBuilder
        .withParentId(new ResourceLocation("RavenApi/src/main/java/com/sasnos/raven_api/recipes/root"))
        .withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id))
        .withRewards(AdvancementRewards.Builder.recipe(id))
        .withRequirementsStrategy(IRequirementsStrategy.OR);
    ResourceLocation advancementId = new ResourceLocation(id.getNamespace(), "RavenApi/src/main/java/com/sasnos/raven_api/recipes/" + Objects.requireNonNull(this.output.asItem().getGroup()).getPath() + "/" + id.getPath());
    consumerIn.accept(createFinishedRecipe(
        id,
        this.group == null ? "" : this.group,
        this.output, this.count, this.input,
        timer, this.advancementBuilder, change,
        advancementId, additionalResult.getItem(), additionalResult.getCount(), additionalChance));
  }

  protected IFinishedRecipe createFinishedRecipe(
      ResourceLocation id, String group, IItemProvider result,
      int count, Ingredient input, int timer,
      Advancement.Builder advancementBuilder, float change, ResourceLocation advancementId, Item additionalResult,
      int additionalCount, float additionalChance) {
    return
        new Result(id, group, result,
            count, input, timer, change,
            advancementBuilder, advancementId, additionalResult,
            additionalCount, additionalChance);
  }

  private void validate(ResourceLocation id) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining alloy recipe " + id);
    }
    if (change > 1) {
      throw new IllegalStateException("The additional drop chance cannot be greater than 1");
    }
    if (id.getNamespace().equals("minecraft") && ForgeRegistries.ITEMS.containsKey(id)) {
      throw new IllegalStateException("Change Name of Recipe to avoid Problems with other Mods for Recipe " + id);
    }
  }

  protected static class Result implements IFinishedRecipe {

    public final ResourceLocation id;
    private final IItemProvider output;
    private final Ingredient input;
    private final int count;
    private final float change;
    private final int timer;
    private final Item additionalResult;
    private final int additionalCount;
    private final float additionalChance;
    private final String group;
    public final Advancement.Builder advBuilder;
    public final ResourceLocation advancementId;

    Result(
        ResourceLocation id,
        String group,
        IItemProvider result,
        int count,
        Ingredient input,
        int timer,
        float change, Advancement.Builder advancementBuilder,
        ResourceLocation advancementId,
        Item additionalResult,
        int additionalCount,
        float additionalChance) {
      this.id = id;
      this.group = group;
      this.output = result;
      this.input = input;
      this.count = count;
      this.timer = timer;
      this.change = change;
      this.advBuilder = advancementBuilder;
      this.advancementId = advancementId;
      this.additionalResult = additionalResult;
      this.additionalCount = additionalCount;
      this.additionalChance = additionalChance;
    }

    @Override
    public void serialize(@NotNull JsonObject json) {

      if (!this.group.isEmpty()) {
        json.addProperty("group", this.group);
      }

      JsonElement ingredients = this.input.serialize();
      if (ingredients.isJsonArray()) {
        JsonArray ingredientArray = JSONUtils.getJsonArray(ingredients, null);
        final int[] i = {0};
        ingredientArray.forEach(jsonElement -> {
          jsonElement.getAsJsonObject().addProperty("count", this.input.getMatchingStacks()[i[0]].getCount());
          i[0]++;
        });
      }

      json.add("ingredient", ingredients);

      JsonObject resultJson = new JsonObject();
      resultJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.output.asItem())).toString());
      if (this.count > 1) {
        resultJson.addProperty("count", this.count);
      }
      json.add("result", resultJson);
      json.addProperty("additionalDropChance", change);
      if (additionalResult != null) {
        JsonObject additionalResult = new JsonObject();
        additionalResult.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.additionalResult)).toString());
        if (additionalCount > 1) {
          additionalResult.addProperty("count", additionalCount);
        }
        additionalResult.addProperty("change", additionalChance);
        json.add("additional", additionalResult);
      }
      json.addProperty("timer", timer);
    }

    @Override
    public @NotNull ResourceLocation getID() {
      return id;
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer() {
      return ModRecipes.HAND_MILL_RECIPE_SERIALIZER.get();
    }

    @Nullable
    @Override
    public JsonObject getAdvancementJson() {
      return advBuilder.serialize();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementID() {
      return advancementId;
    }
  }
}
