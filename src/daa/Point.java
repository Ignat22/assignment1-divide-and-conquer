package daa;

/** Immutable point on the plane. */
public record Point(double x, double y) {

    public static double distanceSquared(Point p, Point q) {
        double dx = p.x - q.x;
        double dy = p.y - q.y;
        return dx * dx + dy * dy;
    }

    public static double distance(Point p, Point q) {
        return Math.sqrt(distanceSquared(p, q));
    }
}
