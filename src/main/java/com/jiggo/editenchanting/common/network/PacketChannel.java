package com.jiggo.editenchanting.common.network;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DisenchantmentEditTable.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class PacketChannel {
    private PacketChannel() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PacketClickButton.TYPE, PacketClickButton.STREAM_CODEC, PacketClickButton::handle);
    }
}
