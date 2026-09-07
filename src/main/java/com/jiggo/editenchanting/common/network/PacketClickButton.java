package com.jiggo.editenchanting.common.network;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.common.inventory.DisenchantmentMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketClickButton(int button) implements CustomPacketPayload {
    public static final Type<PacketClickButton> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DisenchantmentEditTable.MODID, "click_button"));
    public static final StreamCodec<ByteBuf, PacketClickButton> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketClickButton::button,
            PacketClickButton::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketClickButton payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof DisenchantmentMenu menu) {
                switch (payload.button) {
                    case 0 -> menu.previous();
                    case 1 -> menu.next();
                    case 2 -> menu.take();
                    default -> { }
                }
            }
        });
    }
}
