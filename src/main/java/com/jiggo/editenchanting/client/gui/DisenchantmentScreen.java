package com.jiggo.editenchanting.client.gui;

import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.enums.EnumDisplay;
import com.jiggo.editenchanting.common.enums.EnumError;
import com.jiggo.editenchanting.common.enums.EnumPower;
import com.jiggo.editenchanting.common.inventory.DisenchantmentMenu;
import com.jiggo.editenchanting.common.inventory.SlotBook;
import com.jiggo.editenchanting.common.inventory.SlotEnchant;
import com.jiggo.editenchanting.common.network.PacketClickButton;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import com.jiggo.editenchanting.common.util.ExpressionUtils;
import com.jiggo.editenchanting.common.util.TextComponentUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

public class DisenchantmentScreen extends AbstractContainerScreen<DisenchantmentMenu> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath("editenchanting", "textures/gui/container/enchantment_edit_table.png"),
            ResourceLocation.fromNamespaceAndPath("editenchanting", "textures/gui/container/enchantment_edit_table2.png")
    };

    private EnumDisplay display = EnumDisplay.NONE;

    public DisenchantmentScreen(DisenchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 194;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ChangePageButton(
                leftPos + 12, topPos + 50, 18, 18,
                0, imageHeight + 1, 18, TEXTURES[0], b -> send(0)));
        addRenderableWidget(new ChangePageButton(
                leftPos + 32, topPos + 50, 18, 18,
                18, imageHeight + 1, 18, TEXTURES[0], b -> send(1)));
        addRenderableWidget(new ChangePageButton(
                leftPos + 52, topPos + 50, 18, 18,
                36, imageHeight + 1, 18, TEXTURES[0], b -> {
            Player player = menu.getPlayer();
            if (ExpressionUtils.canReport(player, menu.getEnchantingStack())) {
                menu.setError(EnumError.NONE);
                send(2);
            } else if (!EnchantmentUtils.isEmpty(menu.getEnchantingStack())) {
                menu.setError(EnumError.EXPORT);
                menu.setErrorStack(menu.getEnchantingStack().copy());
            }
        }));
    }

    private static void send(int type) {
        PacketDistributor.sendToServer(new PacketClickButton(type));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderDetails(graphics);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = TEXTURES[DisenchantmentTableConfig.POWER.get() == EnumPower.ITEM ? 1 : 0];
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String titleText = I18n.get("tile.container.disenchant");
        graphics.drawString(font, titleText, imageWidth / 5 - font.width(titleText) / 2 + 3, 5, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 4210752, false);
    }

    protected void renderDetails(GuiGraphics graphics) {
        if (!menu.getEnchantingStack().isEmpty() && DisenchantmentTableConfig.POWER.get() != EnumPower.NONE) {
            int position = parseIntTranslation("config.editenchanting.position", 125);
            List<Component> components = getTextLines();
            int offset = components.size() == 1 ? 30 : 15;
            graphics.renderComponentTooltip(font, components, leftPos - position, topPos + offset);
        }
    }

    private static int parseIntTranslation(String key, int fallback) {
        try {
            return Integer.parseInt(I18n.get(key));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public List<Component> getTextLines() {
        Player player = Objects.requireNonNull(Minecraft.getInstance().player);
        List<Component> lines = new ArrayList<>();

        if (menu.getCarried().isEmpty()) {
            if (hoveredSlot != null && hoveredSlot.hasItem()) {
                if (hoveredSlot instanceof SlotBook) display = EnumDisplay.BOOK;
                else if (hoveredSlot instanceof SlotEnchant) display = EnumDisplay.DISENCHANT;
                else if (hoveredSlot.getItem().is(Items.ENCHANTED_BOOK)
                        && player.getInventory().items.contains(hoveredSlot.getItem())) display = EnumDisplay.PLAYER;
                else display = EnumDisplay.DISENCHANT;
            } else if (!menu.getEnchantingStack().isEmpty()) display = EnumDisplay.DISENCHANT;
            else display = EnumDisplay.NONE;
        } else if (!menu.getEnchantingStack().isEmpty()) display = EnumDisplay.DISENCHANT;
        else display = EnumDisplay.NONE;

        switch (display) {
            case DISENCHANT -> {
                ItemStack enchanting = menu.getEnchantingStack();
                lines.add(TextComponentUtils.translatable("desc.editenchanting.enchant_slot").withStyle(ChatFormatting.GRAY));
                lines.add(TextComponentUtils.getItem(enchanting));
                lines.add(TextComponentUtils.EMPTY);
                if (!EnchantmentUtils.isEmpty(enchanting)) {
                    lines.add(TextComponentUtils.translatable("desc.editenchanting.export").withStyle(ChatFormatting.GRAY));
                    lines.add(TextComponentUtils.getExportCost(enchanting));
                    lines.add(TextComponentUtils.EMPTY);
                }
            }
            case BOOK -> {
                ItemStack book = hoveredSlot.getItem();
                lines.add(TextComponentUtils.translatable("desc.editenchanting.book_slot").withStyle(ChatFormatting.GRAY));
                lines.add(TextComponentUtils.getEnchant(book));
                lines.add(TextComponentUtils.EMPTY);
                lines.add(TextComponentUtils.translatable("desc.editenchanting.cost").withStyle(ChatFormatting.GRAY));
                lines.add(TextComponentUtils.getCost(book));
                lines.add(TextComponentUtils.EMPTY);
            }
            case PLAYER -> {
                ItemStack stack = hoveredSlot.getItem();
                lines.add(TextComponentUtils.translatable("desc.editenchanting.book_slot").withStyle(ChatFormatting.GRAY));
                int size = EnchantmentUtils.getEnchantments(stack).size();
                if (size > 1) {
                    if (isShift()) lines.addAll(TextComponentUtils.getEnchantTexts(stack));
                    else lines.add(TextComponentUtils.translatable("desc.editenchanting.shift").withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    lines.add(TextComponentUtils.getEnchant(stack));
                }
                lines.add(TextComponentUtils.EMPTY);
                if (size == 1 || size > 1 && !isShift()) {
                    lines.add(TextComponentUtils.translatable("desc.editenchanting.edit").withStyle(ChatFormatting.GRAY));
                    lines.add(TextComponentUtils.getCost(stack));
                    lines.add(TextComponentUtils.EMPTY);
                }
            }
            case NONE -> { }
        }

        if (menu.getError() != EnumError.NONE) {
            lines.add(TextComponentUtils.translatable("desc.editenchanting.tip").withStyle(ChatFormatting.YELLOW));
            lines.add(TextComponentUtils.getRequire());
            lines.add(TextComponentUtils.EMPTY);
            lines.addAll(TextComponentUtils.getRequire(menu.getError(), menu.getErrorStack()));
            lines.add(TextComponentUtils.EMPTY);
        }
        return lines;
    }

    private boolean isShift() {
        return Screen.hasShiftDown();
    }
}
