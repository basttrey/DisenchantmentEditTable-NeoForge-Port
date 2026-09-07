package com.jiggo.editenchanting.common.control;

import com.jiggo.editenchanting.common.enums.EnumBookStatus;
import com.jiggo.editenchanting.common.enums.EnumMode;
import com.jiggo.editenchanting.common.inventory.DisenchantmentMenu;
import com.jiggo.editenchanting.common.inventory.ItemStackHandlerBook;
import com.jiggo.editenchanting.common.inventory.ItemStackHandlerBooks;
import com.jiggo.editenchanting.common.inventory.ItemStackHandlerEnchant;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantedBookLogic {
    private static final int PAGE_SIZE = 18;

    private final DisenchantmentMenu container;
    private final ItemStackHandlerEnchant tableInventory;
    private final ItemStackHandlerBook bookInventory;
    private final ItemStackHandlerBooks bookArrayInventory;

    private Map<Holder<Enchantment>, Integer> enchantments = new LinkedHashMap<>();
    private int index = 1;
    private EnumMode mode = EnumMode.DEFAULT;
    private EnumBookStatus status = EnumBookStatus.OPEN;
    private boolean edit;
    private boolean size;
    private boolean update;

    public EnchantedBookLogic(DisenchantmentMenu container) {
        this.container = container;
        this.tableInventory = container.getTableInventory();
        this.bookInventory = container.getBookInventory();
        this.bookArrayInventory = container.getBookArrayInventory();
    }

    public void update() {
        ItemStack stack = getEnchantingStack();
        switch (mode) {
            case DEFAULT, NULL -> {
                if (enchantments.isEmpty()) {
                    enchantments = getEnchantments(stack);
                    index = 1;
                }

                if (enchantments.size() == 1 && stack.is(Items.ENCHANTED_BOOK)) {
                    mode = EnumMode.BOOK_EDIT;
                }

                if (!enchantments.isEmpty()) {
                    if (mode != EnumMode.BOOK_EDIT) {
                        mode = stack.is(Items.ENCHANTED_BOOK) ? EnumMode.BOOK : EnumMode.ENCHANT;
                    }

                    status = EnumBookStatus.CLOSE;
                    updateBookArrayStack(mode == EnumMode.BOOK_EDIT);
                    status = EnumBookStatus.OPEN;
                } else if (!stack.is(Items.ENCHANTED_BOOK)) {
                    mode = EnumMode.NULL;
                }
            }
            default -> {
                enchantments = getEnchantments(stack);
                index = 1;
                status = EnumBookStatus.CLOSE;
                clearBookArrayStack();
                status = EnumBookStatus.OPEN;
                mode = EnumMode.DEFAULT;
                update();
            }
        }
    }

    public void close() {
        if (!enchantments.isEmpty()) {
            status = EnumBookStatus.CLOSE;
            enchantments = new LinkedHashMap<>();
            clearBookArrayStack();
            mode = EnumMode.DEFAULT;
            status = EnumBookStatus.OPEN;
        }
    }

    public void bookArray() {
        if (edit || size || status == EnumBookStatus.CLOSE || mode == EnumMode.DEFAULT) return;

        if (mode != EnumMode.BOOK_EDIT) {
            updateEnchantments();
            updateEnchantingStack();
            if (mode == EnumMode.NULL) mode = EnumMode.ENCHANT;
            if (size) updateBookArrayStack();
            size = false;
        } else if (isUpdate()) {
            updateSimpleEnchantments();
            updateEnchantingStack();
            update = false;
        }
    }

    public boolean isUpdate() {
        return !update;
    }

    public void previous() {
        if (enchantments.size() - 1 > PAGE_SIZE - 2) {
            status = EnumBookStatus.CLOSE;
            int i = index - 1;
            index = i == 0 ? (int)Math.ceil((enchantments.size() + 2.0F) / PAGE_SIZE) : i;
            updateBookArrayStack();
            status = EnumBookStatus.OPEN;
        }
    }

    public void next() {
        if (enchantments.size() - 1 > PAGE_SIZE - 2) {
            status = EnumBookStatus.CLOSE;
            int last = (int)Math.ceil((enchantments.size() + 2.0F) / PAGE_SIZE);
            int i = index + 1;
            index = i == last + 1 ? 1 : i;
            updateBookArrayStack();
            status = EnumBookStatus.OPEN;
        }
    }

    public void take() {
        updateBookStack();
        enchantments = new LinkedHashMap<>();
        clearBookArrayStack();
        updateEnchantingStack();
        mode = EnumMode.NULL;
    }

    public void updateEnchantments() {
        int count = 1;
        int start = index * PAGE_SIZE - PAGE_SIZE + 1;
        int limit = index * PAGE_SIZE;
        Map<Holder<Enchantment>, Integer> before = new LinkedHashMap<>();
        Map<Holder<Enchantment>, Integer> page = new LinkedHashMap<>();
        Map<Holder<Enchantment>, Integer> after = new LinkedHashMap<>();

        for (int i = 0; i < bookArrayInventory.getSlots(); i++) {
            ItemStack stack = bookArrayInventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            Map<Holder<Enchantment>, Integer> map = getEnchantments(stack);
            if (map.size() > 1) {
                size = true;
            } else if (!map.isEmpty()) {
                Holder<Enchantment> enchantment = EnchantmentUtils.getFirstEnchantment(map);
                if (page.containsKey(enchantment)) {
                    page.replace(enchantment, page.get(enchantment) + map.get(enchantment));
                    size = true;
                    continue;
                }
            }
            page.putAll(map);
        }

        for (Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            if (count < start) before.put(entry.getKey(), entry.getValue());
            else if (count > limit) after.put(entry.getKey(), entry.getValue());
            count++;
        }

        Map<Holder<Enchantment>, Integer> merged = new LinkedHashMap<>();
        merged.putAll(before);
        merged.putAll(page);
        merged.putAll(after);
        enchantments = merged;
    }

    public void updateSimpleEnchantments() {
        if (enchantments.isEmpty() && mode != EnumMode.BOOK_EDIT) return;

        update = true;
        List<Integer> integers = new ArrayList<>();
        List<Integer> occupiedSlots = new ArrayList<>();

        for (int i = 0; i < bookArrayInventory.getSlots(); i++) {
            ItemStack stack = bookArrayInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Map<Holder<Enchantment>, Integer> map = getEnchantments(stack);
                if (!map.isEmpty()) {
                    integers.add(map.entrySet().iterator().next().getValue());
                    occupiedSlots.add(i);
                }
            }
        }

        if (enchantments.isEmpty()) return;

        Entry<Holder<Enchantment>, Integer> entry = enchantments.entrySet().iterator().next();
        Holder<Enchantment> key = entry.getKey();
        int level = entry.getValue();
        Map<Holder<Enchantment>, Integer> tableMap = getEnchantments(tableInventory.getEnchantingStack());
        if (tableMap.isEmpty() || tableMap.entrySet().iterator().next().getValue() != level) return;

        if (level > integers.size()) {
            int num = 0;
            for (int j = 1; j <= level; j++) {
                if (!integers.contains(j)) num = j;
            }

            if (level - num != 0) {
                enchantments.replace(key, level - num);
                updateBookArrayStack(true);
            } else {
                enchantments.remove(key);
                clearBookArrayStack();
            }
            updateBookArrayStack(true);
            return;
        }

        for (int j = 0; j < bookArrayInventory.getSlots(); j++) {
            ItemStack stack = bookArrayInventory.getStackInSlot(j);
            if (!stack.isEmpty()) {
                Map<Holder<Enchantment>, Integer> map = getEnchantments(stack);
                if (!map.isEmpty()) {
                    Entry<Holder<Enchantment>, Integer> e = map.entrySet().iterator().next();
                    if (!e.getKey().equals(key)) enchantments.put(e.getKey(), e.getValue());
                }
            }
        }

        int num = 0;
        if (level == integers.size()) {
            outer:
            for (int k = 1; k <= level; k++) {
                if (!integers.contains(k)) {
                    for (int slot : occupiedSlots) {
                        Map<Holder<Enchantment>, Integer> map = getEnchantments(bookArrayInventory.getStackInSlot(slot));
                        if (map.isEmpty()) continue;
                        Entry<Holder<Enchantment>, Integer> e = map.entrySet().iterator().next();
                        if (e.getValue() == k && key.equals(e.getKey())) {
                            num = k;
                            break outer;
                        }
                    }
                }
            }
            enchantments.replace(key, level - num);
        }

        if (num == 0 && enchantments.size() == 1) {
            int all = integers.stream().mapToInt(Integer::intValue).sum();
            int expected = 0;
            for (int k = 1; k <= level; k++) expected += k;
            int combined = level + all - expected;
            if (key.value().getMaxLevel() >= combined && combined > 0) {
                enchantments.replace(key, combined);
                edit = true;
                updateBookArrayStack(true);
                edit = false;
            }
            return;
        }

        edit = true;
        updateEnchantingStack();
        if (enchantments.size() > 1) {
            updateBookArrayStack();
            mode = EnumMode.BOOK;
        }
        edit = false;
    }

    public void updateEnchantingStack() {
        ItemStack stack = getEnchantingStack();
        if (stack.is(Items.ENCHANTED_BOOK) && enchantments.isEmpty()) {
            tableInventory.setStackInSlot(0, ItemStack.EMPTY);
        } else if (!stack.isEmpty()) {
            EnchantmentUtils.setEnchantedItemStack(enchantments, stack);
        }
        container.broadcastChanges();
    }

    public void updateBookStack() {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentUtils.setEnchantedItemStack(enchantments, stack);
        bookInventory.setStackInSlot(0, stack);
        container.broadcastChanges();
    }

    public void updateBookArrayStack() {
        updateBookArrayStack(false);
    }

    public void updateBookArrayStack(boolean simple) {
        List<ItemStack> stacks = new ArrayList<>();
        if (simple) {
            if (enchantments.isEmpty()) return;
            Holder<Enchantment> key = getFirstEnchantment();
            int level = enchantments.get(key);
            for (int i = 1; i <= level; i++) stacks.add(EnchantmentUtils.getEnchantedItemStack(key, i));
        } else {
            stacks = getBookStack();
        }

        for (int i = 0; i < bookArrayInventory.getSlots(); i++) {
            bookArrayInventory.setStackInSlot(i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        }
        container.broadcastChanges();
    }

    public void clearBookArrayStack() {
        for (int i = 0; i < bookArrayInventory.getSlots(); i++) {
            bookArrayInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        container.broadcastChanges();
    }

    public List<ItemStack> getBookStack() {
        Map<Holder<Enchantment>, Integer> page = new LinkedHashMap<>();
        int count = 1;
        int start = index * PAGE_SIZE - PAGE_SIZE + 1;
        int limit = index * PAGE_SIZE;

        for (Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            if (count >= start && count <= limit) page.put(entry.getKey(), entry.getValue());
            count++;
        }
        return EnchantmentUtils.getEnchantedItemStackList(page);
    }

    public ItemStack getEnchantingStack() {
        return tableInventory.getEnchantingStack();
    }

    public Map<Holder<Enchantment>, Integer> getEnchantments(ItemStack stack) {
        return EnchantmentUtils.getEnchantments(stack);
    }

    public Entry<Holder<Enchantment>, Integer> getFirstEnchantments(ItemStack stack) {
        return getEnchantments(stack).entrySet().iterator().next();
    }

    public Holder<Enchantment> getFirstEnchantment() {
        return enchantments.entrySet().iterator().next().getKey();
    }

    public EnumMode getMode() {
        return mode;
    }

    public EnumBookStatus getStatus() {
        return status;
    }

    public boolean isEmpty() {
        return enchantments.isEmpty();
    }
}
