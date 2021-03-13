package datagen.builders;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sasnos.ravenutils.api.recipes.Material;
import com.sasnos.ravenutils.init.ModRecipes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AlloyFurnaceRecipeBuilder {

  private final Item result;
  private Item additionalResult = null;
  private int additionalCount = 0;
  private float additionalChance = 0;
  private final int count;
  private final int timer;
  private String group;
  private final List<Material> materials = Lists.newArrayList();
  private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();

  public static AlloyFurnaceRecipeBuilder alloyRecipe(Item result, int count, int timer) {
    return new AlloyFurnaceRecipeBuilder(result, count, timer);
  }

  protected AlloyFurnaceRecipeBuilder(Item resultIn, int countIn, int timerIn) {
    result = resultIn;
    count = countIn;
    if (timerIn < 1) throw new IllegalArgumentException("Timer must be greater than 1");
    timer = timerIn;
  }

  public AlloyFurnaceRecipeBuilder addMaterial(Ingredient ingredientIn) {
    return addMaterial(ingredientIn, 1);
  }


  public AlloyFurnaceRecipeBuilder addMaterial(Ingredient ingredientIn, int count) {
    if (materials.size() >= 2) {
      throw new IllegalArgumentException("There can only be up to 2 materials!");
    }
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be a positive integer!");
    }
    materials.add(Material.of(ingredientIn, count));
    return this;
  }

  public AlloyFurnaceRecipeBuilder addAdditionalOutput(Item output, int count, float change) {
    additionalResult = output;
    additionalCount = count;
    additionalChance = change;
    return this;
  }

  public AlloyFurnaceRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn) {
    this.advancementBuilder.withCriterion(name, criterionIn);
    return this;
  }

  public AlloyFurnaceRecipeBuilder setGroup(String groupIn) {
    this.group = groupIn;
    return this;
  }

  public void build(Consumer<IFinishedRecipe> consumerIn) {
    this.build(consumerIn, ForgeRegistries.ITEMS.getKey(this.result));
  }

  public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
    this.validate(id);
    this.advancementBuilder
        .withParentId(new ResourceLocation("RavenApi/src/main/java/com/sasnos/raven_api/recipes/root"))
        .withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id))
        .withRewards(AdvancementRewards.Builder.recipe(id))
        .withRequirementsStrategy(IRequirementsStrategy.OR);
    assert this.result.getGroup() != null;
    ResourceLocation advancementId = new ResourceLocation(id.getNamespace(), "RavenApi/src/main/java/com/sasnos/raven_api/recipes/" + this.result.getGroup().getPath() + "/" + id.getPath());
    consumerIn.accept(createFinishedRecipe(id, this.group == null ? "" : this.group, this.result, this.count, this.materials, timer, this.advancementBuilder, advancementId, additionalResult, additionalCount, additionalChance));
  }

  private void validate(ResourceLocation id) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    if (this.materials.isEmpty()) {
      throw new IllegalStateException("No ingredients for recipe " + id);
    }
    if (id.getNamespace().equals("minecraft") && ForgeRegistries.ITEMS.containsKey(id)) {
      throw new IllegalStateException("Change name of recipe to avoid problems with other mods for recipe " + id);
    }
  }

  protected IFinishedRecipe createFinishedRecipe(
      ResourceLocation id, String group, Item result,
      int count, List<Material> materials, int timer,
      Advancement.Builder advancementBuilder, ResourceLocation advancementId, Item additionalResult,
      int additionalCount, float additionalChance) {
    return
        new Result(id, group, result,
            count, materials, timer,
            advancementBuilder, advancementId, additionalResult,
            additionalCount, additionalChance);
  }

  protected static class Result implements IFinishedRecipe {

    public final ResourceLocation id;
    public final String group;
    public final Item result;
    private final Item additionalResult;
    private final int additionalCount;
    private final float additionalChance;
    public final int count;
    public final int timer;
    public final List<Material> materials;
    public final Advancement.Builder advBuilder;
    public final ResourceLocation advancementId;

    Result(
        ResourceLocation id,
        String group,
        Item result,
        int count,
        List<Material> materials,
        int timer,
        Advancement.Builder advancementBuilder,
        ResourceLocation advancementId,
        Item additionalResult,
        int additionalCount,
        float additionalChance) {
      this.id = id;
      this.group = group;
      this.result = result;
      this.count = count;
      this.materials = materials;
      this.timer = timer;
      this.advBuilder = advancementBuilder;
      this.advancementId = advancementId;
      this.additionalResult = additionalResult;
      this.additionalCount = additionalCount;
      this.additionalChance = additionalChance;
    }

    @Override
    public void serialize(@NotNull JsonObject json) {

      JsonArray jsonarray = new JsonArray();
      for (Material material : this.materials) {
        jsonarray.add(material.serialize());
      }
      json.add("materials", jsonarray);

      JsonObject resultJson = new JsonObject();
      resultJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result)).toString());
      if (this.count > 1) {
        resultJson.addProperty("count", this.count);
      }
      json.add("result", resultJson);
      if (additionalResult != null) {
        JsonObject additionalResult = new JsonObject();
        additionalResult.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.additionalResult)).toString());
        if (additionalCount > 1) {
          additionalResult.addProperty("count", additionalCount);
        }
        additionalResult.addProperty("change", additionalChance);
        json.add("additional", additionalResult);
      }
      json.addProperty("cookingtime", timer);
    }

    @Override
    public @NotNull ResourceLocation getID() {
      return id;
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer() {
      return ModRecipes.ALLOY_FURNACE_RECIPE_SERIALIZER.get();
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
