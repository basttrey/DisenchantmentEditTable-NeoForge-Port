package com.jiggo.editenchanting.common.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantmentUtils {
    private EnchantmentUtils() {}

    public static Map<Holder<Enchantment>, Integer> getEnchantments(ItemStack stack) {
        Map<Holder<Enchantment>, Integer> result = new LinkedHashMap<>();
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (var entry : enchantments.entrySet()) {
            result.put(entry.getKey(), entry.getIntValue());
        }
        return result;
    }

    public static ItemStack getEnchantedItemStack(Holder<Enchantment> enchantment, int level) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
        return stack;
    }

    public static ItemStack getEnchantedItemStack(Map<Holder<Enchantment>, Integer> map, ItemStack stack) {
        return setEnchantedItemStack(map, stack);
    }

    public static ItemStack getEnchantedItemStack(Map<Holder<Enchantment>, Integer> map) {
        return setEnchantedItemStack(map, new ItemStack(Items.ENCHANTED_BOOK));
    }

    public static List<ItemStack> getEnchantedItemStackList(Map<Holder<Enchantment>, Integer> map) {
        List<ItemStack> list = new ArrayList<>();
        map.forEach((key, value) -> list.add(getEnchantedItemStack(key, value)));
        return list;
    }

    public static ItemStack setEnchantedItemStack(Map<Holder<Enchantment>, Integer> map, ItemStack stack) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        map.forEach(mutable::set);
        EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
        return stack;
    }

    public static Holder<Enchantment> getFirstEnchantment(Map<Holder<Enchantment>, Integer> map) {
        return map.entrySet().iterator().next().getKey();
    }

    public static Entry<Holder<Enchantment>, Integer> getEnchantmentAttribute(ItemStack stack) {
        return getEnchantments(stack).entrySet().iterator().next();
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack.isEmpty() || getEnchantments(stack).isEmpty();
    }

    public static boolean isCompatible(Map<Holder<Enchantment>, Integer> first, Map<Holder<Enchantment>, Integer> second) {
        for (Holder<Enchantment> one : first.keySet()) {
            for (Holder<Enchantment> two : second.keySet()) {
                if (!one.equals(two) && !Enchantment.areCompatible(one, two)) return false;
            }
        }
        return true;
    }
}
