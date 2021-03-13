package containers;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public abstract class EssentialsItemContainer extends EssentialsCommonContainer {

  protected final int selectedSlot;

  protected final IItemHandler inventory;

  protected EssentialsItemContainer(
      @Nullable ContainerType<?> type,
      int id,
      PlayerInventory playerInventory,
      IItemHandler inventory,
      int selectedSlot) {
    super(type, id, playerInventory);
    this.selectedSlot = selectedSlot;
    this.inventory = inventory;
  }


}
