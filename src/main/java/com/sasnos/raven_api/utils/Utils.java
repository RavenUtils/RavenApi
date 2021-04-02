package com.sasnos.raven_api.utils;

import net.minecraft.item.BucketItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class Utils {
  public static ResourceLocation createResourceLocation(String modId, String namespace) {
    return new ResourceLocation(modId, namespace);
  }

  public static FluidStack createFluidStackFromBucket(BucketItem bucket) {
    return new FluidStack(bucket.getFluid(), 1000);
  }
}
