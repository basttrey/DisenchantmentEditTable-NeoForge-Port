package com.jiggo.editenchanting.common.inventory;

import com.jiggo.editenchanting.common.block.ModBlocks;
import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.control.EnchantedBookLogic;
import com.jiggo.editenchanting.common.enums.EnumError;
import com.jiggo.editenchanting.common.enums.EnumPower;
import com.jiggo.editenchanting.common.sound.ModSounds;
import com.jiggo.editenchanting.common.util.EnchantmentUtils;
import com.jiggo.editenchanting.common.util.ExpressionUtils;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.items.ItemStackHandler;

public class DisenchantmentMenu extends AbstractContainerMenu {
    private static final int TABLE_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int BOOK_START = 2;
    private static final int BOOK_END = 20;
    private static final int PLAYER_START = 20;
    private static final int PLAYER_END = 47;
    private static final int HOTBAR_START = 47;
    private static final int HOTBAR_END = 56;

    private final Player player;
    private final ItemStackHandlerEnchant tableInventory;
    private final ItemStackHandlerBook bookInventory;
    private final ItemStackHandlerBooks bookArrayInventory;
    private final ItemStackHandlerCost costInventory;
    private final EnchantedBookLogic logic;
    private final ContainerLevelAccess access;
    private EnumError error = EnumError.NONE;
    private ItemStack errorStack = ItemStack.EMPTY;

    public DisenchantmentMenu(int windowId, Inventory inventory) {
        this(windowId, inventory, ContainerLevelAccess.NULL);
    }

    public DisenchantmentMenu(int windowId, Inventory inventory, ContainerLevelAccess access) {
        super(ModBlocks.CONTAINER.get(), windowId);
        this.player = inventory.player;
        this.access = access;
        this.tableInventory = new ItemStackHandlerEnchant(this);
        this.bookInventory = new ItemStackHandlerBook(this);
        this.bookArrayInventory = new ItemStackHandlerBooks(this);
        this.costInventory = new ItemStackHandlerCost(this);

        addSlot(new SlotEnchant(tableInventory, 0, 13, 26));
        addSlot(new SlotEnchant(bookInventory, 0, 53, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addBookArrayInventory(bookArrayInventory);
        addPlayerInventory(inventory);
        if (DisenchantmentTableConfig.POWER.get() == EnumPower.ITEM) {
            addSlot(new SlotCost(costInventory, 0, 170, 84));
        }

        this.logic = new EnchantedBookLogic(this);
    }

    private void addBookArrayInventory(ItemStackHandlerBooks inventory) {
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 6; column++) {
                addSlot(new SlotBook(inventory, this, index++, 80 + column * 18, 15 + row * 18));
            }
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.TABLE.get());
    }

    public void onContentsChanged(ItemStackHandler inventory) {
        access.execute((level, pos) -> {
            if (level.isClientSide) return;
            if (inventory instanceof ItemStackHandlerEnchant) {
                if (!tableInventory.getEnchantingStack().isEmpty()) logic.update();
                else logic.close();
            } else if (inventory instanceof ItemStackHandlerBooks) {
                logic.bookArray();
            }
        });
    }

    public void previous() {
        if (DisenchantmentTableConfig.PAGE_TURN.get()) logic.previous();
    }

    public void next() {
        if (DisenchantmentTableConfig.PAGE_TURN.get()) logic.next();
    }

    public void take() {
        ItemStack stack = getEnchantingStack();
        if (stack.isEmpty()) return;
        if (!ExpressionUtils.canReport(player, stack)) {
            setError(EnumError.EXPORT);
            setErrorStack(stack.copy());
            return;
        }
        setError(EnumError.NONE);
        disenchantCost(stack, DisenchantmentTableConfig.POWER.get(), player);
        logic.take();
        playSound();
    }

    public void disenchantCost(ItemStack stack, EnumPower power, Player player) {
        Map<Holder<Enchantment>, Integer> map = logic.getEnchantments(stack);
        if (map.isEmpty()) return;

        int value = Math.max(0, (int)Math.floor(ExpressionUtils.getCost(map, ExpressionUtils.getRepairCost(stack))));
        switch (power) {
            case ITEM -> costInventory.getCost().shrink(value);
            case EXPERIENCE -> player.onEnchantmentPerformed(stack, value);
            case NONE -> { }
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> {
            if (level.isClientSide) return;
            returnToPlayer(player, tableInventory.getStackInSlot(0));
            returnToPlayer(player, bookInventory.getStackInSlot(0));
            if (DisenchantmentTableConfig.POWER.get() == EnumPower.ITEM) {
                returnToPlayer(player, costInventory.getStackInSlot(0));
            }
        });
    }

    private static void returnToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.isAlive() || player instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected()) {
            player.drop(stack, false);
        } else {
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();

        if (slotIndex <= OUTPUT_SLOT) {
            if (!moveItemStackTo(source, PLAYER_START, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (slotIndex >= PLAYER_START && slotIndex < PLAYER_END) {
            if (!moveItemStackTo(source, TABLE_SLOT, OUTPUT_SLOT, false)
                    && !moveItemStackTo(source, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= HOTBAR_START && slotIndex < HOTBAR_END) {
            if (!moveItemStackTo(source, TABLE_SLOT, OUTPUT_SLOT, false)
                    && !moveItemStackTo(source, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, PLAYER_START, HOTBAR_END, false)) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (source.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (slot instanceof SlotBook && !player.level().isClientSide) {
            if (!player.hasInfiniteMaterials()) {
                bookArrayInventory.disenchantPower(original, DisenchantmentTableConfig.POWER.get(), player);
            }
            playSound();
        }
        slot.onTake(player, source);
        return original;
    }

    public void playSound() {
        access.execute((level, pos) -> {
            if (!level.isClientSide) {
                level.playSound(null, pos, ModSounds.DISENCHANTMENT_TABLE_USE.get(), SoundSource.BLOCKS,
                        1.0F, level.random.nextFloat() * 0.1F + 0.9F);
            }
        });
    }

    public ItemStackHandlerEnchant getTableInventory() { return tableInventory; }
    public ItemStackHandlerBook getBookInventory() { return bookInventory; }
    public ItemStackHandlerBooks getBookArrayInventory() { return bookArrayInventory; }
    public ItemStackHandlerCost getCostInventory() { return costInventory; }
    public Player getPlayer() { return player; }
    public ContainerLevelAccess getAccess() { return access; }
    public EnchantedBookLogic getLogic() { return logic; }
    public ItemStack getEnchantingStack() { return tableInventory.getEnchantingStack(); }
    public int getCostCount() { return costInventory.getCost().isEmpty() ? 0 : costInventory.getCost().getCount(); }
    public EnumError getError() { return error; }
    public void setError(EnumError error) { this.error = error; }
    public ItemStack getErrorStack() { return errorStack; }
    public void setErrorStack(ItemStack stack) { this.errorStack = stack; }
}
