package com.jiggo.editenchanting.common.util;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.enums.EnumPower;
import com.jiggo.editenchanting.common.inventory.DisenchantmentMenu;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.slf4j.Logger;

public final class ExpressionUtils {
    private static final Logger LOGGER = DisenchantmentEditTable.getLogger();

    private ExpressionUtils() {}

    public static int getRepairCost(ItemStack stack) {
        return stack.getOrDefault(DataComponents.REPAIR_COST, 0);
    }

    public static int getCost(ItemStack stack) {
        Map<Holder<Enchantment>, Integer> map = EnchantmentUtils.getEnchantments(stack);
        return getCostFromExpression(DisenchantmentTableConfig.POWER_EXPRESSION.get(), map, getRepairCost(stack));
    }

    public static int getCost(int level, int count, int repair) {
        return evaluate(DisenchantmentTableConfig.POWER_EXPRESSION.get(), level, count, repair);
    }

    public static int getCost(Map<Holder<Enchantment>, Integer> map, int repair) {
        return getCostFromExpression(DisenchantmentTableConfig.REPORT_EXPRESSION.get(), map, repair);
    }

    private static int getCostFromExpression(String expression, Map<Holder<Enchantment>, Integer> map, int repair) {
        int level = map.values().stream().mapToInt(Integer::intValue).sum();
        return evaluate(expression, level, map.size(), repair);
    }

    public static boolean canUse(Player player) {
        return player.hasInfiniteMaterials() || DisenchantmentTableConfig.POWER.get() == EnumPower.NONE;
    }

    public static boolean canUse(Player player, ItemStack stack) {
        if (canUse(player)) return true;
        if (!EnchantmentUtils.isEmpty(stack) && DisenchantmentTableConfig.POWER.get() != EnumPower.NONE) {
            return canUse(player, Math.max(0, (int)Math.floor(getCost(stack))));
        }
        return false;
    }

    public static boolean canUse(Player player, int value) {
        EnumPower power = DisenchantmentTableConfig.POWER.get();
        return switch (power) {
            case ITEM -> player.containerMenu instanceof DisenchantmentMenu menu && menu.getCostCount() >= value;
            case EXPERIENCE -> player.experienceLevel >= value;
            case NONE -> true;
        };
    }

    public static boolean canReport(Player player, ItemStack stack) {
        Map<Holder<Enchantment>, Integer> map = EnchantmentUtils.getEnchantments(stack);
        if (map.isEmpty()) return false;
        if (canUse(player)) return true;
        int value = Math.max(0, (int)Math.floor(getCost(map, getRepairCost(stack))));
        return canUse(player, value);
    }

    public static void setSlotItem(DisenchantmentMenu menu, Slot slot, ItemStack stack) {
        if (!menu.getPlayer().hasInfiniteMaterials() && !EnchantmentUtils.isEmpty(stack)) {
            int value = Math.max(0, (int)Math.floor(getCost(stack)));
            switch (DisenchantmentTableConfig.POWER.get()) {
                case ITEM -> menu.getCostInventory().getCost().shrink(value);
                case EXPERIENCE -> menu.getPlayer().onEnchantmentPerformed(stack, value);
                case NONE -> { }
            }
        }
        menu.playSound();
        slot.set(stack);
    }

    public static int evaluate(String expression, int level, int count, int repair) {
        try {
            String expanded = expression
                    .replaceAll("\\blevel\\b", Integer.toString(level))
                    .replaceAll("\\bcount\\b", Integer.toString(count))
                    .replaceAll("\\brepair\\b", Integer.toString(repair));
            double value = new ArithmeticParser(expanded).parse();
            if (!Double.isFinite(value)) return 0;
            return (int)value;
        } catch (RuntimeException ex) {
            LOGGER.error("Invalid disenchantment cost expression '{}': {}", expression, ex.getMessage());
            return 0;
        }
    }

    private static final class ArithmeticParser {
        private final String text;
        private int pos;
        ArithmeticParser(String text) { this.text = text.replace(" ", ""); }
        double parse() {
            double value = expression();
            if (pos != text.length()) throw new IllegalArgumentException("unexpected token at " + pos);
            return value;
        }
        double expression() {
            double value = term();
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if (c == '+') { pos++; value += term(); }
                else if (c == '-') { pos++; value -= term(); }
                else break;
            }
            return value;
        }
        double term() {
            double value = factor();
            while (pos < text.length()) {
                char c = text.charAt(pos);
                if (c == '*') { pos++; value *= factor(); }
                else if (c == '/') { pos++; value /= factor(); }
                else break;
            }
            return value;
        }
        double factor() {
            if (pos >= text.length()) throw new IllegalArgumentException("unexpected end");
            if (text.charAt(pos) == '+') { pos++; return factor(); }
            if (text.charAt(pos) == '-') { pos++; return -factor(); }
            if (text.charAt(pos) == '(') {
                pos++;
                double value = expression();
                if (pos >= text.length() || text.charAt(pos) != ')') throw new IllegalArgumentException("missing )");
                pos++;
                return value;
            }
            int start = pos;
            while (pos < text.length() && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) pos++;
            if (start == pos) throw new IllegalArgumentException("expected number at " + pos);
            return Double.parseDouble(text.substring(start, pos));
        }
    }
}
