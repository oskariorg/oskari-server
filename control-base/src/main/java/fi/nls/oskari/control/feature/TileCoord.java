package fi.nls.oskari.control.feature;

public class TileCoord {

    private final int z;
    private final int x;
    private final int y;

    public TileCoord(int z, int x, int y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
