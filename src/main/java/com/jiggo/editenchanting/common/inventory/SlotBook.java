package com.jiggo.editenchanting.common.inventory;

import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.enums.EnumError;
import com.jiggo.editenchanting.common.enums.EnumType;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import com.jiggo.editenchanting.common.util.ExpressionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class SlotBook extends SlotItemHandler {
    protected final DisenchantmentMenu container;
    private final int index;

    public SlotBook(IItemHandler itemStackHandler, DisenchantmentMenu container, int index, int xPosition, int yPosition) {
        super(itemStackHandler, index, xPosition, yPosition);
        this.container = container;
        this.index = index;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (DisenchantmentTableConfig.TYPE.get() == EnumType.DISENCHANT) return false;

        ItemStack enchanting = container.getTableInventory().getEnchantingStack();
        if (enchanting.isEmpty() || !stack.is(Items.ENCHANTED_BOOK)) return false;

        if (!ExpressionUtils.canUse(container.getPlayer(), stack)) {
            container.setError(EnumError.EDIT);
            container.setErrorStack(stack.copy());
            return false;
        }

        container.setError(EnumError.NONE);
        List<Map<Holder<Enchantment>, Integer>> list = new ArrayList<>();
        list.add(EnchantmentUtils.getEnchantments(enchanting));
        list.add(EnchantmentUtils.getEnchantments(stack));

        if (list.get(1).isEmpty()) return false;
        if (list.get(0).size() == 1 && list.get(1).size() > 1) return false;

        for (Map.Entry<Holder<Enchantment>, Integer> entry : list.get(1).entrySet()) {
            Holder<Enchantment> key = entry.getKey();
            if (list.get(0).containsKey(key)) {
                int max = key.value().getMaxLevel();
                int level = entry.getValue();
                int value = list.get(0).get(key);
                if (level + value > max) return false;
            }
        }

        if (DisenchantmentTableConfig.STRICT_MODE.get() && !enchanting.is(Items.ENCHANTED_BOOK)) {
            long supported = list.get(1).keySet().stream()
                    .filter(enchant -> enchant.value().canEnchant(enchanting))
                    .count();
            if (supported != list.get(1).size()) return false;
            if (!EnchantmentUtils.isCompatible(list.get(0), list.get(1))) return false;
        }

        return true;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (!player.level().isClientSide && !stack.isEmpty()) {
            if (!player.hasInfiniteMaterials()) {
                container.getBookArrayInventory().disenchantPower(stack, DisenchantmentTableConfig.POWER.get(), player);
            }
            container.playSound();
        }
        super.onTake(player, stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = getItemHandler().getStackInSlot(index);
        if (!ExpressionUtils.canUse(container.getPlayer(), stack)) {
            container.setError(EnumError.DISENCHANT);
            container.setErrorStack(stack.copy());
            return false;
        }

        container.setError(EnumError.NONE);
        ItemStack enchanting = container.getTableInventory().getEnchantingStack();
        if (!enchanting.is(Items.ENCHANTED_BOOK)) return true;

        Map<Holder<Enchantment>, Integer> map = EnchantmentUtils.getEnchantments(enchanting);
        return map.size() != 1 || map.entrySet().iterator().next().getValue() != 1;
    }
}
