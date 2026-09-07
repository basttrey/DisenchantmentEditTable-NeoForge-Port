package com.jiggo.editenchanting.common.inventory;

import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.enums.EnumPower;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import com.jiggo.editenchanting.common.util.ExpressionUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemStackHandlerBooks extends ItemStackHandlerBasic {
    public ItemStackHandlerBooks(DisenchantmentMenu container) {
        super(18);
        this.container = container;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!simulate && !container.getPlayer().level().isClientSide) {
            EnumPower power = DisenchantmentTableConfig.POWER.get();
            if (!container.getPlayer().hasInfiniteMaterials()) {
                disenchantPower(stack, power, container.getPlayer());
            }
            container.playSound();
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }

    public void disenchantPower(ItemStack stack, EnumPower power, Player player) {
        if (EnchantmentUtils.isEmpty(stack)) return;
        int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(stack)));
        switch (power) {
            case ITEM -> container.getCostInventory().getCost().shrink(value);
            case EXPERIENCE -> player.onEnchantmentPerformed(stack, value);
            case NONE -> { }
        }
    }

    public boolean contains(ItemStack stack) {
        for (int i = 0; i < getSlots(); i++) {
            if (stack == getStackInSlot(i)) return true;
        }
        return false;
    }
}
