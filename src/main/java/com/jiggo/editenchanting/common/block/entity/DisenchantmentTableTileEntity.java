package com.jiggo.editenchanting.common.block.entity;

import com.jiggo.editenchanting.common.block.ModBlocks;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DisenchantmentTableTileEntity extends BlockEntity implements Nameable {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final Random RANDOM = new Random();

    public DisenchantmentTableTileEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TABLE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, DisenchantmentTableTileEntity entity) {
        entity.oOpen = entity.open;
        entity.oRot = entity.rot;
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
        if (player != null) {
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);
            entity.tRot = (float) Mth.atan2(dz, dx);
            entity.open += 0.1F;
            if (entity.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float previous = entity.flipT;
                do {
                    entity.flipT += RANDOM.nextInt(4) - RANDOM.nextInt(4);
                } while (previous == entity.flipT);
            }
        } else {
            entity.tRot += 0.02F;
            entity.open -= 0.1F;
        }

        while (entity.rot >= Math.PI) entity.rot -= (float) (Math.PI * 2);
        while (entity.rot < -Math.PI) entity.rot += (float) (Math.PI * 2);
        while (entity.tRot >= Math.PI) entity.tRot -= (float) (Math.PI * 2);
        while (entity.tRot < -Math.PI) entity.tRot += (float) (Math.PI * 2);

        float delta = entity.tRot - entity.rot;
        while (delta >= Math.PI) delta -= (float) (Math.PI * 2);
        while (delta < -Math.PI) delta += (float) (Math.PI * 2);

        entity.rot += delta * 0.4F;
        entity.open = Mth.clamp(entity.open, 0.0F, 1.0F);
        entity.time++;
        entity.oFlip = entity.flip;
        float f = (entity.flipT - entity.flip) * 0.4F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        entity.flipA += (f - entity.flipA) * 0.9F;
        entity.flip += entity.flipA;
    }

    @Override
    public Component getName() {
        return Component.translatable("block.editenchanting.enchantment_edit_table");
    }
}
