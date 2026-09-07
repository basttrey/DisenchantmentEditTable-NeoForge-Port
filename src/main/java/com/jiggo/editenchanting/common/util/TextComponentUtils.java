package com.jiggo.editenchanting.common.util;

import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.enums.EnumError;
import com.jiggo.editenchanting.common.enums.EnumPower;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class TextComponentUtils {
    public static final MutableComponent EMPTY = translatable("desc.editenchanting.empty");
    public static final MutableComponent ERROR = literal("Game Error").withStyle(ChatFormatting.RED);

    private TextComponentUtils() {}

    public static MutableComponent translatable(String key) {
        return Component.translatable(key);
    }

    public static MutableComponent translatable(String key, Object... values) {
        return Component.translatable(key, values);
    }

    public static MutableComponent literal(String text) {
        return Component.literal(text);
    }

    public static List<Component> getToolTip(MutableComponent text, int value) {
        List<Component> lines = new ArrayList<>();
        EnumPower power = DisenchantmentTableConfig.POWER.get();
        lines.add(text.withStyle(ChatFormatting.GRAY));
        switch (power) {
            case ITEM -> lines.add(space(translatable("desc.editenchanting.item", value, DisenchantmentTableConfig.getCostName()))
                    .withStyle(ChatFormatting.BLUE));
            case EXPERIENCE -> lines.add(space(translatable("desc.editenchanting.experience", value))
                    .withStyle(ChatFormatting.GREEN));
            case NONE -> { }
        }
        lines.add(Component.empty());
        return lines;
    }

    public static MutableComponent getItem(ItemStack stack) {
        MutableComponent text = Component.empty().append(stack.getHoverName());
        if (stack.has(DataComponents.CUSTOM_NAME)) text.withStyle(ChatFormatting.ITALIC);
        return text;
    }

    public static MutableComponent getExportCost(ItemStack stack) {
        Map<Holder<Enchantment>, Integer> map = EnchantmentUtils.getEnchantments(stack);
        if (!map.isEmpty()) {
            int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(map, ExpressionUtils.getRepairCost(stack))));
            return switch (DisenchantmentTableConfig.POWER.get()) {
                case ITEM -> translatable("desc.editenchanting.item", value, DisenchantmentTableConfig.getCostName()).withStyle(ChatFormatting.BLUE);
                case EXPERIENCE -> translatable("desc.editenchanting.experience", value).withStyle(ChatFormatting.GREEN);
                case NONE -> ERROR.copy();
            };
        }
        return ERROR.copy();
    }

    public static MutableComponent getCost(ItemStack stack) {
        if (!stack.isEmpty() && !EnchantmentUtils.getEnchantments(stack).isEmpty()) {
            int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(stack)));
            return switch (DisenchantmentTableConfig.POWER.get()) {
                case ITEM -> translatable("desc.editenchanting.item", value, DisenchantmentTableConfig.getCostName()).withStyle(ChatFormatting.BLUE);
                case EXPERIENCE -> translatable("desc.editenchanting.experience", value).withStyle(ChatFormatting.GREEN);
                case NONE -> ERROR.copy();
            };
        }
        return ERROR.copy();
    }

    public static List<MutableComponent> getEnchantTexts(ItemStack stack) {
        List<MutableComponent> list = new ArrayList<>();
        EnchantmentUtils.getEnchantments(stack).forEach((holder, level) -> {
            ChatFormatting formatting = holder.is(EnchantmentTags.CURSE) ? ChatFormatting.RED : ChatFormatting.AQUA;
            list.add(Enchantment.getFullname(holder, level).copy().withStyle(formatting));
        });
        return list;
    }

    public static MutableComponent getEnchant(ItemStack stack) {
        if (!stack.isEmpty() && !EnchantmentUtils.getEnchantments(stack).isEmpty()) {
            Map.Entry<Holder<Enchantment>, Integer> entry = EnchantmentUtils.getEnchantmentAttribute(stack);
            ChatFormatting formatting = entry.getKey().is(EnchantmentTags.CURSE) ? ChatFormatting.RED : ChatFormatting.AQUA;
            return Enchantment.getFullname(entry.getKey(), entry.getValue()).copy().withStyle(formatting);
        }
        return ERROR.copy();
    }

    public static MutableComponent getRequire() {
        String key = DisenchantmentTableConfig.POWER.get() == EnumPower.ITEM
                ? DisenchantmentTableConfig.getCostName()
                : "desc.editenchanting.exp";
        String cost = translatable(key).getString();
        return translatable("desc.editenchanting.require", cost).withStyle(ChatFormatting.RED);
    }

    public static List<Component> getRequire(EnumError error, ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        switch (error) {
            case DISENCHANT -> {
                lines.add(translatable("desc.editenchanting.cost").withStyle(ChatFormatting.RED));
                lines.add(getCost(stack).withStyle(ChatFormatting.RED));
            }
            case EDIT -> {
                lines.add(translatable("desc.editenchanting.edit").withStyle(ChatFormatting.RED));
                lines.add(getCost(stack).withStyle(ChatFormatting.RED));
            }
            case EXPORT -> {
                lines.add(translatable("desc.editenchanting.export").withStyle(ChatFormatting.RED));
                lines.add(getExportCost(stack).withStyle(ChatFormatting.RED));
            }
            case NONE -> { }
        }
        return lines;
    }

    public static MutableComponent space(MutableComponent component) {
        return literal(" ").append(component);
    }
}
