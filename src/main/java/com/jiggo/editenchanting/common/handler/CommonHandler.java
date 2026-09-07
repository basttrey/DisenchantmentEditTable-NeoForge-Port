package com.jiggo.editenchanting.common.handler;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.common.block.ModBlocks;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = DisenchantmentEditTable.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CommonHandler {
    private CommonHandler() {}

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.BLOCK_ITEM.get());
        }
    }
}
