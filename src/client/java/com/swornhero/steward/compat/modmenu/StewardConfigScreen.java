package com.swornhero.steward.compat.modmenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class StewardConfigScreen extends Screen {

    private final Screen parent;

    public StewardConfigScreen(Screen parent) {
        super(Component.translatable("screen.steward.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(
                Button.builder(
                                CommonComponents.GUI_DONE,
                                button -> onClose()
                        )
                        .pos(width / 2 - 100, height - 40)
                        .size(200, 20)
                        .build()
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(
                font,
                title,
                width / 2,
                20,
                0xFFFFFFFF
        );

        graphics.centeredText(
                font,
                Component.translatable(
                        "screen.steward.config.placeholder"
                ),
                width / 2,
                50,
                0xFFAAAAAA
        );
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }
}