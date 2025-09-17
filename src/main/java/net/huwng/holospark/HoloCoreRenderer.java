package net.huwng.holospark.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.huwng.holospark.HolosparkMod;
import net.huwng.holospark.block.entity.HoloCoreBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public class HoloCoreRenderer implements BlockEntityRenderer<HoloCoreBlockEntity> {
    private static final ResourceLocation HOLOGRAM_TEX =
            new ResourceLocation(HolosparkMod.MODID, "textures/hologram.png");

    public HoloCoreRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(HoloCoreBlockEntity be, float partialTicks, PoseStack poseStack,
	                   MultiBufferSource buffer, int light, int overlay) {
	    if (!be.isPoweredScanned()) return;
	
	    int[][] map = be.getHeightmap();
	    int radius = be.getWorldRadius();
	    int grid = be.getRenderGrid();
	    int worldSize = 2 * radius;
	    if (map == null || map.length < worldSize) return;
	
	    long gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
	    long elapsed = gameTime - be.getScanStart();
	    float progress = Math.min(1.0f, (elapsed + partialTicks) / (float) be.getScanDurationTicks());
	
	    int rowsToReveal = Math.max(1, (int) Math.floor(worldSize * progress));
	
	    poseStack.pushPose();
	    poseStack.translate(0.5, 1.0, 0.5);
	
	    VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(HOLOGRAM_TEX));
	    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
	    for (int i = 0; i < worldSize; i++) {
	        for (int j = 0; j < worldSize; j++) {
	            int y = map[i][j];
	            if (y <= 0) continue;
	            if (y < minY) minY = y;
	            if (y > maxY) maxY = y;
	        }
	    }
	    if (minY == Integer.MAX_VALUE) {
	        poseStack.popPose();
	        return;
	    }
	
	    float renderSize = (float) grid;
	    float scale = renderSize / (float) worldSize;
	
	    int heightRange = maxY - minY;
	    float effectiveHeight = (heightRange <= 5) ? heightRange : 5f;
	
	    // ---------- vẽ địa hình ----------
	    for (int j = 0; j < rowsToReveal; j++) {
	        for (int i = 0; i < worldSize; i++) {
	            int y = map[i][j];
	            if (y <= 0) continue;
	
	            // ❌ không lọc dưới sea nữa (vẫn render)
	            float hy = (heightRange > 0)
	                    ? (y - minY) * (effectiveHeight / (float) heightRange)
	                    : 0f;
	
	            float normH = (heightRange > 0)
	                    ? (y - minY) / (float) heightRange
	                    : 0f;
	
	            float hx = (i - (worldSize / 2f)) * scale + (scale * 0.5f);
	            float hz = (j - (worldSize / 2f)) * scale + (scale * 0.5f);
	
	            float dist = (float) Math.sqrt(Math.pow(i - worldSize / 2f, 2) + Math.pow(j - worldSize / 2f, 2)) / (worldSize / 2f);
	            if (dist > 1f) dist = 1f;
	
	            int baseR = 0;
	            int baseG = (int) (150 + normH * 105);
	            int baseB = 255;
	
	            int r = (int) (baseR * (1 - dist) + 255 * dist);
	            int g = (int) (baseG * (1 - dist) + 255 * dist);
	            int b = (int) (baseB * (1 - dist) + 255 * dist);
	            int a = 180;
	
	            float nHy = (j > 0) ? ((map[i][j - 1] - minY) * (effectiveHeight / (float) heightRange)) : 0f;
	            float sHy = (j < worldSize - 1) ? ((map[i][j + 1] - minY) * (effectiveHeight / (float) heightRange)) : 0f;
	            float wHy = (i > 0) ? ((map[i - 1][j] - minY) * (effectiveHeight / (float) heightRange)) : 0f;
	            float eHy = (i < worldSize - 1) ? ((map[i + 1][j] - minY) * (effectiveHeight / (float) heightRange)) : 0f;
	
	            // top
	            drawTop(builder, poseStack, hx, hy, hz, scale, r, g, b, a, overlay);
	
	            // side faces
	            boolean isEdge = (i == 0 || j == 0 || i == worldSize - 1 || j == worldSize - 1);
	            if (!isEdge && hy > nHy + 0.01f) drawSide(builder, poseStack, hx, nHy, hz, scale, hy - nHy, r, g, b, a, overlay, "N");
	            if (!isEdge && hy > sHy + 0.01f) drawSide(builder, poseStack, hx, sHy, hz, scale, hy - sHy, r, g, b, a, overlay, "S");
	            if (!isEdge && hy > wHy + 0.01f) drawSide(builder, poseStack, hx, wHy, hz, scale, hy - wHy, r, g, b, a, overlay, "W");
	            if (!isEdge && hy > eHy + 0.01f) drawSide(builder, poseStack, hx, eHy, hz, scale, hy - eHy, r, g, b, a, overlay, "E");
	        }
	    }
	
	    // ---------- vẽ mặt biển phẳng ----------
	    int sea = be.getLevel().getSeaLevel();
	    float seaHeight = (sea - minY) * (effectiveHeight / (float) heightRange);
	    for (int i = 0; i < worldSize; i++) {
	        for (int j = 0; j < worldSize; j++) {
	            float hx = (i - (worldSize / 2f)) * scale + (scale * 0.5f);
	            float hz = (j - (worldSize / 2f)) * scale + (scale * 0.5f);
	
	            int r = 0, g = 100, b = 255, a = 100; // xanh biển mờ
	            drawTop(builder, poseStack, hx, seaHeight, hz, scale, r, g, b, a, overlay);
	        }
	    }
	
	    // ---------- render entity ----------
	    for (Entity e : be.getLevel().getEntities(null, new AABB(be.getBlockPos()).inflate(radius))) {
	        int color;
	        if (e instanceof Player) color = 0x00FF00;
	        else if (e.getType().getCategory() == MobCategory.MONSTER) color = 0xFF0000;
	        else if (e instanceof LivingEntity) color = 0xFFFF00;
	        else if (e instanceof ItemEntity) color = 0xFFFFFF;
	        else continue;
	
	        // vị trí mượt
	        double exWorld = e.xOld + (e.getX() - e.xOld) * partialTicks;
	        double eyWorld = e.yOld + (e.getY() - e.yOld) * partialTicks;
	        double ezWorld = e.zOld + (e.getZ() - e.zOld) * partialTicks;
	
	        double baseX = be.getBlockPos().getX() - radius;
	        double baseZ = be.getBlockPos().getZ() - radius;
	
	        double relX = exWorld - baseX;
	        double relZ = ezWorld - baseZ;
	        float ex = (float) ((relX / (double) worldSize) * renderSize - (renderSize / 2.0));
	        float ez = (float) ((relZ / (double) worldSize) * renderSize - (renderSize / 2.0));
	
	        float ey = (maxY > minY)
	                ? (float) ((eyWorld - minY) / (double) (maxY - minY)) * 5.0f
	                : 0.6f;
	
	        float cubeX = Math.max(0.05f, (e.getBbWidth() / (float) worldSize) * renderSize);
	        float cubeY = Math.max(0.05f, (e.getBbHeight() / (float) (maxY - minY)) * 5.0f);
	        float cubeZ = cubeX;
	
	        int rr = (color >> 16) & 0xFF;
	        int gg = (color >> 8) & 0xFF;
	        int bb = color & 0xFF;
	
	        drawEntityCube(builder, poseStack, ex, ey, ez, cubeX, cubeY, cubeZ, rr, gg, bb, 220, overlay);
	    }
	
	    poseStack.popPose();
	}
	
	    private void drawTop(VertexConsumer builder, PoseStack poseStack,
                         float x, float y, float z, float size,
                         int r, int g, int b, int a, int overlay) {
        float half = size * 0.5f;
        var pose = poseStack.last();
        int fullbright = 0xF000F0;

        builder.vertex(pose.pose(), x - half, y, z - half).color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
        builder.vertex(pose.pose(), x + half, y, z - half).color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
        builder.vertex(pose.pose(), x + half, y, z + half).color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
        builder.vertex(pose.pose(), x - half, y, z + half).color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
    }

    private void drawSide(VertexConsumer builder, PoseStack poseStack,
                          float x, float y, float z, float size, float height,
                          int r, int g, int b, int a, int overlay, String dir) {
        float half = size * 0.5f;
        var pose = poseStack.last();
        int fullbright = 0xF000F0;
        switch (dir) {
            case "N" -> {
                builder.vertex(pose.pose(), x - half, y, z - half).color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, -1).endVertex();
                builder.vertex(pose.pose(), x + half, y, z - half).color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, -1).endVertex();
                builder.vertex(pose.pose(), x + half, y + height, z - half).color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, -1).endVertex();
                builder.vertex(pose.pose(), x - half, y + height, z - half).color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, -1).endVertex();
            }
            case "S" -> {
                builder.vertex(pose.pose(), x - half, y, z + half).color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, 1).endVertex();
                builder.vertex(pose.pose(), x + half, y, z + half).color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, 1).endVertex();
                builder.vertex(pose.pose(), x + half, y + height, z + half).color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, 1).endVertex();
                builder.vertex(pose.pose(), x - half, y + height, z + half).color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 0, 1).endVertex();
            }
            case "E" -> {
                builder.vertex(pose.pose(), x + half, y, z - half).color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 1, 0, 0).endVertex();
                builder.vertex(pose.pose(), x + half, y, z + half).color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 1, 0, 0).endVertex();
                builder.vertex(pose.pose(), x + half, y + height, z + half).color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 1, 0, 0).endVertex();
                builder.vertex(pose.pose(), x + half, y + height, z - half).color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 1, 0, 0).endVertex();
            }
            case "W" -> {
                builder.vertex(pose.pose(), x - half, y, z - half).color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), -1, 0, 0).endVertex();
                builder.vertex(pose.pose(), x - half, y, z + half).color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), -1, 0, 0).endVertex();
                builder.vertex(pose.pose(), x - half, y + height, z + half).color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), -1, 0, 0).endVertex();
                builder.vertex(pose.pose(), x - half, y + height, z - half).color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), -1, 0, 0).endVertex();
            }
        }
    }

    private void drawEntityDot(VertexConsumer builder, PoseStack poseStack,
                               float x, float y, float z, float size,
                               int r, int g, int b, int a, int overlay) {
        float half = size * 0.5f;
        var pose = poseStack.last();
        int fullbright = 0xF000F0;

        builder.vertex(pose.pose(), x - half, y, z - half).color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
        builder.vertex(pose.pose(), x + half, y, z - half).color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
        builder.vertex(pose.pose(), x + half, y, z + half).color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
        builder.vertex(pose.pose(), x - half, y, z + half).color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(), 0, 1, 0).endVertex();
    }
    
	private void drawEntityCube(VertexConsumer builder, PoseStack poseStack,
	                            float x, float y, float z,
	                            float sx, float sy, float sz,
	                            int r, int g, int b, int a, int overlay) {
	    var pose = poseStack.last();
	    int fullbright = 0xF000F0;
	
	    float minX = x - sx / 2f;
	    float maxX = x + sx / 2f;
	    float minY = y;
	    float maxY = y + sy;
	    float minZ = z - sz / 2f;
	    float maxZ = z + sz / 2f;
	
	    // --- Top ---
	    builder.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a).uv(0,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,1,0).endVertex();
	    builder.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a).uv(1,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,1,0).endVertex();
	    builder.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a).uv(1,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,1,0).endVertex();
	    builder.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a).uv(0,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,1,0).endVertex();
	
	    // --- Bottom ---
	    builder.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a).uv(0,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,-1,0).endVertex();
	    builder.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a).uv(1,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,-1,0).endVertex();
	    builder.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a).uv(1,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,-1,0).endVertex();
	    builder.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a).uv(0,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,-1,0).endVertex();
	
	    // --- North ---
	    builder.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a).uv(0,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,-1).endVertex();
	    builder.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a).uv(1,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,-1).endVertex();
	    builder.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a).uv(1,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,-1).endVertex();
	    builder.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a).uv(0,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,-1).endVertex();
	
	    // --- South ---
	    builder.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a).uv(0,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,1).endVertex();
	    builder.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a).uv(1,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,1).endVertex();
	    builder.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a).uv(1,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,1).endVertex();
	    builder.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a).uv(0,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),0,0,1).endVertex();
	
	    // --- East ---
	    builder.vertex(pose.pose(), maxX, minY, minZ).color(r,g,b,a).uv(0,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),1,0,0).endVertex();
	    builder.vertex(pose.pose(), maxX, minY, maxZ).color(r,g,b,a).uv(1,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),1,0,0).endVertex();
	    builder.vertex(pose.pose(), maxX, maxY, maxZ).color(r,g,b,a).uv(1,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),1,0,0).endVertex();
	    builder.vertex(pose.pose(), maxX, maxY, minZ).color(r,g,b,a).uv(0,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),1,0,0).endVertex();
	
	    // --- West ---
	    builder.vertex(pose.pose(), minX, minY, minZ).color(r,g,b,a).uv(0,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),-1,0,0).endVertex();
	    builder.vertex(pose.pose(), minX, minY, maxZ).color(r,g,b,a).uv(1,0).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),-1,0,0).endVertex();
	    builder.vertex(pose.pose(), minX, maxY, maxZ).color(r,g,b,a).uv(1,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),-1,0,0).endVertex();
	    builder.vertex(pose.pose(), minX, maxY, minZ).color(r,g,b,a).uv(0,1).overlayCoords(overlay).uv2(fullbright).normal(pose.normal(),-1,0,0).endVertex();
	}

    @Override
    public boolean shouldRenderOffScreen(HoloCoreBlockEntity be) {
        return true;
    }
}
