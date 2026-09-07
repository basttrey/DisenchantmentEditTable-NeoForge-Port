package com.jiggo.editenchanting.common.inventory;

import net.minecraft.world.item.ItemStack;

public class ItemStackHandlerCost extends ItemStackHandlerBasic {
    public ItemStackHandlerCost(DisenchantmentMenu container) {
        super(1);
        this.container = container;
    }

    public ItemStack getCost() {
        return getStackInSlot(0);
    }
}
