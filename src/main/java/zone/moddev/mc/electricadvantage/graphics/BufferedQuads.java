package zone.moddev.mc.electricadvantage.graphics;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

/** Preserves the legacy quad coordinates and UVs in the native entity vertex format. */
final class BufferedQuads {
    private final MatrixStack.Entry transform;
    private final IVertexBuilder buffer;
    private final int light;
    private final int overlay;
    private final float[][] vertices = new float[4][5];
    private int count;

    BufferedQuads(MatrixStack matrices, IVertexBuilder buffer, int light, int overlay) {
        this.transform = matrices.getLast();
        this.buffer = buffer;
        this.light = light;
        this.overlay = overlay;
    }

    BufferedQuads pos(double x, double y, double z) {
        vertices[count][0] = (float) x;
        vertices[count][1] = (float) y;
        vertices[count][2] = (float) z;
        return this;
    }

    BufferedQuads tex(float u, float v) {
        vertices[count][3] = u;
        vertices[count][4] = v;
        return this;
    }

    void endVertex() {
        if (++count != 4) return;
        float ax = vertices[1][0] - vertices[0][0];
        float ay = vertices[1][1] - vertices[0][1];
        float az = vertices[1][2] - vertices[0][2];
        float bx = vertices[2][0] - vertices[0][0];
        float by = vertices[2][1] - vertices[0][1];
        float bz = vertices[2][2] - vertices[0][2];
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        for (float[] vertex : vertices) {
            buffer.pos(transform.getMatrix(), vertex[0], vertex[1], vertex[2])
                    .color(255, 255, 255, 255).tex(vertex[3], vertex[4])
                    .overlay(overlay).lightmap(light)
                    .normal(transform.getNormal(), nx, ny, nz).endVertex();
        }
        count = 0;
    }
}
