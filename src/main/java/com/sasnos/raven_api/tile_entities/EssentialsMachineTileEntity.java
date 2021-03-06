package com.sasnos.raven_api.tile_entities;

import com.sasnos.raven_api.recipes.CommonRecipe;
import com.sasnos.raven_api.recipes.ICommonRecipe;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nullable;
import java.util.Set;

public abstract class EssentialsMachineTileEntity<T extends IRecipe<?>> extends EssentialsRecipeTileEntity<T> implements ITickableTileEntity, INamedContainerProvider {

  protected int burnTime;
  protected int cookingTime;
  protected int cookingTimeTotal;
  protected int recipeUsed;
  protected boolean isBurning;
  protected ICommonRecipe recipe;

  protected IIntArray teData = new IIntArray() {
    @Override
    public int get(int index) {
      switch (index) {
        case 0:
          return EssentialsMachineTileEntity.this.isBurning() ? EssentialsMachineTileEntity.this.burnTime : 0;
        case 1:
          return EssentialsMachineTileEntity.this.cookingTime;
        case 2:
          return EssentialsMachineTileEntity.this.cookingTimeTotal;
        case 3:
          return EssentialsMachineTileEntity.this.recipeUsed;
        default:
          throw new IllegalArgumentException("Invalid index: " + index);
      }
    }

    @Override
    public void set(int index, int value) {

    }

    @Override
    public int getCount() {
      return 4;
    }
  };


  public EssentialsMachineTileEntity(TileEntityType<?> tileEntityTypeIn, IRecipeType<?> recipeType) {
    super(tileEntityTypeIn, recipeType);
  }

  @Override
  public void load(BlockState state, CompoundNBT nbt) {
    burnTime = nbt.getInt("burnTime");
    cookingTime = nbt.getInt("cookingTime");
    cookingTimeTotal = nbt.getInt("cookingTimeTotal");
    recipeUsed = nbt.getInt("recipeUsed");
    isBurning = nbt.getBoolean("isBurning");
    super.load(state, nbt);
  }


  @Override
  public CompoundNBT save(CompoundNBT compound) {
    compound.putInt("burnTime", burnTime);
    compound.putInt("cookingTime", cookingTime);
    compound.putInt("cookingTimeTotal", cookingTimeTotal);
    compound.putInt("recipeUsed", recipeUsed);
    compound.putBoolean("isBurning", isBurning);
    return super.save(compound);
  }

  public boolean isBurning() {
    return isBurning && this.burnTime > 0;
  }

  public void setCookingTimeTotal(int cookingTimeTotal) {
    this.cookingTimeTotal = cookingTimeTotal;
  }

  public boolean setIsBurning() {
    if (!itemHandler.getStackInSlot(getFuelSlot()).isEmpty()) {
      isBurning = true;
      if (handleBurning(itemHandler.getStackInSlot(getFuelSlot()))) {
        setChanged();
      }
      this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
      return true;
    }
    return false;

  }

  public abstract int getFuelSlot();

  @Override
  public T getRecipe(ItemStack stack) {
    if (stack == ItemStack.EMPTY) return null;

    Set<IRecipe<?>> recipes = findRecipeByType(recipeType, this.level);
    RecipeWrapper wrapper = new RecipeWrapper(itemHandler);
    return matching(recipes, wrapper, level);
  }

  @Override
  public T getRecipeFromOutput(ItemStack result) {
    if (result == ItemStack.EMPTY) return null;

    Set<IRecipe<?>> recipes = findRecipeByType(recipeType, this.level);

    return matchingOutput(recipes, result, level);
  }

  protected abstract T matchingOutput(Set<IRecipe<?>> recipes, ItemStack result, World world);

  protected abstract T matching(Set<IRecipe<?>> recipes, RecipeWrapper wrapper, World world);

  protected boolean handleBurning(ItemStack fuel) {
    boolean isDirty = false;
    burnTime = ForgeHooks.getBurnTime(fuel);
    recipeUsed = burnTime;
    if (isBurning()) {
      isDirty = true;
      if (fuel.hasContainerItem()) {
        itemHandler.setStackInSlot(getFuelSlot(), fuel.getContainerItem());
      } else if (!fuel.isEmpty()) {
        fuel.shrink(1);
        if (fuel.isEmpty()) {
          itemHandler.setStackInSlot(getFuelSlot(), fuel.getContainerItem());
        }
      }
    }
    return isDirty;
  }

  protected void smelt(@javax.annotation.Nullable IRecipe<?> recipe) {
    if (recipe != null && this.canSmelt(recipe)) {
      ItemStack input = this.itemHandler.getStackInSlot(0);
      ItemStack output = recipe.getResultItem();
      if (recipe instanceof CommonRecipe) {
        output = ((CommonRecipe) recipe).getOutput().get(0);
      }
      ItemStack outputSlot = this.itemHandler.getStackInSlot(2);
      if (outputSlot.isEmpty()) {
        this.itemHandler.setStackInSlot(2, output.copy());
      } else if (outputSlot.getItem() == output.getItem()) {
        outputSlot.grow(output.getCount());
      }

      if (input.getItem() == Blocks.WET_SPONGE.asItem() && !this.itemHandler.getStackInSlot(1).isEmpty() && this.itemHandler.getStackInSlot(1).getItem() == Items.BUCKET) {
        this.itemHandler.setStackInSlot(1, new ItemStack(Items.WATER_BUCKET));
      }

      input.shrink(1);
    }
  }

  protected boolean canSmelt(@Nullable IRecipe<?> recipeIn) {
    if (!this.itemHandler.getStackInSlot(0).isEmpty() && recipeIn != null) {
      ItemStack output = recipeIn.getResultItem();
      if (output.isEmpty()) {
        return false;
      } else {
        ItemStack outputSlot = this.itemHandler.getStackInSlot(2);
        if (outputSlot.isEmpty()) {
          return true;
        } else if (!outputSlot.sameItem(output)) {
          return false;
        } else {
          return outputSlot.getCount() + output.getCount() <= output.getMaxStackSize(); // Forge fix: make furnace respect stack sizes in furnace recipes
        }
      }
    } else {
      return false;
    }
  }


  @Override
  public void tick() {
    boolean localIsBurning = isBurning();
    boolean isDirty = false;

    if (this.isBurning()) {
      --this.burnTime;
    }

    if (!level.isClientSide) {
      ItemStack fuel = itemHandler.getStackInSlot(getFuelSlot());
      if (isBurning() || !fuel.isEmpty()) {
        ICommonRecipe recipe = (ICommonRecipe) getRecipe(itemHandler.getStackInSlot(0));
        if (this.recipe != recipe) {
          cookingTime = 0;
          this.recipe = recipe;
        }
        if (!this.isBurning()) {
          isDirty = handleBurning(fuel);
        }
        if (this.isBurning() && this.canSmelt(recipe)) {
          ++cookingTime;
          if (cookingTime == cookingTimeTotal) {
            cookingTime = 0;
            cookingTimeTotal = recipe.getTimer();
            smelt(recipe);
            isDirty = true;
          }
        } else {
          cookingTime = 0;
        }
        if (cookingTimeTotal == 0 && !itemHandler.getStackInSlot(0).isEmpty() && recipe != null) {
          cookingTimeTotal = recipe.getTimer();
        }
      } else if (!this.isBurning() && this.cookingTime > 0) {
        this.cookingTime = MathHelper.clamp(this.cookingTime - 2, 0, this.cookingTimeTotal);
      }

      if (localIsBurning != this.isBurning()) {
        if (!this.isBurning()) isBurning = false;
        isDirty = true;
        this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
      }
    }

    if (isDirty) {
      setChanged();
    }
  }

  @Override
  public ITextComponent getDisplayName() {
    return new TranslationTextComponent(getScreenName());
  }

  protected abstract String getScreenName();
}


