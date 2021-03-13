package utils.blockingridient;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.stream.Stream;

public class BlockIngredientSerializer implements IIngredientSerializer<BlockIngredient> {

    public static final BlockIngredientSerializer INSTANCE = new BlockIngredientSerializer();

    @Override
    public BlockIngredient parse(PacketBuffer buffer) {
        return BlockIngredient.fromBlockListStream(Stream.generate(() ->
                new BlockIngredient.SingleItemList(ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation())))
                .limit(buffer.readVarInt()));
    }

    @Override
    public BlockIngredient parse(JsonObject json) {
        return BlockIngredient.fromBlockListStream(Stream.of(BlockIngredient.deserializeBlockList(json)));
    }

    @Override
    public void write(PacketBuffer buffer, BlockIngredient ingredient) {
        Block[] blocks = ingredient.getMatchingBlocks();
        buffer.writeVarInt(blocks.length);

        for (Block block : blocks)
            buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)));
    }
}
