package MindustryOptiFine.core.graphics.primitives;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import MindustryOptiFine.core.graphics.shaders.ManagedShader;
import MindustryOptiFine.core.graphics.shaders.ShaderManager;

public class PrimitiveRenderer {
    private static Mesh vertexMesh;

    private static IPrimitiveSettings mainSettings;
    private static Seq<Vec2> mainPositions = new Seq<>();
    private static Seq<VertexPosition2DColorTexture> mainVertices = new Seq<>();
    private static short[] mainIndices = new short[0];

    private static final short MAX_TRAIL_POSITIONS = 2000;
    private static final short MAX_CIRCLE_POSITIONS = 1500;
    private static final short MAX_VERTICES = 6144;
    private static final short MAX_INDICES = 16384;

    private static short positionsIndex;
    private static short verticesIndex;
    private static short indicesIndex;

    private static final short[] QUAD_INDICES = {0, 1, 2, 2, 3, 0};

    public static void load() {
        if (Core.graphics == null) return;

        mainPositions = new Seq<>(MAX_TRAIL_POSITIONS);
        mainVertices = new Seq<>(MAX_VERTICES);
        mainIndices = new short[MAX_INDICES];

        vertexMesh = new Mesh(true, MAX_VERTICES, MAX_INDICES,
            new VertexAttribute(0, 2, false, "a_position"),
            new VertexAttribute(1, 4, false, "a_color"),
            new VertexAttribute(2, 3, false, "a_texCoord")
        );
    }

    public static void unload() {
        if (Core.graphics == null) return;
        
        if (vertexMesh != null) {
            vertexMesh.dispose();
            vertexMesh = null;
        }
        mainPositions.clear();
        mainVertices.clear();
        mainIndices = new short[0];
    }

    private static void performPixelationSafetyChecks(IPrimitiveSettings settings) {
        if (settings.getPixelate() && !PrimitivePixelationSystem.currentlyRendering) {
            throw new RuntimeException("Error: Primitives using pixelation MUST be prepared/rendered from the IPixelatedPrimitiveRenderer.RenderPixelatedPrimitives method");
        } else if (!settings.getPixelate() && PrimitivePixelationSystem.currentlyRendering) {
            throw new RuntimeException("Error: Primitives not using pixelation MUST NOT be prepared/rendered from the IPixelatedPrimitiveRenderer.RenderPixelatedPrimitives method");
        }
    }

    public static void renderTrail(Seq<Vec2> positions, PrimitiveSettings settings) {
        renderTrail(positions, settings, null);
    }

    public static void renderTrail(Seq<Vec2> positions, PrimitiveSettings settings, Integer pointsToCreate) {
        performPixelationSafetyChecks(settings);

        int count = positions.size;
        if (count <= 2) return;
        if (count >= MAX_TRAIL_POSITIONS) return;

        if (!assignPointsRectangleTrail(positions, settings, pointsToCreate != null ? pointsToCreate : count)) {
            return;
        }

        if (mainPositions.size <= 2) return;

        mainSettings = settings;
        assignVerticesRectangleTrail(settings);
        assignIndicesRectangleTrail();
        privateRender();
    }

    private static void privateRender() {
        if (indicesIndex % 6 != 0 || verticesIndex <= 3) return;

        Core.gl.glEnable(Core.gl.GL_SCISSOR_TEST);
        Core.gl.glScissor(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        Mat view = new Mat();
        Mat projection = new Mat();
        int width = mainSettings.getProjectionAreaWidth() != null ? mainSettings.getProjectionAreaWidth() : Core.graphics.getWidth();
        int height = mainSettings.getProjectionAreaHeight() != null ? mainSettings.getProjectionAreaHeight() : Core.graphics.getHeight();

        if (mainSettings.getPixelate() || mainSettings.getUseUnscaledMatrix()) {
            calculateUnscaledMatrices(width, height, view, projection);
        } else {
            calculatePrimitiveMatrices(width, height, view, projection);
        }

        ManagedShader shaderToUse = mainSettings.getShader();
        if (shaderToUse == null) {
            shaderToUse = ShaderManager.getShader("StandardPrimitiveShader");
            Log.info("PrimitiveRenderer: trying StandardPrimitiveShader: " + (shaderToUse != null));
        }
        if (shaderToUse == null) {
            shaderToUse = ShaderManager.getShader("primitive");
            Log.info("PrimitiveRenderer: trying primitive shader: " + (shaderToUse != null));
        }
        
        if (shaderToUse != null) {
            Log.info("PrimitiveRenderer: using shader: " + shaderToUse.name);
            Mat worldViewProjection = new Mat(view).mul(projection);
            shaderToUse.shader.bind();
            shaderToUse.shader.setUniformMatrix("uWorldViewProjection", worldViewProjection);
            shaderToUse.apply();
        } else {
            Log.info("PrimitiveRenderer: no shader available, rendering without shader");
        }

        uploadVertices();
        uploadIndices();

        if (shaderToUse != null) {
            Log.info("PrimitiveRenderer: rendering mesh with " + (indicesIndex / 3) + " triangles");
            vertexMesh.render(shaderToUse.shader, Core.gl.GL_TRIANGLES, 0, indicesIndex / 3);
            Log.info("PrimitiveRenderer: mesh rendered successfully");
        }

        Core.gl.glDisable(Core.gl.GL_SCISSOR_TEST);
    }

    private static boolean assignPointsRectangleTrail(Seq<Vec2> positions, PrimitiveSettings settings, int pointsToCreate) {
        int positionsCount = positions.size;
        pointsToCreate = Math.min(pointsToCreate, MAX_TRAIL_POSITIONS - 1);

        if (!settings.smoothen) {
            positionsIndex = 0;
            Seq<Vec2> filteredPositions = new Seq<>();
            for (Vec2 pos : positions) {
                if (!pos.isZero()) {
                    filteredPositions.add(pos);
                }
            }
            positionsCount = filteredPositions.size;

            if (positionsCount <= 2) return false;

            mainPositions.clear();
            for (int i = 0; i < pointsToCreate; i++) {
                float completionRatio = i / (float)(pointsToCreate - 1);
                int currentIndex = (int)(completionRatio * (positionsCount - 1));
                Vec2 currentPoint = filteredPositions.get(currentIndex);
                Vec2 nextPoint = filteredPositions.get((currentIndex + 1) % positionsCount);
                float lerpAmount = completionRatio * (positionsCount - 1) % 0.99999f;
                Vec2 lerped = new Vec2(currentPoint).lerp(nextPoint, lerpAmount);
                lerped.sub(Core.camera.position);
                mainPositions.add(lerped);
                positionsIndex++;
            }
            return true;
        }

        positionsIndex = 1;
        Seq<Vec2> controlPoints = new Seq<>();
        int index = 0;
        for (Vec2 position : positions) {
            if (position.isZero()) continue;

            float completionRatio = index / (float)positionsCount;
            Vec2 offset = new Vec2().sub(Core.camera.position);
            if (settings.offsetFunction != null) {
                offset.add(settings.offsetFunction.get(completionRatio));
            }
            controlPoints.add(position.cpy().add(offset));
            index++;
        }

        if (controlPoints.size <= 4) return false;

        mainPositions.clear();
        mainPositions.add(controlPoints.get(0));

        for (int j = 0; j < pointsToCreate; j++) {
            float splineInterpolant = j / (float)pointsToCreate;
            float localSplineInterpolant = splineInterpolant * (controlPoints.size - 1) % 1f;
            int localSplineIndex = (int)(splineInterpolant * (controlPoints.size - 1));

            Vec2 farLeft, left, right, farRight;
            left = controlPoints.get(localSplineIndex);
            right = controlPoints.get(localSplineIndex + 1);

            if (localSplineIndex <= 0) {
                farLeft = left.cpy().scl(2).sub(right);
            } else {
                farLeft = controlPoints.get(localSplineIndex - 1);
            }

            if (localSplineIndex >= controlPoints.size - 2) {
                farRight = right.cpy().scl(2).sub(left);
            } else {
                farRight = controlPoints.get(localSplineIndex + 2);
            }

            Vec2 result = catmullRom(farLeft, left, right, farRight, localSplineInterpolant);
            mainPositions.add(result);
            positionsIndex++;
        }

        mainPositions.add(controlPoints.get(controlPoints.size - 1));
        positionsIndex++;
        return true;
    }

    private static Vec2 catmullRom(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;

        float q0 = -t3 + 2 * t2 - t;
        float q1 = 3 * t3 - 5 * t2 + 2;
        float q2 = -3 * t3 + 4 * t2 + t;
        float q3 = t3 - t2;

        float x = 0.5f * (p0.x * q0 + p1.x * q1 + p2.x * q2 + p3.x * q3);
        float y = 0.5f * (p0.y * q0 + p1.y * q1 + p2.y * q2 + p3.y * q3);

        return new Vec2(x, y);
    }

    private static void assignVerticesRectangleTrail(PrimitiveSettings settings) {
        verticesIndex = 0;
        mainVertices.clear();

        for (int i = 0; i < positionsIndex; i++) {
            float completionRatio = i / (float)(positionsIndex - 1);
            float widthAtVertex = settings.widthFunction.get(completionRatio);
            Color vertexColor = settings.colorFunction.get(completionRatio);
            Vec2 currentPosition = mainPositions.get(i);

            Vec2 directionToAhead;
            if (i == positionsIndex - 1) {
                directionToAhead = currentPosition.cpy().sub(mainPositions.get(i - 1)).nor();
            } else {
                directionToAhead = mainPositions.get(i + 1).cpy().sub(currentPosition).nor();
            }

            Vec2 leftCurrentTextureCoord = new Vec2(completionRatio, 0.5f - widthAtVertex * 0.5f);
            Vec2 rightCurrentTextureCoord = new Vec2(completionRatio, 0.5f + widthAtVertex * 0.5f);

            Vec2 sideDirection = new Vec2(-directionToAhead.y, directionToAhead.x);

            Vec2 left = currentPosition.cpy().sub(sideDirection.cpy().scl(widthAtVertex));
            Vec2 right = currentPosition.cpy().add(sideDirection.cpy().scl(widthAtVertex));

            if (i == 0 && settings.initialVertexPositionsOverride != null && settings.initialVertexPositionsOverride.isValid()) {
                left = settings.initialVertexPositionsOverride.left;
                right = settings.initialVertexPositionsOverride.right;
            }

            mainVertices.add(new VertexPosition2DColorTexture(left, vertexColor, leftCurrentTextureCoord, widthAtVertex));
            mainVertices.add(new VertexPosition2DColorTexture(right, vertexColor, rightCurrentTextureCoord, widthAtVertex));
            verticesIndex += 2;
        }
    }

    private static void assignIndicesRectangleTrail() {
        indicesIndex = 0;

        for (short i = 0; i < positionsIndex - 2; i++) {
            short connectToIndex = (short)(i * 2);
            mainIndices[indicesIndex++] = connectToIndex;
            mainIndices[indicesIndex++] = (short)(connectToIndex + 1);
            mainIndices[indicesIndex++] = (short)(connectToIndex + 2);
            mainIndices[indicesIndex++] = (short)(connectToIndex + 2);
            mainIndices[indicesIndex++] = (short)(connectToIndex + 1);
            mainIndices[indicesIndex++] = (short)(connectToIndex + 3);
        }
    }

    public static void renderQuad(Texture texture, Vec2 center, float scale, float rotation) {
        renderQuad(texture, center, new Vec2(scale, scale), rotation, null, null);
    }

    public static void renderQuad(Texture texture, Vec2 center, Vec2 scale, float rotation) {
        renderQuad(texture, center, scale, rotation, null, null);
    }

    public static void renderQuad(Texture texture, Vec2 center, Vec2 scale, float rotation, Color color, ManagedShader shader) {
        float quadAreaX = texture.width;
        float quadAreaY = texture.height;

        Mat rotationMatrix = new Mat().setToRotationRad(rotation);
        Mat scaleMatrix = new Mat().setToScaling(scale.x, scale.y);
        Mat viewMatrix = new Mat().setToTranslation(center.x - Core.camera.position.x, center.y - Core.camera.position.y);

        Mat worldViewProjection = new Mat(rotationMatrix).mul(scaleMatrix).mul(viewMatrix);

        if (color == null) {
            color = Color.white;
        }

        verticesIndex = 0;
        mainVertices.clear();
        mainVertices.add(new VertexPosition2DColorTexture(0f, -quadAreaY, color, 0.01f, 0.01f, 1f));
        mainVertices.add(new VertexPosition2DColorTexture(quadAreaX, -quadAreaY, color, 0.99f, 0.01f, 1f));
        mainVertices.add(new VertexPosition2DColorTexture(quadAreaX, 0f, color, 0.99f, 0.99f, 1f));
        mainVertices.add(new VertexPosition2DColorTexture(0f, 0f, color, 0.01f, 0.99f, 1f));
        verticesIndex = 4;

        Core.gl.glEnable(Core.gl.GL_SCISSOR_TEST);
        Core.gl.glScissor(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        if (shader == null) {
            shader = ShaderManager.getShader("quadrenderer");
        }

        if (shader != null) {
            shader.shader.bind();
            shader.shader.setUniformMatrix("uWorldViewProjection", worldViewProjection);
            shader.setTexture(texture, 1);
            shader.apply();
        }

        uploadVertices();
        System.arraycopy(QUAD_INDICES, 0, mainIndices, 0, QUAD_INDICES.length);
        indicesIndex = (short)QUAD_INDICES.length;
        uploadIndices();

        if (shader != null) {
            vertexMesh.render(shader.shader, Core.gl.GL_TRIANGLES, 0, 2);
        }

        Core.gl.glDisable(Core.gl.GL_SCISSOR_TEST);
    }

    public static void renderCircle(Vec2 center, PrimitiveSettingsCircle settings) {
        renderCircle(center, settings, 512);
    }

    public static void renderCircle(Vec2 center, PrimitiveSettingsCircle settings, int sideCount) {
        if (sideCount <= 0) return;

        performPixelationSafetyChecks(settings);
        mainSettings = settings;
        sideCount = Math.min(sideCount, MAX_CIRCLE_POSITIONS - 1);

        float sideAngle = Mathf.PI2 / sideCount;
        float sideLengthMinusRadius = (float)Math.sqrt(2f - Mathf.cos(sideAngle) * 2f);

        verticesIndex = 0;
        indicesIndex = 0;
        mainVertices.clear();

        for (int i = 0; i < sideCount; i++) {
            float completionRatio = i / (float)sideCount;
            float nextSideCompletionRatio = (i + 1f) / sideCount;
            float radius = settings.radiusFunction.get(completionRatio);
            Color color = settings.colorFunction.get(completionRatio);

            Vec2 orthogonal = new Vec2().set(Mathf.cos(Mathf.PI2 * completionRatio + Mathf.PI / 2), Mathf.sin(Mathf.PI2 * completionRatio + Mathf.PI / 2));
            Vec2 radiusOffset = new Vec2().set(Mathf.cos(Mathf.PI2 * completionRatio), Mathf.sin(Mathf.PI2 * completionRatio)).scl(radius);

            Vec2 leftEdge = center.cpy().add(radiusOffset).add(orthogonal.cpy().scl(sideLengthMinusRadius * radius * -0.5f)).sub(Core.camera.position);
            Vec2 rightEdge = center.cpy().add(radiusOffset).add(orthogonal.cpy().scl(sideLengthMinusRadius * radius * 0.5f)).sub(Core.camera.position);
            Vec2 centerPos = center.cpy().sub(Core.camera.position);

            mainVertices.add(new VertexPosition2DColorTexture(leftEdge, color, new Vec2(completionRatio, 1f), 1f));
            mainVertices.add(new VertexPosition2DColorTexture(rightEdge, color, new Vec2(nextSideCompletionRatio, 1f), 1f));
            mainVertices.add(new VertexPosition2DColorTexture(centerPos, color, new Vec2(nextSideCompletionRatio, 0f), 1f));
            mainVertices.add(new VertexPosition2DColorTexture(centerPos, color, new Vec2(completionRatio, 0f), 1f));
            verticesIndex += 4;

            mainIndices[indicesIndex++] = (short)(i * 4);
            mainIndices[indicesIndex++] = (short)(i * 4 + 1);
            mainIndices[indicesIndex++] = (short)(i * 4 + 2);
            mainIndices[indicesIndex++] = (short)(i * 4);
            mainIndices[indicesIndex++] = (short)(i * 4 + 2);
            mainIndices[indicesIndex++] = (short)(i * 4 + 3);
        }

        Mat view = new Mat();
        Mat projection = new Mat();
        int width = mainSettings.getProjectionAreaWidth() != null ? mainSettings.getProjectionAreaWidth() : Core.graphics.getWidth();
        int height = mainSettings.getProjectionAreaHeight() != null ? mainSettings.getProjectionAreaHeight() : Core.graphics.getHeight();

        if (mainSettings.getPixelate() || mainSettings.getUseUnscaledMatrix()) {
            calculateUnscaledMatrices(width, height, view, projection);
        } else {
            calculatePrimitiveMatrices(width, height, view, projection);
        }

        ManagedShader shaderToUse = mainSettings.getShader() != null ? mainSettings.getShader() : ShaderManager.getShader("standardprimitiveshader");
        
        if (shaderToUse != null) {
            Mat worldViewProjection = new Mat(view).mul(projection);
            shaderToUse.shader.bind();
            shaderToUse.shader.setUniformMatrix("uWorldViewProjection", worldViewProjection);
            shaderToUse.apply();
        }

        uploadVertices();
        uploadIndices();

        if (shaderToUse != null) {
            vertexMesh.render(shaderToUse.shader, Core.gl.GL_TRIANGLES, 0, sideCount * 2);
        }
    }

    public static void renderCircleEdge(Vec2 center, PrimitiveSettingsCircleEdge settings) {
        renderCircleEdge(center, settings, 200);
    }

    public static void renderCircleEdge(Vec2 center, PrimitiveSettingsCircleEdge settings, int totalPoints) {
        if (totalPoints <= 0) return;

        performPixelationSafetyChecks(settings);
        mainSettings = settings;
        totalPoints = Math.min(totalPoints, MAX_CIRCLE_POSITIONS);

        verticesIndex = 0;
        mainVertices.clear();

        for (int i = 0; i <= totalPoints; i++) {
            float interpolant = i / (float)totalPoints;
            float currentWidth = settings.edgeWidthFunction.get(interpolant);
            Color color = settings.colorFunction.get(interpolant);
            float radius = settings.radiusFunction.get(interpolant);

            float angle = i * Mathf.PI2 / totalPoints;
            Vec2 innerPosition = center.cpy().add(new Vec2(Mathf.cos(angle), Mathf.sin(angle)).scl(radius)).sub(Core.camera.position);
            Vec2 outerPosition = center.cpy().add(new Vec2(Mathf.cos(angle), Mathf.sin(angle)).scl(radius + currentWidth)).sub(Core.camera.position);

            mainVertices.add(new VertexPosition2DColorTexture(innerPosition, color, new Vec2(interpolant, 0f), 1f));
            mainVertices.add(new VertexPosition2DColorTexture(outerPosition, color, new Vec2(interpolant, 1f), 1f));
            verticesIndex += 2;
        }

        Mat view = new Mat();
        Mat projection = new Mat();
        int width = mainSettings.getProjectionAreaWidth() != null ? mainSettings.getProjectionAreaWidth() : Core.graphics.getWidth();
        int height = mainSettings.getProjectionAreaHeight() != null ? mainSettings.getProjectionAreaHeight() : Core.graphics.getHeight();

        if (mainSettings.getPixelate() || mainSettings.getUseUnscaledMatrix()) {
            calculateUnscaledMatrices(width, height, view, projection);
        } else {
            calculatePrimitiveMatrices(width, height, view, projection);
        }

        ManagedShader shaderToUse = mainSettings.getShader() != null ? mainSettings.getShader() : ShaderManager.getShader("standardprimitiveshader");
        
        if (shaderToUse != null) {
            Mat worldViewProjection = new Mat(view).mul(projection);
            shaderToUse.shader.bind();
            shaderToUse.shader.setUniformMatrix("uWorldViewProjection", worldViewProjection);
            shaderToUse.apply();
        }

        uploadVertices();

        if (shaderToUse != null) {
            vertexMesh.render(shaderToUse.shader, Core.gl.GL_TRIANGLE_STRIP, 0, verticesIndex - 2);
        }
    }

    private static void calculateUnscaledMatrices(int width, int height, Mat viewMatrix, Mat projectionMatrix) {
        viewMatrix.idt();
        projectionMatrix.setOrtho(0, 0, width, height);
    }

    private static void calculatePrimitiveMatrices(int width, int height, Mat viewMatrix, Mat projectionMatrix) {
        viewMatrix.idt();
        projectionMatrix.setOrtho(0, 0, width, height);
    }

    private static void uploadVertices() {
        float[] data = new float[mainVertices.size * VertexPosition2DColorTexture.getSize()];
        int offset = 0;

        for (VertexPosition2DColorTexture v : mainVertices) {
            data[offset++] = v.position.x;
            data[offset++] = v.position.y;
            data[offset++] = v.color.r;
            data[offset++] = v.color.g;
            data[offset++] = v.color.b;
            data[offset++] = v.color.a;
            data[offset++] = v.textureCoordinates.x;
            data[offset++] = v.textureCoordinates.y;
            data[offset++] = v.textureCoordinates.z;
        }

        vertexMesh.setVertices(data);
    }

    private static void uploadIndices() {
        vertexMesh.setIndices(mainIndices, 0, indicesIndex);
    }

    public static class PrimitivePixelationSystem {
        public static boolean currentlyRendering = false;
    }
}