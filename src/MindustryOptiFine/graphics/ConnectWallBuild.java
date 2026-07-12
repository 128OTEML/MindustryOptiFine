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
    private int size;

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
        size = block.size;
        minX = tile.x;
        minY = tile.y;
        maxX = tile.x + size - 1;
        maxY = tile.y + size - 1;
    }

    // 检查两个方块是否完全面对齐（整面接触）
    private boolean isFullyAligned(Building other, Point2 direction) {
        if (other == null || other.block != this.block) return false;
        if (other == this) return false;

        int otherMinX = other.tile.x;
        int otherMinY = other.tile.y;
        int otherMaxX = other.tile.x + other.block.size - 1;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 检查是否重叠（如果是同一个方块）
        boolean xOverlap = (minX <= otherMaxX && maxX >= otherMinX);
        boolean yOverlap = (minY <= otherMaxY && maxY >= otherMinY);
        if (xOverlap && yOverlap) return false;

        // 根据方向检查是否完全对齐
        if (direction.x > 0 && direction.y == 0) {
            // 右侧：Y轴必须完全重叠
            return maxX + 1 == otherMinX && minY >= otherMinY && maxY <= otherMaxY;
        } else if (direction.x < 0 && direction.y == 0) {
            // 左侧：Y轴必须完全重叠
            return minX - 1 == otherMaxX && minY >= otherMinY && maxY <= otherMaxY;
        } else if (direction.x == 0 && direction.y > 0) {
            // 上侧：X轴必须完全重叠
            return maxY + 1 == otherMinY && minX >= otherMinX && maxX <= otherMaxX;
        } else if (direction.x == 0 && direction.y < 0) {
            // 下侧：X轴必须完全重叠
            return minY - 1 == otherMaxY && minX >= otherMinX && maxX <= otherMaxX;
        } else if (direction.x > 0 && direction.y > 0) {
            // 右上：检查右上角是否对齐
            return maxX + 1 == otherMinX && maxY + 1 == otherMinY &&
                    minY >= otherMinY && maxY <= otherMaxY &&
                    minX >= otherMinX && maxX <= otherMaxX;
        } else if (direction.x > 0 && direction.y < 0) {
            // 右下：检查右下角是否对齐
            return maxX + 1 == otherMinX && minY - 1 == otherMaxY &&
                    minY >= otherMinY && maxY <= otherMaxY &&
                    minX >= otherMinX && maxX <= otherMaxX;
        } else if (direction.x < 0 && direction.y > 0) {
            // 左上：检查左上角是否对齐
            return minX - 1 == otherMaxX && maxY + 1 == otherMinY &&
                    minY >= otherMinY && maxY <= otherMaxY &&
                    minX >= otherMinX && maxX <= otherMaxX;
        } else if (direction.x < 0 && direction.y < 0) {
            // 左下：检查左下角是否对齐
            return minX - 1 == otherMaxX && minY - 1 == otherMaxY &&
                    minY >= otherMinY && maxY <= otherMaxY &&
                    minX >= otherMinX && maxX <= otherMaxX;
        }

        return false;
    }

    // 检查某个位置是否属于同类型的其他方块
    private Building getSameBlockAt(int x, int y) {
        Building other = Vars.world.build(x, y);
        if (other != null && other.block == this.block && other != this) {
            return other;
        }
        return null;
    }

    // 检查某个方向是否有完全对齐的相邻方块
    private boolean hasAlignedNeighbor(Point2 dir) {
        int startX, startY, endX, endY;

        // 根据方向确定检查范围
        if (dir.x > 0 && dir.y == 0) {
            // 右侧：检查右边缘外侧
            startX = maxX + 1;
            endX = maxX + 1;
            startY = minY;
            endY = maxY;
        } else if (dir.x < 0 && dir.y == 0) {
            // 左侧：检查左边缘外侧
            startX = minX - 1;
            endX = minX - 1;
            startY = minY;
            endY = maxY;
        } else if (dir.x == 0 && dir.y > 0) {
            // 上侧：检查上边缘外侧
            startX = minX;
            endX = maxX;
            startY = maxY + 1;
            endY = maxY + 1;
        } else if (dir.x == 0 && dir.y < 0) {
            // 下侧：检查下边缘外侧
            startX = minX;
            endX = maxX;
            startY = minY - 1;
            endY = minY - 1;
        } else if (dir.x > 0 && dir.y > 0) {
            // 右上：检查右边缘和上边缘的交接处
            startX = maxX + 1;
            endX = maxX + 1;
            startY = minY;
            endY = maxY + 1;
        } else if (dir.x > 0 && dir.y < 0) {
            // 右下：检查右边缘和下边缘的交接处
            startX = maxX + 1;
            endX = maxX + 1;
            startY = minY - 1;
            endY = maxY;
        } else if (dir.x < 0 && dir.y > 0) {
            // 左上：检查左边缘和上边缘的交接处
            startX = minX - 1;
            endX = minX - 1;
            startY = minY;
            endY = maxY + 1;
        } else if (dir.x < 0 && dir.y < 0) {
            // 左下：检查左边缘和下边缘的交接处
            startX = minX - 1;
            endX = minX - 1;
            startY = minY - 1;
            endY = maxY;
        } else {
            return false; // 无效方向
        }

        // 检查边缘的所有格子
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                Building other = getSameBlockAt(x, y);
                if (other != null) {
                    // 检查这个方块是否完全对齐
                    if (isFullyAligned(other, dir)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 获取指定方向上完全对齐的相邻方块
    private ConnectWallBuild getAlignedNeighbor(Point2 dir) {
        int startX, startY, endX, endY;

        if (dir.x > 0) {
            startX = maxX + 1;
            endX = maxX + 1;
            startY = minY;
            endY = maxY;
        } else if (dir.x < 0) {
            startX = minX - 1;
            endX = minX - 1;
            startY = minY;
            endY = maxY;
        } else if (dir.y > 0) {
            startX = minX;
            endX = maxX;
            startY = maxY + 1;
            endY = maxY + 1;
        } else {
            startX = minX;
            endX = maxX;
            startY = minY - 1;
            endY = minY - 1;
        }

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                Building other = getSameBlockAt(x, y);
                if (other instanceof ConnectWallBuild wall && isFullyAligned(wall, dir)) {
                    return wall;
                }
            }
        }
        return null;
    }

    // 获取所有完全对齐的相邻方块（用于更新）
    private Seq<ConnectWallBuild> getAlignedNeighbors() {
        Seq<ConnectWallBuild> neighbors = new Seq<>();

        // 检查4个主要方向
        Point2[] directions = {
                new Point2(1, 0), new Point2(-1, 0),
                new Point2(0, 1), new Point2(0, -1)
        };

        for (Point2 dir : directions) {
            ConnectWallBuild neighbor = getAlignedNeighbor(dir);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    public void updateDrawRegion() {
        updateBounds();

        String blockName = block.name;
        drawIndex = 0;
        drawInnerIndex = 0;

        // 检查8个方向
        for (int i = 0; i < 8; i++) {
            Point2 dir = Geometry.d8[i];
            if (hasAlignedNeighbor(dir)) {
                drawIndex |= (1 << i);
            }
        }

        drawIndex = TileBitmask.values[drawIndex];

        // 内层纹理检测（4方向）
        if (drawIndex == 13 && ConnectWallHandler.hasInnerTexture(blockName)) {
            for (int i = 0; i < 4; i++) {
                Point2 dir = Geometry.d4[i];
                ConnectWallBuild neighbor = getAlignedNeighbor(dir);
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

        // 获取所有完全对齐的相邻方块
        connectedWalls = getAlignedNeighbors();

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