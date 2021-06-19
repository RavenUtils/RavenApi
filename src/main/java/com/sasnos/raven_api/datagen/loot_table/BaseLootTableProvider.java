package com.sasnos.raven_api.datagen.loot_table;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.loot.functions.SetContents;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BaseLootTableProvider extends LootTableProvider {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  protected final Set<Map<Block, LootTable.Builder>> lootTables = new HashSet<>();
  public static Map<ResourceLocation, LootTable> tables = new HashMap<>();
  protected final DataGenerator generator;
  private String modid;

  public BaseLootTableProvider(DataGenerator dataGeneratorIn, String modidIn) {
    super(dataGeneratorIn);
    this.generator = dataGeneratorIn;
    modid = modidIn;
  }

  protected abstract void addTables();

  public static LootTable.Builder createStandardBlockTable(String name, Block block) {
    LootPool.Builder builder = LootPool.lootPool()
        .name(name)
        .setRolls(ConstantRange.exactly(1))
        .add(ItemLootEntry.lootTableItem(block)
            .apply(CopyName.copyName(CopyName.Source.BLOCK_ENTITY))
            .apply(SetContents.setContents()
                .withEntry(DynamicLootEntry.dynamicEntry(new ResourceLocation("minecraft", "contents"))))
        );
    return LootTable.lootTable().withPool(builder).setParamSet(LootParameterSets.BLOCK);
  }

  @Override
  public void run(DirectoryCache cache) {
    addTables();

    lootTables.forEach(blockBuilderMap -> {
      for (Map.Entry<Block, LootTable.Builder> entry : blockBuilderMap.entrySet()) {
        tables.put(entry.getKey().getLootTable(), entry.getValue().build());
      }
    });

    writeTables(cache, tables);
  }

  private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
    Path outputFolder = this.generator.getOutputFolder();
    tables.forEach((key, lootTable) -> {
      Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
      try {
        IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable), path);
      } catch (IOException e) {
        LOGGER.error("Couldn't write loot table {}", path, (Object) e);
      }
    });
  }

  @Override
  public String getName() {
    return modid + " LootTables";
  }
}
