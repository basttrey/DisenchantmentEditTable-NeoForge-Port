package com.jiggo.editenchanting;

import com.jiggo.editenchanting.common.block.ModBlocks;
import com.jiggo.editenchanting.common.config.DisenchantmentTableConfig;
import com.jiggo.editenchanting.common.sound.ModSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(DisenchantmentEditTable.MODID)
public class DisenchantmentEditTable {
    public static final String MODID = "editenchanting";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DisenchantmentEditTable(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.MENUS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, DisenchantmentTableConfig.CONFIG);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
