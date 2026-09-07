package com.jiggo.editenchanting.client.gui;

import com.jiggo.editenchanting.common.sound.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;

/** Restores the original textured page/control buttons and page-turn click sound. */
public class ChangePageButton extends ImageButton {
    private boolean pressedPulse;

    public ChangePageButton(int x, int y, int width, int height,
                            int xTexStart, int yTexStart, int yDiffTex,
                            ResourceLocation texture, OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, texture, onPress);
    }

    @Override
    public void playDownSound(SoundManager manager) {
        manager.play(SimpleSoundInstance.forUI(ModSounds.BOOK_PAGE_TURN.get(), 1.0F));
    }

    @Override
    public void onPress() {
        super.onPress();
        this.pressedPulse = true;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (pressedPulse) {
            pressedPulse = false;
            setFocused(true);
        }
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        setFocused(false);
    }
}
