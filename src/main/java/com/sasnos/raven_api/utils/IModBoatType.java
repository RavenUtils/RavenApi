package utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IModBoatType {

  public String getName();

  public ResourceLocation getTexture();

  public ItemStack asPlank();

  public Item getItem();
}
