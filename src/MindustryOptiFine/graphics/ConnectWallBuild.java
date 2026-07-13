package MindustryOptiFine.graphics;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.Vars;

public class ConnectWallBuild extends Building {
    public Seq<ConnectWallBuild> connectedWalls = new Seq<>();
    public int drawIndex = 0;
    public int drawInnerIndex = 0;

    // 缓存的方块边界
    private int minX, minY, maxX, maxY;
    private int centerX, centerY;

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

    // 更新缓存的边界数据
    private void updateBounds() {
        int size = block.size;
        minX = tile.x;
        minY = tile.y;
        maxX = tile.x + size - 1;
        maxY = tile.y + size - 1;
        centerX = tile.x + size / 2;
        centerY = tile.y + size / 2;
    }

    // 检查两个方块是否相邻（AABB碰撞检测）
    private boolean isAdjacentTo(Building other) {
        if (other == null || other.block != this.block) return false;

        int otherMinX = other.tile.x;
        int otherMinY = other.tile.y;
        int otherMaxX = other.tile.x + other.block.size - 1;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 检查是否重叠（如果是同一个方块）
        boolean xOverlap = (minX <= otherMaxX && maxX >= otherMinX);
        boolean yOverlap = (minY <= otherMaxY && maxY >= otherMinY);
        if (xOverlap && yOverlap) return false;

        // 计算两个矩形之间的最小距离
        int dx = 0;
        if (maxX < otherMinX) dx = otherMinX - maxX;
        else if (minX > otherMaxX) dx = minX - otherMaxX;

        int dy = 0;
        if (maxY < otherMinY) dy = otherMinY - maxY;
        else if (minY > otherMaxY) dy = minY - otherMaxY;

        // 如果两个矩形在8方向相邻（距离为1格）
        return dx <= 1 && dy <= 1;
    }

    // 检查某个位置是否被同类型方块占据
    private boolean isSameBlockAt(int x, int y) {
        Building other = Vars.world.build(x, y);
        return other != null && other.block == this.block;
    }

    // 检查某个位置是否属于当前方块
    private boolean isPartOfThisBlock(int x, int y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    // 检查某个方向是否有相邻的同类型方块
    private boolean hasNeighborInDirection(Point2 dir) {
        int size = block.size;

        // 根据方向确定检查的边缘
        int startX, startY, endX, endY;
        boolean checkHorizontal = (dir.x != 0);
        boolean checkVertical = (dir.y != 0);

        if (dir.x > 0) {
            // 检查右边缘
            startX = maxX + 1;
            endX = maxX + 1;
            startY = minY;
            endY = maxY;
        } else if (dir.x < 0) {
            // 检查左边缘
            startX = minX - 1;
            endX = minX - 1;
            startY = minY;
            endY = maxY;
        } else {
            startX = minX;
            endX = maxX;
            startY = minY;
            endY = maxY;
        }

        if (dir.y > 0) {
            // 检查上边缘
            startY = maxY + 1;
            endY = maxY + 1;
            if (dir.x == 0) {
                startX = minX;
                endX = maxX;
            }
        } else if (dir.y < 0) {
            // 检查下边缘
            startY = minY - 1;
            endY = minY - 1;
            if (dir.x == 0) {
                startX = minX;
                endX = maxX;
            }
        }

        // 遍历边缘的所有格子
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (isSameBlockAt(x, y)) {
                    Building other = Vars.world.build(x, y);
                    if (other != null && other != this) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 获取指定方向上相邻的方块
    private ConnectWallBuild getNeighborInDirection(Point2 dir) {
        int size = block.size;

        // 根据方向确定检查位置
        int checkX, checkY;
        if (dir.x > 0) checkX = maxX + 1;
        else if (dir.x < 0) checkX = minX - 1;
        else checkX = centerX;

        if (dir.y > 0) checkY = maxY + 1;
        else if (dir.y < 0) checkY = minY - 1;
        else checkY = centerY;

        // 检查该位置及其周围
        for (int dx = -size; dx <= size; dx++) {
            for (int dy = -size; dy <= size; dy++) {
                Building other = Vars.world.build(checkX + dx, checkY + dy);
                if (other instanceof ConnectWallBuild wall &&
                        wall.block == this.block &&
                        wall != this &&
                        isAdjacentTo(wall)) {
                    return wall;
                }
            }
        }
        return null;
    }

    public void updateDrawRegion() {
        updateBounds();

        String blockName = block.name;
        drawIndex = 0;
        drawInnerIndex = 0;

        // 检查8个方向
        for (int i = 0; i < 8; i++) {
            Point2 dir = Geometry.d8[i];
            if (hasNeighborInDirection(dir)) {
                drawIndex |= (1 << i);
            }
        }

        drawIndex = TileBitmask.values[drawIndex];

        // 内层纹理检测（4方向）
        if (drawIndex == 13 && ConnectWallHandler.hasInnerTexture(blockName)) {
            for (int i = 0; i < 4; i++) {
                Point2 dir = Geometry.d4[i];
                ConnectWallBuild neighbor = getNeighborInDirection(dir);
                if (neighbor != null && neighbor.drawIndex == 13) {
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
        return build instanceof ConnectWallBuild wall &&
                build.block.name.equals(block.name) &&
                wall.drawIndex == 13;
    }

    public void updateProximityWall() {
        updateBounds();
        connectedWalls.clear();

        // 检查所有可能的相邻位置
        int size = block.size;
        int range = size + 2; // 扩大检测范围

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                if (dx == 0 && dy == 0) continue;

                Building other = Vars.world.build(tile.x + dx, tile.y + dy);
                if (other instanceof ConnectWallBuild wall &&
                        wall.block == this.block &&
                        wall != this &&
                        isAdjacentTo(wall)) {
                    connectedWalls.add(wall);
                }
            }
        }

        updateDrawRegion();
    }

    @Override
    public void draw() {
        if (!ConnectWallHandler.enabled) {
            super.draw();
            return;
        }

        // 更新绘制尺寸
        float pixelSize = block.size * Vars.tilesize;

        String blockName = block.name;
        TextureRegion[] regions = ConnectWallHandler.getTiledRegions(blockName);
        if (regions != null && drawIndex < regions.length) {
            Draw.rect(regions[drawIndex], x, y, pixelSize, pixelSize);

            if (ConnectWallHandler.hasInnerTexture(blockName) && drawIndex == 13) {
                TextureRegion[] innerRegions = ConnectWallHandler.getInnerTiledRegions(blockName);
                if (innerRegions != null && drawInnerIndex < innerRegions.length) {
                    Draw.rect(innerRegions[drawInnerIndex], x, y, pixelSize, pixelSize);
                }
            }
        } else {
            super.draw();
        }
    }

    @Override
    public void updateProximity() {
        super.updateProximity();

        if (!ConnectWallHandler.enabled) return;

        updateProximityWall();
        for (ConnectWallBuild other : connectedWalls) {
            other.updateProximityWall();
        }
    }

    @Override
    public void onRemoved() {
        if (ConnectWallHandler.enabled) {
            for (ConnectWallBuild other : connectedWalls) {
                other.updateProximityWall();
            }
        }
        super.onRemoved();
    }
}