/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Mar 17, 2014, 8:30:41 PM (GMT)]
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.common.item.ItemLens;
import vazkii.botania.common.item.ModItems;

public class CompositeLensRecipe implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting var1, World var2) {
		boolean foundLens = false;
		boolean foundSecondLens = false;
		boolean foundSlimeball = false;

		for(int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
			if(stack != null) {
				if(stack.getItem() instanceof ILens && !foundSecondLens) {
					if(foundLens)
						foundSecondLens = true;
					else foundLens = true;
				} else if(stack.getItem() == Items.slime_ball)
					foundSlimeball = true;
				else return false; // Found an invalid item, breaking the recipe
			}
		}

		return foundSecondLens && foundSlimeball;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		ItemStack lens = null;
		ItemStack secondLens = null;

		for(int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
			if(stack != null) {
				if(stack.getItem() instanceof ILens)
					if(lens == null)
						lens = stack;
					else secondLens = stack;
			}
		}

		if(lens.getItem() instanceof ILens) {
			ILens lensItem = (ILens) lens.getItem();
			if(secondLens == null || !lensItem.canCombineLenses(lens, secondLens) || lensItem.getCompositeLens(lens) != null || lensItem.getCompositeLens(secondLens) != null)
				return null;

			ItemStack lensCopy = lens.copy();
			((ItemLens) ModItems.lens).setCompositeLens(lensCopy, secondLens);

			return lensCopy;
		}

		return null;
	}

	@Override
	public int getRecipeSize() {
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}