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

    /**
     * 检查两个方块是否在某个方向上完全对齐（面完全接触）
     * @param other 另一个方块
     * @return true 如果两个方块完全对齐并接触
     */
    private boolean isFullyAlignedWith(Building other) {
        if (other == null || other.block != this.block) return false;

        int otherMinX = other.tile.x;
        int otherMinY = other.tile.y;
        int otherMaxX = other.tile.x + other.block.size - 1;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 排除自身重叠
        boolean xOverlap = (minX <= otherMaxX && maxX >= otherMinX);
        boolean yOverlap = (minY <= otherMaxY && maxY >= otherMinY);
        if (xOverlap && yOverlap) return false;

        // 计算水平和垂直距离
        int dx = 0;
        if (maxX < otherMinX) dx = otherMinX - maxX;
        else if (minX > otherMaxX) dx = minX - otherMaxX;

        int dy = 0;
        if (maxY < otherMinY) dy = otherMinY - maxY;
        else if (minY > otherMaxY) dy = minY - otherMaxY;

        // 先检查基本相邻（距离为1格或交错接触）
        if (dx > 1 || dy > 1) return false;

        // 检查是否完全对齐
        return isFullyAlignedInDirection(other, dx, dy);
    }

    /**
     * 检查两个方块在特定方向上是否完全对齐
     */
    private boolean isFullyAlignedInDirection(Building other, int dx, int dy) {
        int otherMinX = other.tile.x;
        int otherMinY = other.tile.y;
        int otherMaxX = other.tile.x + other.block.size - 1;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 水平相邻（dx == 1 且 dy == 0）
        if (dx == 1 && dy == 0) {
            // 检查垂直方向是否完全重叠
            return isVerticalFullyOverlapped(other);
        }

        // 垂直相邻（dy == 1 且 dx == 0）
        if (dy == 1 && dx == 0) {
            // 检查水平方向是否完全重叠
            return isHorizontalFullyOverlapped(other);
        }

        // 对角相邻（dx == 1 && dy == 1）
        if (dx == 1 && dy == 1) {
            // 检查角是否完全接触
            return isCornerFullyAligned(other, dx, dy);
        }

        // 交错接触（dx == 0 && dy == 0）
        if (dx == 0 && dy == 0) {
            return isInterlockingContact(other);
        }

        // 其他情况（距离大于1或不在标准方向）
        return false;
    }

    /**
     * 检测交错接触（两个方块在边缘交错但不重叠）
     * 例如：2x2 方块 A 和 B 在垂直方向交错 1 格
     */
    private boolean isInterlockingContact(Building other) {
        int otherMinX = other.tile.x;
        int otherMinY = other.tile.y;
        int otherMaxX = other.tile.x + other.block.size - 1;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 检查是否在水平方向接触但垂直方向交错
        boolean horizontalContact = (maxX + 1 == otherMinX) || (minX == otherMaxX + 1);
        boolean verticalOverlap = (minY <= otherMaxY && maxY >= otherMinY);

        // 检查是否在垂直方向接触但水平方向交错
        boolean verticalContact = (maxY + 1 == otherMinY) || (minY == otherMaxY + 1);
        boolean horizontalOverlap = (minX <= otherMaxX && maxX >= otherMinX);

        // 如果水平接触且垂直完全覆盖，则为有效接触
        if (horizontalContact && verticalOverlap) {
            return isVerticalFullyOverlapped(other);
        }

        // 如果垂直接触且水平完全覆盖，则为有效接触
        if (verticalContact && horizontalOverlap) {
            return isHorizontalFullyOverlapped(other);
        }

        return false;
    }

    /**
     * 检查垂直方向是否完全重叠
     */
    private boolean isVerticalFullyOverlapped(Building other) {
        int otherMinY = other.tile.y;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 检查垂直范围是否完全覆盖
        return (minY <= otherMinY && maxY >= otherMaxY) ||
                (otherMinY <= minY && otherMaxY >= maxY) ||
                (minY >= otherMinY && maxY <= otherMaxY);
    }

    /**
     * 检查水平方向是否完全重叠
     */
    private boolean isHorizontalFullyOverlapped(Building other) {
        int otherMinX = other.tile.x;
        int otherMaxX = other.tile.x + other.block.size - 1;

        // 检查水平范围是否完全覆盖
        return (minX <= otherMinX && maxX >= otherMaxX) ||
                (otherMinX <= minX && otherMaxX >= maxX) ||
                (minX >= otherMinX && maxX <= otherMaxX);
    }

    /**
     * 检查对角方向是否完全对齐
     */
    private boolean isCornerFullyAligned(Building other, int dx, int dy) {
        int otherMinX = other.tile.x;
        int otherMinY = other.tile.y;
        int otherMaxX = other.tile.x + other.block.size - 1;
        int otherMaxY = other.tile.y + other.block.size - 1;

        // 检查角接触面积
        int contactWidth, contactHeight;

        // 右上对角（当前在左上，其他在右下）
        if (maxX + 1 == otherMinX && maxY + 1 == otherMinY) {
            contactWidth = Math.min(maxX - minX + 1, otherMaxX - otherMinX + 1);
            contactHeight = Math.min(maxY - minY + 1, otherMaxY - otherMinY + 1);
            return contactWidth >= 1 && contactHeight >= 1;
        }
        // 右下对角（当前在右上，其他在左下）
        else if (maxX + 1 == otherMinX && minY == otherMaxY + 1) {
            contactWidth = Math.min(maxX - minX + 1, otherMaxX - otherMinX + 1);
            contactHeight = Math.min(maxY - minY + 1, otherMaxY - otherMinY + 1);
            return contactWidth >= 1 && contactHeight >= 1;
        }
        // 左下对角（当前在右下，其他在左上）
        else if (minX == otherMaxX + 1 && maxY + 1 == otherMinY) {
            contactWidth = Math.min(maxX - minX + 1, otherMaxX - otherMinX + 1);
            contactHeight = Math.min(maxY - minY + 1, otherMaxY - otherMinY + 1);
            return contactWidth >= 1 && contactHeight >= 1;
        }
        // 左上对角（当前在左下，其他在右上）
        else if (minX == otherMaxX + 1 && minY == otherMaxY + 1) {
            contactWidth = Math.min(maxX - minX + 1, otherMaxX - otherMinX + 1);
            contactHeight = Math.min(maxY - minY + 1, otherMaxY - otherMinY + 1);
            return contactWidth >= 1 && contactHeight >= 1;
        }

        return false;
    }

    // 检查两个方块是否相邻（完全对齐版本）
    private boolean isAdjacentTo(Building other) {
        if (other == null || other.block != this.block) return false;
        return isFullyAlignedWith(other);
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

    // 检查某个方向是否有相邻的同类型方块（完全对齐版本）
    private boolean hasNeighborInDirection(Point2 dir) {
        int size = block.size;

        // 根据方向确定检查的边缘
        int startX, startY, endX, endY;

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
                    if (other != null && other != this && isFullyAlignedWith(other)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 获取指定方向上相邻的方块（完全对齐版本）
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
                        isFullyAlignedWith(wall)) {
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
                        isFullyAlignedWith(wall)) {
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