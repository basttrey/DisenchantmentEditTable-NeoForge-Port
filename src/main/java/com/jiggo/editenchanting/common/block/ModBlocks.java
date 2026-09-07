package com.jiggo.editenchanting.common.block;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.common.block.entity.DisenchantmentTableTileEntity;
import com.jiggo.editenchanting.common.inventory.DisenchantmentMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DisenchantmentEditTable.MODID);
    public static final DeferredBlock<DisenchantmentEditTableBlock> TABLE = BLOCKS.registerBlock(
            "enchantment_edit_table",
            DisenchantmentEditTableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 2000.0F)
                    .lightLevel(state -> 0));

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DisenchantmentEditTable.MODID);
    public static final DeferredItem<BlockItem> BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TABLE);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DisenchantmentEditTable.MODID);
    public static final Supplier<BlockEntityType<DisenchantmentTableTileEntity>> TABLE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "enchantment_edit_table",
            () -> BlockEntityType.Builder.of(DisenchantmentTableTileEntity::new, TABLE.get()).build(null));

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, DisenchantmentEditTable.MODID);
    public static final Supplier<MenuType<DisenchantmentMenu>> CONTAINER = MENUS.register(
            "enchantment_edit_table",
            () -> new MenuType<>(DisenchantmentMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
