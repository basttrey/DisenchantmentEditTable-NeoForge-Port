package com.jiggo.editenchanting.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Atlas-backed button used by the original Disenchantment Edit Table GUI.
 * The texture stores normal, highlighted and disabled states vertically.
 */
public class ImageButton extends Button {
    protected final ResourceLocation texture;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;

    public ImageButton(int x, int y, int width, int height,
                       int xTexStart, int yTexStart,
                       ResourceLocation texture, OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, height, texture, 256, 256, onPress);
    }

    public ImageButton(int x, int y, int width, int height,
                       int xTexStart, int yTexStart, int yDiffTex,
                       ResourceLocation texture, OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, texture, 256, 256, onPress);
    }

    public ImageButton(int x, int y, int width, int height,
                       int xTexStart, int yTexStart, int yDiffTex,
                       ResourceLocation texture, int textureWidth, int textureHeight,
                       OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.texture = texture;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int v = yTexStart;
        if (!this.active) {
            v += yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            v += yDiffTex;
        }

        RenderSystem.enableDepthTest();
        graphics.blit(
                texture,
                getX(), getY(),
                (float) xTexStart, (float) v,
                getWidth(), getHeight(),
                textureWidth, textureHeight);
    }
}
