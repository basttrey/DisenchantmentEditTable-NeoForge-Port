package com.jiggo.editenchanting.common.inventory;

import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SlotCost extends SlotItemHandler {
    public SlotCost(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public Item getCostItem() {
        return DisenchantmentTableConfig.getCostItem();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getCostItem().getDefaultInstance().getMaxStackSize();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(getCostItem());
    }
}
