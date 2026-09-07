package com.jiggo.editenchanting.common.sound;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, DisenchantmentEditTable.MODID);

    public static final Supplier<SoundEvent> DISENCHANTMENT_TABLE_USE = register("block.disenchantment_table.use");
    public static final Supplier<SoundEvent> BOOK_PAGE_TURN = register("block.disenchantment_table.page_turn");

    private static Supplier<SoundEvent> register(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(DisenchantmentEditTable.MODID, path)));
    }
}
