package com.jiggo.editenchanting.client.handler;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.client.gui.DisenchantmentScreen;
import com.jiggo.editenchanting.client.renderer.DisenchantTableRenderer;
import com.jiggo.editenchanting.common.block.ModBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = DisenchantmentEditTable.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientHandler {
    private ClientHandler() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModBlocks.CONTAINER.get(), DisenchantmentScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.TABLE_BLOCK_ENTITY.get(), DisenchantTableRenderer::new);
    }
}
