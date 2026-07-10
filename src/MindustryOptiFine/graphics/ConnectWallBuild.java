package MindustryOptiFine.graphics;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;

public class ConnectWallBuild extends Building {
    public Seq<ConnectWallBuild> connectedWalls = new Seq<>();
    public int drawIndex = 0;
    public int drawInnerIndex = 0;

    private static final Point2[] checkPos = {
            new Point2(0, 1),
            new Point2(1, 0),
            new Point2(0, -1),
            new Point2(-1, 0),

            new Point2(1, 1),
            new Point2(1, -1),
            new Point2(-1, -1),
            new Point2(-1, 1),

            new Point2(0, 2),
            new Point2(2, 0),
            new Point2(0, -2),
            new Point2(-2, 0),
    };

    public void updateDrawRegion() {
        String blockName = block.name;
        drawIndex = 0;
        drawInnerIndex = 0;
        for (int i = 0; i < 8; i++) {
            Tile other = tile.nearby(Geometry.d8[i]);
            if (checkAutotileSame(other)) {
                drawIndex |= (1 << i);
            }
        }
        drawIndex = TileBitmask.values[drawIndex];
        if (drawIndex == 13 && ConnectWallHandler.hasInnerTexture(blockName)) {
            for (int i = 0; i < 4; i++) {
                Tile other1 = tile.nearby(Geometry.d4[i]);
                if (checkAutotileInnerSame(other1)) {
                    drawInnerIndex |= (1 << i);
                }
            }
        }
    }

    public boolean checkAutotileSame(Tile other) {
        return other != null && checkAutotileSame(other.build);
    }

    public boolean checkAutotileInnerSame(Tile other) {
        return other != null && checkAutotileInnerSame(other.build);
    }

    public boolean checkAutotileSame(Building build) {
        return build != null && build.block.name.equals(block.name);
    }

    public boolean checkAutotileInnerSame(Building build) {
        return build instanceof ConnectWallBuild wall && build.block.name.equals(block.name) && wall.drawIndex == 13;
    }

    public void updateProximityWall() {
        connectedWalls.clear();

        for (Point2 point : checkPos) {
            Building other = mindustry.Vars.world.build(tile.x + point.x, tile.y + point.y);
            if (other == null) continue;
            if (other instanceof ConnectWallBuild wall && checkAutotileSame(other)) {
                connectedWalls.add(wall);
            }
        }

        updateDrawRegion();
    }

    @Override
    public void draw() {
        String blockName = block.name;
        TextureRegion[] regions = ConnectWallHandler.getTiledRegions(blockName);
        if (regions != null && drawIndex < regions.length) {
            Draw.rect(regions[drawIndex], x, y);
            if (ConnectWallHandler.hasInnerTexture(blockName) && drawIndex == 13) {
                TextureRegion[] innerRegions = ConnectWallHandler.getInnerTiledRegions(blockName);
                if (innerRegions != null && drawInnerIndex < innerRegions.length) {
                    Draw.rect(innerRegions[drawInnerIndex], x, y);
                }
            }
        } else {
            super.draw();
        }
    }

    @Override
    public void updateProximity() {
        super.updateProximity();

        updateProximityWall();
        for (ConnectWallBuild other : connectedWalls) {
            other.updateProximityWall();
        }
    }

    @Override
    public void onRemoved() {
        for (ConnectWallBuild other : connectedWalls) {
            other.updateProximityWall();
        }
        super.onRemoved();
    }
}