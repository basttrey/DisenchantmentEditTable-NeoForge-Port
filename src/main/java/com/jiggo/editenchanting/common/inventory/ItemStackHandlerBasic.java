package com.jiggo.editenchanting.common.inventory;

import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class ItemStackHandlerBasic extends ItemStackHandler {
    protected DisenchantmentMenu container;

    protected ItemStackHandlerBasic(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (container != null) container.onContentsChanged(this);
    }
}
