package datagen.loot_table;

import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootTable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class EssentialsLootTableProvider<T> implements ILootTableProvider<T> {

  protected LootParameterSet parameterSet;
  protected Map<T, LootTable.Builder> lootTables = new HashMap<>();

  public EssentialsLootTableProvider(Consumer<Map<T, LootTable.Builder>> consumer, LootParameterSet parameterSet) {
    this.parameterSet = parameterSet;
    consumer.accept(getTables());
  }

  public LootTable.Builder addParameterSet(LootTable.Builder builder) {
    return builder.setParameterSet(parameterSet);
  }
}
