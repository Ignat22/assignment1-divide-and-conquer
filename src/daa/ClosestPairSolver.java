package daa;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Closest pair of points on the plane, divide and conquer, Theta(n log n).
 * <ol>
 *   <li>sort the points by x once;</li>
 *   <li>split at the median x, solve both halves recursively (each half comes back sorted by y,
 *       merged in linear time - no re-sorting at every level);</li>
 *   <li>let d be the best distance found so far; build the strip of points with |x - midX| &lt; d
 *       (already in y order) and compare every strip point only with the following points whose
 *       y-distance is below d (at most 7 of them).</li>
 * </ol>
 * Recurrence: T(n) = 2T(n/2) + O(n) = Theta(n log n).
 * Squared distances are used internally; the square root is taken once at the end.
 */
public final class ClosestPairSolver {

    /** The closest pair and the distance between the two points. */
    public record Result(Point a, Point b, double distance) {
    }

    private static final int BRUTE_FORCE_LIMIT = 3;

    private final Metrics metrics;

    // working state of one solve() call
    private Point[] pts;      // points, sorted by x; ranges become sorted by y while recursion unwinds
    private Point[] buffer;   // merge buffer
    private Point[] strip;    // strip buffer
    private double bestSq;
    private Point bestA;
    private Point bestB;

    public ClosestPairSolver() {
        this(new Metrics());
    }

    public ClosestPairSolver(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Finds the closest pair. The input array is not modified.
     *
     * @throws IllegalArgumentException if fewer than two points are given
     */
    public Result solve(Point[] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("at least two points are required");
        }
        int n = points.length;
        pts = points.clone();
        buffer = new Point[n];
        strip = new Point[n];
        metrics.allocations += 3;
        Arrays.sort(pts, Comparator.comparingDouble(Point::x).thenComparingDouble(Point::y));

        bestSq = Double.POSITIVE_INFINITY;
        bestA = null;
        bestB = null;
        solve(0, n);

        Result result = new Result(bestA, bestB, Math.sqrt(bestSq));
        pts = buffer = strip = null; // do not keep the data alive
        return result;
    }

    /** O(n^2) reference implementation used for testing and for the experiments. */
    public static Result bruteForce(Point[] points, Metrics metrics) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("at least two points are required");
        }
        double best = Double.POSITIVE_INFINITY;
        Point ba = null;
        Point bb = null;
        long cmp = 0;
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                cmp++;
                double d = Point.distanceSquared(points[i], points[j]);
                if (d < best) {
                    best = d;
                    ba = points[i];
                    bb = points[j];
                }
            }
        }
        metrics.comparisons += cmp;
        return new Result(ba, bb, Math.sqrt(best));
    }

    /** Solves pts[lo, hi); on return that range is sorted by y. */
    private void solve(int lo, int hi) {
        metrics.enter();
        int n = hi - lo;
        if (n <= BRUTE_FORCE_LIMIT) {
            for (int i = lo; i < hi; i++) {
                for (int j = i + 1; j < hi; j++) {
                    check(pts[i], pts[j]);
                }
            }
            sortByY(lo, hi);
            metrics.exit();
            return;
        }

        int mid = (lo + hi) >>> 1;
        double midX = pts[mid].x(); // read BEFORE the recursion reorders the range by y
        solve(lo, mid);
        solve(mid, hi);
        mergeByY(lo, mid, hi);

        // strip: points closer than d to the dividing line, in y order
        int size = 0;
        for (int i = lo; i < hi; i++) {
            double dx = pts[i].x() - midX;
            if (dx * dx < bestSq) {
                strip[size++] = pts[i];
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double dy = strip[j].y() - strip[i].y();
                if (dy * dy >= bestSq) {
                    break; // all following points are even farther in y
                }
                check(strip[i], strip[j]);
            }
        }
        metrics.exit();
    }

    private void check(Point p, Point q) {
        metrics.comparisons++;
        double d = Point.distanceSquared(p, q);
        if (d < bestSq) {
            bestSq = d;
            bestA = p;
            bestB = q;
        }
    }

    private void sortByY(int lo, int hi) { // tiny ranges: insertion sort
        for (int i = lo + 1; i < hi; i++) {
            Point key = pts[i];
            int j = i - 1;
            while (j >= lo && pts[j].y() > key.y()) {
                pts[j + 1] = pts[j];
                j--;
            }
            pts[j + 1] = key;
        }
    }

    private void mergeByY(int lo, int mid, int hi) {
        int i = lo;
        int j = mid;
        int k = lo;
        long cmp = 0;
        while (i < mid && j < hi) {
            cmp++;
            if (pts[i].y() <= pts[j].y()) {
                buffer[k++] = pts[i++];
            } else {
                buffer[k++] = pts[j++];
            }
        }
        while (i < mid) {
            buffer[k++] = pts[i++];
        }
        while (j < hi) {
            buffer[k++] = pts[j++];
        }
        System.arraycopy(buffer, lo, pts, lo, hi - lo);
        metrics.comparisons += cmp;
        metrics.swaps += 2L * (hi - lo);
    }
}
