package com.jiggo.editenchanting.common.inventory;

import net.minecraft.world.item.ItemStack;

public class ItemStackHandlerEnchant extends ItemStackHandlerBasic {
    public ItemStackHandlerEnchant(DisenchantmentMenu container) {
        super(1);
        this.container = container;
    }

    public ItemStack getEnchantingStack() {
        return getStackInSlot(0);
    }
}
