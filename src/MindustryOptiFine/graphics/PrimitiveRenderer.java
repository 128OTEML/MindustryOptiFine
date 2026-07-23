package MindustryOptiFine.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.Vars;
import MindustryOptiFine.shaders.ModShaders;

public class PrimitiveRenderer{
    private static Mesh mesh;
    private static float[] vertices = new float[4096];
    private static short[] indices = new short[4096];
    private static int vertexCount = 0;
    private static int indexCount = 0;
    private static boolean drawing = false;

    private static final int FLOATS_PER_VERTEX = 8;

    public static void renderTrail(Vec2[] points, TrailSettings settings, int steps){
        if(points.length < 2) return;
        
        begin();
        
        float totalLength = 0;
        for(int i = 1; i < points.length; i++){
            totalLength += points[i].dst(points[i - 1]);
        }
        
        float stepLength = totalLength / steps;
        float currentLength = 0;
        
        Vec2 prev = points[0];
        Vec2 next;
        
        for(int i = 1; i < points.length; i++){
            next = points[i];
            float segmentLength = prev.dst(next);
            
            while(currentLength < totalLength){
                float t = currentLength / totalLength;
                float localT = Mathf.clamp((currentLength) / (totalLength / (points.length - 1)));
                
                float posX = Interp.linear.apply(prev.x, next.x, localT);
                float posY = Interp.linear.apply(prev.y, next.y, localT);
                Vec2 pos = new Vec2(posX, posY);
                
                Vec2 dir = next.cpy().sub(prev).nor();
                Vec2 perp = new Vec2(-dir.y, dir.x);
                
                float width = settings.width.get(t);
                Color color = settings.color.get(t);
                Vec2 offset = settings.offset.get(t);
                
                Vec2 p1 = pos.cpy().add(perp.scl(width)).add(offset);
                Vec2 p2 = pos.cpy().sub(perp.scl(width)).add(offset);
                
                addVertex(p1.x, p1.y, color);
                addVertex(p2.x, p2.y, color);
                
                currentLength += stepLength;
            }
            
            prev = next;
        }
        
        end();
    }

    public static void renderCircle(Vec2 center, CircleSettings settings, int segments){
        begin();
        
        for(int i = 0; i <= segments; i++){
            float angle = (i / (float)segments) * Mathf.PI2;
            float radius = settings.radius.get(i / (float)segments);
            Color color = settings.color.get(i / (float)segments);
            
            float x = center.x + Mathf.cos(angle) * radius;
            float y = center.y + Mathf.sin(angle) * radius;
            
            addVertex(center.x, center.y, color);
            addVertex(x, y, color);
        }
        
        end();
    }

    public static void renderLine(Vec2 start, Vec2 end, Color color, float width){
        begin();
        
        Vec2 dir = end.cpy().sub(start).nor();
        Vec2 perp = new Vec2(-dir.y, dir.x).scl(width);
        
        addVertex(start.x + perp.x, start.y + perp.y, color);
        addVertex(start.x - perp.x, start.y - perp.y, color);
        addVertex(end.x + perp.x, end.y + perp.y, color);
        addVertex(end.x - perp.x, end.y - perp.y, color);
        
        indices[indexCount++] = 0;
        indices[indexCount++] = 1;
        indices[indexCount++] = 2;
        indices[indexCount++] = 2;
        indices[indexCount++] = 1;
        indices[indexCount++] = 3;
        
        end();
    }

    private static void begin(){
        if(drawing) return;
        drawing = true;
        vertexCount = 0;
        indexCount = 0;
    }

    private static void addVertex(float x, float y, Color color){
        if(vertexCount * FLOATS_PER_VERTEX >= vertices.length){
            float[] newVertices = new float[vertices.length * 2];
            System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            vertices = newVertices;
        }
        
        int offset = vertexCount * FLOATS_PER_VERTEX;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = 0;
        vertices[offset + 3] = 0;
        vertices[offset + 4] = color.r;
        vertices[offset + 5] = color.g;
        vertices[offset + 6] = color.b;
        vertices[offset + 7] = color.a;
        
        vertexCount++;
    }

    private static void end(){
        if(!drawing) return;
        drawing = false;
        
        if(vertexCount < 2) return;
        
        if(mesh == null){
            mesh = new Mesh(true, 1024, 1024,
                VertexAttribute.position,
                VertexAttribute.texCoords,
                VertexAttribute.color
            );
        }
        
        mesh.setVertices(vertices, 0, vertexCount * FLOATS_PER_VERTEX);
        
        if(indexCount == 0){
            for(int i = 0; i < vertexCount - 1; i += 2){
                if(indexCount + 6 > indices.length){
                    short[] newIndices = new short[indices.length * 2];
                    System.arraycopy(indices, 0, newIndices, 0, indices.length);
                    indices = newIndices;
                }
                indices[indexCount++] = (short)i;
                indices[indexCount++] = (short)(i + 1);
                indices[indexCount++] = (short)(i + 2);
                indices[indexCount++] = (short)(i + 2);
                indices[indexCount++] = (short)(i + 1);
                indices[indexCount++] = (short)(i + 3);
            }
        }
        
        mesh.setIndices(indices, 0, indexCount);
        
        ModShaders.primitive.bind();
        ModShaders.primitive.apply();
        
        mesh.render(ModShaders.primitive, Gl.triangles);
        
        Gl.useProgram(0);
    }

    public static class TrailSettings{
        public Floatp width = f -> 10f;
        public Colorp color = f -> Color.white;
        public Vec2p offset = f -> new Vec2();

        public TrailSettings width(Floatp width){
            this.width = width;
            return this;
        }

        public TrailSettings color(Colorp color){
            this.color = color;
            return this;
        }

        public TrailSettings offset(Vec2p offset){
            this.offset = offset;
            return this;
        }
    }

    public static class CircleSettings{
        public Floatp radius = f -> 100f;
        public Colorp color = f -> Color.white;

        public CircleSettings radius(Floatp radius){
            this.radius = radius;
            return this;
        }

        public CircleSettings color(Colorp color){
            this.color = color;
            return this;
        }
    }

    public interface Floatp{
        float get(float t);
    }

    public interface Colorp{
        Color get(float t);
    }

    public interface Vec2p{
        Vec2 get(float t);
    }
}