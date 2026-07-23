package MindustryOptiFine.core.graphics.primitives;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;

public class VertexPosition2DColorTexture {
    public Vec2 position;
    public Color color;
    public Vec3 textureCoordinates;

    public VertexPosition2DColorTexture(Vec2 position, Color color, Vec2 textureCoordinates, float widthCorrectionFactor) {
        this.position = position;
        this.color = color;
        this.textureCoordinates = new Vec3(textureCoordinates.x, textureCoordinates.y, widthCorrectionFactor);
    }

    public VertexPosition2DColorTexture(float x, float y, Color color, float u, float v, float widthCorrectionFactor) {
        this.position = new Vec2(x, y);
        this.color = color;
        this.textureCoordinates = new Vec3(u, v, widthCorrectionFactor);
    }

    public static int getSize() {
        return 9;
    }

    public float[] toArray(float[] arr, int offset) {
        arr[offset] = position.x;
        arr[offset + 1] = position.y;
        arr[offset + 2] = color.r;
        arr[offset + 3] = color.g;
        arr[offset + 4] = color.b;
        arr[offset + 5] = color.a;
        arr[offset + 6] = textureCoordinates.x;
        arr[offset + 7] = textureCoordinates.y;
        arr[offset + 8] = textureCoordinates.z;
        return arr;
    }

    public static float[] toArray(VertexPosition2DColorTexture[] vertices) {
        float[] arr = new float[vertices.length * getSize()];
        int offset = 0;
        for (VertexPosition2DColorTexture v : vertices) {
            v.toArray(arr, offset);
            offset += getSize();
        }
        return arr;
    }
}