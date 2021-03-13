package datagen.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sasnos.ravenutils.init.ModRecipes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class BarrelRecipeBuilder {

  private final int timer;
  private Fluid fluidOutput;
  private ItemStack itemOutput;

  private FluidStack fluid;
  private Ingredient input;
  private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
  private String group;
  private boolean closedLit = false;

  public BarrelRecipeBuilder(int timer) {
    this.timer = timer;
  }

  public BarrelRecipeBuilder addFluidIntake(FluidStack fluid) {
    this.fluid = fluid;
    return this;
  }

  public BarrelRecipeBuilder addFluidOutput(Fluid fluid) {
    this.fluidOutput = fluid;
    return this;
  }

  public BarrelRecipeBuilder addItemIntake(ItemStack input) {
    this.input = Ingredient.fromStacks(input);
    return this;
  }

  public BarrelRecipeBuilder addItemOutput(ItemStack output) {
    this.itemOutput = output;
    return this;
  }

  public BarrelRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn) {
    this.advancementBuilder.withCriterion(name, criterionIn);
    return this;
  }

  public BarrelRecipeBuilder lidClosed() {
    this.closedLit = true;
    return this;
  }

  public BarrelRecipeBuilder setGroup(String groupIn) {
    this.group = groupIn;
    return this;
  }

  public void build(Consumer<IFinishedRecipe> consumerIn) {
    ResourceLocation output;
    if (this.itemOutput != null) {
      output = ForgeRegistries.ITEMS.getKey(this.itemOutput.getItem());
    } else {
      output = ForgeRegistries.FLUIDS.getKey(this.fluidOutput.getFluid());
    }
    this.build(consumerIn, output);
  }

  public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
    this.validate(id);
    this.advancementBuilder
        .withParentId(new ResourceLocation("RavenApi/src/main/java/com/sasnos/raven_api/recipes/root"))
        .withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id))
        .withRewards(AdvancementRewards.Builder.recipe(id))
        .withRequirementsStrategy(IRequirementsStrategy.OR);
    ResourceLocation advancementId;
    advancementId = (itemOutput != null)
        ? new ResourceLocation(id.getNamespace(), Objects.requireNonNull(this.itemOutput.getItem().getGroup()).getPath())
        : new ResourceLocation(id.getNamespace(), Objects.requireNonNull(this.fluidOutput.getFluid().getRegistryName()).getPath());
    consumerIn.accept(createFinishedRecipe(id, this.group == null ? "" : this.group, this.itemOutput, this.fluidOutput, this.fluid, this.input, closedLit, timer, this.advancementBuilder, advancementId));
  }

  protected IFinishedRecipe createFinishedRecipe(ResourceLocation id, String group, ItemStack output, Fluid fluidOutput, FluidStack fluid, Ingredient input, boolean closedLit, int timer, Advancement.Builder advancementBuilder, ResourceLocation advancementId) {
    return new Result(id, group == null ? "" : group, output, fluidOutput, fluid, input, closedLit, timer, advancementBuilder, advancementId);
  }


  private void validate(ResourceLocation id) {
    if (advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    if (this.itemOutput == null && this.fluidOutput == null) {
      throw new IllegalStateException("Needs either fluid or item input for recipe " + id);
    }
    if (this.fluid == null || this.fluid == FluidStack.EMPTY) {
      throw new IllegalStateException("Needs fluid for recipe " + id);
    }
    if (id.getNamespace().equals("minecraft") && ForgeRegistries.ITEMS.containsKey(id)) {
      throw new IllegalStateException("Change name of recipe to avoid problems with other mods for recipe " + id);
    }
  }

  protected static class Result implements IFinishedRecipe {
    public final ResourceLocation id;
    private final FluidStack fluid;
    private final ItemStack output;
    private final Fluid fluidOutput;
    private final Ingredient input;
    private final int timer;
    private String group;
    private final boolean closedLit;
    public final Advancement.Builder advBuilder;
    public final ResourceLocation advancementId;

    public Result(ResourceLocation id, String group, ItemStack output, Fluid fluidOutput, FluidStack fluid, Ingredient input, boolean closedLit, int timer, Advancement.Builder advancementBuilder, ResourceLocation advancementId) {
      this.id = id;
      this.group = group;
      this.output = output;
      this.fluidOutput = fluidOutput;
      this.fluid = fluid;
      this.input = input;
      this.timer = timer;
      this.closedLit = closedLit;
      this.advBuilder = advancementBuilder;
      this.advancementId = advancementId;
    }

    @Override
    public void serialize(@NotNull JsonObject json) {
      JsonArray array = new JsonArray();
      JsonObject object = new JsonObject();
      if (this.output != null) {
        object.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(output.getItem())).toString());
        object.addProperty("count", output.getCount());
        array.add(object);
      }
      object = new JsonObject();
      if (this.fluidOutput != null) {
        object.addProperty("fluid", Objects.requireNonNull(fluidOutput.getRegistryName()).toString());
        array.add(object);
      }
      json.add("outputs", array);
      array = new JsonArray();
      object = new JsonObject();
      if (fluid != null) {
        object.addProperty("fluid", Objects.requireNonNull(fluid.getFluid().getRegistryName()).toString());
        object.addProperty("count", fluid.getAmount());
        array.add(object);
      }
      if (input != null) {
        array.add(input.serialize());
      }
      json.add("inputs", array);
      json.addProperty("litClosed", closedLit);
      json.addProperty("timer", timer);

    }

    @Override
    public @NotNull ResourceLocation getID() {
      return id;
    }

    @Override
    public @NotNull IRecipeSerializer<?> getSerializer() {
      return ModRecipes.BARREL_RECIPE_SERIALIZER.get();
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
