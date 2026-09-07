package com.jiggo.editenchanting.client.renderer;

import com.jiggo.editenchanting.DisenchantmentEditTable;
import com.jiggo.editenchanting.common.block.entity.DisenchantmentTableTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Renders the animated floating book above the custom table, matching the original mod. */
public class DisenchantTableRenderer implements BlockEntityRenderer<DisenchantmentTableTileEntity> {
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            DisenchantmentEditTable.MODID, "textures/entity/enchantment_table_book.png");

    private final BookModel bookModel;

    public DisenchantTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(DisenchantmentTableTileEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);

        float time = entity.time + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);

        float rotationDelta = entity.rot - entity.oRot;
        while (rotationDelta >= Math.PI) rotationDelta -= (float) (Math.PI * 2.0);
        while (rotationDelta < -Math.PI) rotationDelta += (float) (Math.PI * 2.0);

        float rotation = entity.oRot + rotationDelta * partialTick;
        poseStack.mulPose(Axis.YP.rotation(-rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

        float page = Mth.lerp(partialTick, entity.oFlip, entity.flip);
        float rightPage = Mth.frac(page + 0.25F) * 1.6F - 0.3F;
        float leftPage = Mth.frac(page + 0.75F) * 1.6F - 0.3F;
        float open = Mth.lerp(partialTick, entity.oOpen, entity.open);

        bookModel.setupAnim(
                time,
                Mth.clamp(rightPage, 0.0F, 1.0F),
                Mth.clamp(leftPage, 0.0F, 1.0F),
                open);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(BOOK_TEXTURE));
        bookModel.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
