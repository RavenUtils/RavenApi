package com.sasnos.raven_api.datagen.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Function;

public abstract class EssentialsBlockStates extends BlockStateProvider {

  public EssentialsBlockStates(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
    super(gen, modid, exFileHelper);
  }

  public void generateBlockStatesForBlock(Block block, Function<BlockState, ModelFile> modelFunction) {
    getVariantBuilder(block)
        .forAllStates(blockState -> ConfiguredModel.builder()
            .modelFile(modelFunction.apply(blockState))
            .build());
  }

  public void orientedBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
    getVariantBuilder(block)
        .forAllStates(state -> {
          Direction dir = state.getValue(BlockStateProperties.FACING);
          return ConfiguredModel.builder()
              .modelFile(modelFunc.apply(state))
              .rotationX(dir.getAxis() == Direction.Axis.Y ? dir.getAxisDirection().getStep() * -90 : 0)
              .rotationY(dir.getAxis() != Direction.Axis.Y ? ((dir.get2DDataValue() + 2) % 4) * 90 : 0)
              .build();
        });
  }
}
