package com.teammoeg.frostedheart.content.scenario.client.gui.layered.gl;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.util.client.ClientUtils;
import com.teammoeg.frostedheart.util.client.FHGuiHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class TypedDynamicTexture {
	RenderType type;
	DynamicTexture texture;
	RenderType renderType;
	ResourceLocation resourceLocation;
	public TypedDynamicTexture(NativeImage texture) {
        this.texture = new DynamicTexture(texture);
        resourceLocation = FHMain.rl("fhscenario/generated_"+this.hashCode());
        ClientUtils.mc().textureManager.register(resourceLocation, this.texture);
        this.renderType=FHGuiHelper.RenderStateAccess.createTempType(resourceLocation);
	}
	public void close() {
		texture.close();
		ClientUtils.mc().textureManager.release(resourceLocation);
	}
	public void draw(GuiGraphics graphics,int x,int y,int w,int h,int uOffset,int vOffset,int uWidth,int vHeight,float alpha) {
        /*Matrix4f matrix4f = graphics.pose().last().pose();
        int textureWidth=texture.getPixels().getWidth();
        int textureHeight=texture.getPixels().getHeight();
        float u1= (uOffset + 0.0F) / textureWidth;
        float u2=(uOffset + uWidth) / textureWidth;
		float v1=(vOffset + 0.0F) / textureHeight;
		float v2=(vOffset + vHeight) / textureHeight;
		texture.bind();
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(renderType);
        vertexconsumer.vertex(matrix4f, x, y+h, 0F).color(1, 1, 1, alpha).uv(u1, v2).endVertex();
        vertexconsumer.vertex(matrix4f, x+w, y+h, 0F).color(1, 1, 1, alpha).uv(u2, v2).endVertex();
        vertexconsumer.vertex(matrix4f, x+w, y, 0F).color(1, 1, 1, alpha).uv(u2,v1).endVertex();
        vertexconsumer.vertex(matrix4f, x, y, 0F).color(1, 1, 1, alpha).uv(u1, v1).endVertex();*/
		FHGuiHelper.bindTexture(resourceLocation);
		FHGuiHelper.blit(graphics.pose(), x, y, w, h, uOffset, vOffset, uWidth, vHeight, texture.getPixels().getWidth(), texture.getPixels().getHeight(), alpha);
	}

}
