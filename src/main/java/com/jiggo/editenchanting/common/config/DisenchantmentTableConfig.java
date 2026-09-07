package com.jiggo.editenchanting.common.config;

import com.jiggo.editenchanting.common.enums.EnumPower;
import com.jiggo.editenchanting.common.enums.EnumType;
import com.jiggo.editenchanting.common.util.TextComponentUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class DisenchantmentTableConfig {
    public static final ModConfigSpec CONFIG;
    public static final ModConfigSpec.BooleanValue CREATIVE_MODE;
    public static final ModConfigSpec.EnumValue<EnumPower> POWER;
    public static final ModConfigSpec.ConfigValue<String> POWER_EXPRESSION;
    public static final ModConfigSpec.ConfigValue<String> REPORT_EXPRESSION;
    public static final ModConfigSpec.ConfigValue<String> COST;
    public static final ModConfigSpec.EnumValue<EnumType> TYPE;
    public static final ModConfigSpec.BooleanValue PAGE_TURN;
    public static final ModConfigSpec.BooleanValue STRICT_MODE;

    private DisenchantmentTableConfig() {}

    public static Item getCostItem() {
        ResourceLocation id = ResourceLocation.tryParse(COST.get());
        if (id == null) return Items.EMERALD;
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? Items.EMERALD : item;
    }

    public static String getCostName() {
        return TextComponentUtils.translatable(getCostItem().getDescriptionId()).getString();
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("General settings").push("general");
        CREATIVE_MODE = builder.comment("True if item verification needs to be deactivated")
                .define("creative_mode", false);
        POWER = builder.comment("What consumables to use").defineEnum("power", EnumPower.NONE);
        POWER_EXPRESSION = builder.comment(
                "Enchant and Edit computed expression",
                "Variables: level = total enchantment levels, count = enchantment count, repair = repair cost",
                "Supported operators: + - * / and parentheses",
                "Examples: level, ( level + repair ), ( level * 3 ), 10, ( ( level / 3 ) + 1 ), count")
                .define("power_expression", "level");
        REPORT_EXPRESSION = builder.comment("Export/report computed expression; same syntax as power_expression")
                .define("report_expression", "count");
        COST = builder.comment("Item cost when disenchanting, e.g. minecraft:emerald or minecraft:diamond")
                .define("cost", "minecraft:emerald");
        TYPE = builder.comment("Disenchant type will not be editable").defineEnum("type", EnumType.DEFAULT);
        PAGE_TURN = builder.comment("True if page turn is allowed").define("page_turn", true);
        STRICT_MODE = builder.comment("True if incompatible enchantments are rejected").define("strict_mode", false);
        builder.pop();
        CONFIG = builder.build();
    }
}
