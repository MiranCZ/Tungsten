package me.miran.mixin.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import me.miran.Main;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.lineWidth(2.0F);

        Main.RENDERERS.forEach(r -> {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            r.render();
            tessellator.draw();
        });

        Main.PATH_RENDERERS.forEach(r -> {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            r.render();
            tessellator.draw();
        });

        RenderSystem.lineWidth(10);
        Main.TEST.forEach(r -> {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            r.render();
            tessellator.draw();
        });

        RenderSystem.lineWidth(3);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        Main.targetRenderer.render();
        tessellator.draw();

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }

}
