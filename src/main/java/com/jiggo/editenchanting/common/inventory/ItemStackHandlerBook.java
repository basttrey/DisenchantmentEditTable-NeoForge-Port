package com.jiggo.editenchanting.common.inventory;

import net.minecraft.world.item.ItemStack;

public class ItemStackHandlerBook extends ItemStackHandlerBasic {
    public ItemStackHandlerBook(DisenchantmentMenu container) {
        super(1);
        this.container = container;
    }

    public ItemStack getEnchantedBook() {
        return getStackInSlot(0);
    }
}
