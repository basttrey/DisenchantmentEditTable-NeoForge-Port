package com.jiggo.editenchanting.client.handler;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.enums.EnumPower;
import com.jiggo.editenchanting.common.enums.EnumType;
import com.jiggo.editenchanting.common.inventory.DisenchantmentMenu;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import com.jiggo.editenchanting.common.util.ExpressionUtils;
import com.jiggo.editenchanting.common.util.TextComponentUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = DisenchantmentEditTable.MODID, value = Dist.CLIENT)
public final class TooltipHandler {
    private TooltipHandler() {}

    @SubscribeEvent
    public static void onDisenchantmentContainer(ItemTooltipEvent event) {
        if (DisenchantmentTableConfig.POWER.get() == EnumPower.NONE) return;
        Player player = event.getEntity();
        if (player == null || !(player.containerMenu instanceof DisenchantmentMenu menu)) return;

        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        if (stack == menu.getEnchantingStack()) {
            Map<Holder<Enchantment>, Integer> map = menu.getLogic().getEnchantments(stack);
            if (!map.isEmpty()) {
                int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(map, ExpressionUtils.getRepairCost(stack))));
                lines.addAll(1, TextComponentUtils.getToolTip(TextComponentUtils.translatable("desc.editenchanting.export"), value));
            }
        } else if (player.getInventory().items.contains(stack)) {
            if (DisenchantmentTableConfig.TYPE.get() == EnumType.DISENCHANT) return;
            if (stack.is(Items.ENCHANTED_BOOK) && !EnchantmentUtils.isEmpty(stack) && !menu.getEnchantingStack().isEmpty()) {
                int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(stack)));
                lines.addAll(1, TextComponentUtils.getToolTip(TextComponentUtils.translatable("desc.editenchanting.edit"), value));
            }
        } else if (menu.getBookArrayInventory().contains(stack) && !EnchantmentUtils.isEmpty(stack)) {
            int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(stack)));
            lines.addAll(1, TextComponentUtils.getToolTip(TextComponentUtils.translatable("desc.editenchanting.cost"), value));
        }
    }
}
