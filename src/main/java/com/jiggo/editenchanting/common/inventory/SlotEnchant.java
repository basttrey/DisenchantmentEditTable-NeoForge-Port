package com.jiggo.editenchanting.common.inventory;

import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SlotEnchant extends SlotItemHandler {
    public SlotEnchant(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (DisenchantmentTableConfig.CREATIVE_MODE.get()) return true;
        return (stack.is(Items.ENCHANTED_BOOK) && !EnchantmentUtils.getEnchantments(stack).isEmpty())
                || stack.isEnchantable()
                || stack.isEnchanted();
    }
}
