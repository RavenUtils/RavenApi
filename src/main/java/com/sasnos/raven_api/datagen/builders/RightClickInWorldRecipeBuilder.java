package datagen.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sasnos.ravenutils.api.utils.blockingridient.BlockIngredient;
import com.sasnos.ravenutils.init.ModRecipes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RightClickInWorldRecipeBuilder {

    private Ingredient input;
    private NonNullList<ItemStack> output = NonNullList.create();
    private BlockIngredient block;

    private final Advancement.Builder advancementBuilder = Advancement.Builder.builder();
    private String group;


    public RightClickInWorldRecipeBuilder(Ingredient input, ItemStack output, BlockIngredient block) {

        this.input = input;
        this.output.add(output);
        this.block = block;
    }

    public RightClickInWorldRecipeBuilder(Ingredient input, NonNullList<ItemStack> output, BlockIngredient block) {
        this.input = input;
        this.output = output;
        this.block = block;
    }

    public RightClickInWorldRecipeBuilder addOutput(ItemStack addOutput) {
        output.add(addOutput);
        return this;
    }

    public RightClickInWorldRecipeBuilder addCriterion(String name, ICriterionInstance criterionIn) {
        this.advancementBuilder.withCriterion(name, criterionIn);
        return this;
    }

    public RightClickInWorldRecipeBuilder setGroup(String groupIn) {
        this.group = groupIn;
        return this;
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        this.build(consumer, ForgeRegistries.ITEMS.getKey(output.get(0).getItem()));
    }

    public void build(Consumer<IFinishedRecipe> consumer, String id) {
        this.build(consumer, new ResourceLocation(id));
    }

    public void build(Consumer<IFinishedRecipe> consumer, ResourceLocation id) {
        this.validate(id);
        this.advancementBuilder
                .withParentId(new ResourceLocation("RavenApi/src/main/java/com/sasnos/raven_api/recipes/root"))
                .withCriterion("has_the_recipe", RecipeUnlockedTrigger.create(id))
                .withRewards(AdvancementRewards.Builder.recipe(id))
                .withRequirementsStrategy(IRequirementsStrategy.OR);
        ResourceLocation advancementId = new ResourceLocation(id.getNamespace(), "RavenApi/src/main/java/com/sasnos/raven_api/recipes/" + this.output.get(0).getItem().getGroup().getPath() + "/" + id.getPath());
        consumer.accept(createFinishedRecipe(
                id,
                this.group == null ? "" : this.group,
                this.output, this.input, this.block,
                this.advancementBuilder,
                advancementId));
    }

    private IFinishedRecipe createFinishedRecipe(ResourceLocation id, String group, NonNullList<ItemStack> output, Ingredient input, BlockIngredient block, Advancement.Builder advancementBuilder, ResourceLocation advancementId) {
        return new Result(id, group, output, input, block, advancementBuilder, advancementId);
    }

    private void validate(ResourceLocation id) {
        if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    protected static class Result implements IFinishedRecipe {


        private final ResourceLocation id;
        private final String group;
        private final NonNullList<ItemStack> output;
        private final Ingredient input;
        private final BlockIngredient block;
        public final Advancement.Builder advBuilder;
        public final ResourceLocation advancementId;

        public Result(ResourceLocation id, String group, NonNullList<ItemStack> output, Ingredient input, BlockIngredient block, Advancement.Builder advancementBuilder, ResourceLocation advancementId) {

            this.id = id;
            this.group = group;
            this.output = output;
            this.input = input;
            this.block = block;
            this.advBuilder = advancementBuilder;
            this.advancementId = advancementId;
        }

        @Override
        public void serialize(JsonObject json) {
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

            JsonElement blockIngredients = this.block.serialize();
            json.add("blockIngredient", blockIngredients);

            JsonArray array = new JsonArray();
            for (ItemStack stack : this.output) {
                JsonObject jo = new JsonObject();
                jo.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                jo.addProperty("count", stack.getCount());
                array.add(jo);
            }
            json.add("result", array);

        }

        @Override
        public ResourceLocation getID() {
            return id;
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return ModRecipes.RIGHT_CLICK_IN_WORLD_RECIPE_SERIALIZER.get();
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
