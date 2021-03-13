package utils.blockingridient;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fml.DatagenModLoader;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BlockIngredient extends Ingredient {
    public static final BlockIngredient EMPTY = new BlockIngredient(Stream.empty());
    private final IBlockList[] acceptedBlocks;
    private Block[] matchingBlocks;
    private final boolean isSimple;

    protected BlockIngredient(Stream<? extends IBlockList> blockLists) {
        super(Stream.empty());
        this.acceptedBlocks = blockLists.toArray(IBlockList[]::new);
        this.isSimple = !DatagenModLoader.isRunningDataGen();
    }

    public Block[] getMatchingBlocks() {
        this.determineMatchingBlocks();
        return this.matchingBlocks;
    }

    private void determineMatchingBlocks() {
        if (this.matchingBlocks == null) {
            this.matchingBlocks = Arrays.stream(this.acceptedBlocks).flatMap((ingredientList) -> {
                return ingredientList.getStacks().stream();
            }).distinct().toArray(Block[]::new);
        }

    }

    public boolean test(@Nullable Block p_test_1_) {
        if (p_test_1_ == null) {
            return false;
        } else {
            this.determineMatchingBlocks();
            if (this.matchingBlocks.length == 0) {
                return p_test_1_ == Blocks.AIR;
            } else {
                for (Block block : this.matchingBlocks) {
                    if (block == p_test_1_) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public JsonElement serialize() {
        if (this.acceptedBlocks.length == 1) {
            return this.acceptedBlocks[0].serialize();
        } else {
            JsonArray jsonarray = new JsonArray();

            for (IBlockList blockList : this.acceptedBlocks) {
                jsonarray.add(blockList.serialize());
            }

            return jsonarray;
        }
    }

    public boolean hasNoMatchingItems() {
        return this.acceptedBlocks.length == 0 && (this.matchingBlocks == null || this.matchingBlocks.length == 0);
    }

    protected void invalidate() {
        this.matchingBlocks = null;
    }

    public boolean isSimple() {
        return isSimple || this == EMPTY;
    }

    public IIngredientSerializer<? extends BlockIngredient> getSerializer() {
        return BlockIngredientSerializer.INSTANCE;
    }

    public static BlockIngredient fromBlockListStream(Stream<? extends IBlockList> stream) {
        BlockIngredient ingredient = new BlockIngredient(stream);
        return ingredient.acceptedBlocks.length == 0 ? EMPTY : ingredient;
    }

    public static BlockIngredient fromBlocks(Block... blocks) {
        return fromBlocks(Arrays.stream(blocks));
    }

    public static BlockIngredient fromBlocks(Stream<Block> stacks) {
        return fromBlockListStream(stacks.filter((block) -> {
            return block != Blocks.AIR;
        }).map(SingleItemList::new));
    }

    public static BlockIngredient fromBlockTag(ITag<Block> tagIn) {
        return fromBlockListStream(Stream.of(new TagList(tagIn)));
    }

    public static BlockIngredient read(PacketBuffer buffer) {
        int i = buffer.readVarInt();
        return fromBlockListStream(Stream.generate(() -> {
            return new SingleItemList(ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation()));
        }).limit((long) i));
    }

    public static BlockIngredient deserialize(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            if (json.isJsonObject()) {
                return fromBlockListStream(Stream.of(deserializeBlockList(json.getAsJsonObject())));
            } else if (json.isJsonArray()) {
                JsonArray jsonarray = json.getAsJsonArray();
                if (jsonarray.size() == 0) {
                    throw new JsonSyntaxException("Block array cannot be empty, at least one item must be defined");
                } else {
                    return fromBlockListStream(StreamSupport.stream(jsonarray.spliterator(), false).map((element) -> {
                        return deserializeBlockList(JSONUtils.getJsonObject(element, "block"));
                    }));
                }
            } else {
                throw new JsonSyntaxException("Expected block to be object or array of objects");
            }
        } else {
            throw new JsonSyntaxException("Block cannot be null");
        }
    }

    public static IBlockList deserializeBlockList(JsonObject json) {
        if (json.has("block") && json.has("tag")) {
            throw new JsonParseException("An block ingredient entry is either a tag or an Block, not both");
        } else if (json.has("block")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(JSONUtils.getString(json, "block"));
            Block block = ForgeRegistries.BLOCKS.getValue(resourcelocation1);
            if (block == null) throw new JsonSyntaxException("Unknown item '" + resourcelocation1 + "'");
            return new SingleItemList(block);
        } else if (json.has("tag")) {
            ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(json, "tag"));
            ITag<Block> itag = TagCollectionManager.getManager().getBlockTags().get(resourcelocation);
            if (itag == null) {
                throw new JsonSyntaxException("Unknown item tag '" + resourcelocation + "'");
            } else {
                return new TagList(itag);
            }
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    //Merges several vanilla Ingredients together. As a quirk of how the json is structured, we can't tell if its a single Ingredient type or multiple so we split per item and re-merge here.
    //Only public for internal use, so we can access a private field in here.
    public static BlockIngredient mergeBlocks(Collection<BlockIngredient> parts) {
        return fromBlockListStream(parts.stream().flatMap(i -> Arrays.stream(i.acceptedBlocks)));
    }

    public interface IBlockList {
        Collection<Block> getStacks();

        JsonObject serialize();
    }

    public static class SingleItemList implements IBlockList {
        private final Block block;

        public SingleItemList(Block stackIn) {
            this.block = stackIn;
        }

        public Collection<Block> getStacks() {
            return Collections.singleton(this.block);
        }

        public JsonObject serialize() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("block", ForgeRegistries.BLOCKS.getKey(this.block.getBlock()).toString());
            return jsonobject;
        }
    }

    public static class TagList implements IBlockList {
        private final ITag<Block> tag;

        public TagList(ITag<Block> tagIn) {
            this.tag = tagIn;
        }

        public Collection<Block> getStacks() {
            List<Block> list = Lists.newArrayList();

            for (Block block : this.tag.getAllElements()) {
                list.add(block);
            }

            if (list.size() == 0 && !net.minecraftforge.common.ForgeConfig.SERVER.treatEmptyTagsAsAir.get()) {
                list.add(net.minecraft.block.Blocks.BARRIER);
            }
            return list;
        }

        public JsonObject serialize() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("tag", TagCollectionManager.getManager().getBlockTags().getValidatedIdFromTag(this.tag).toString());
            return jsonobject;
        }
    }
}
