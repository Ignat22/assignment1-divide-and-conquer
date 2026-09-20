package daa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;
import org.junit.jupiter.api.Test;

class ClosestPairSolverTest {
    private static final double EPS = 1e-9;

    private static void assertMatchesBruteForce(Point[] points) {
        ClosestPairSolver.Result expected = ClosestPairSolver.bruteForce(points, new Metrics());
        ClosestPairSolver.Result actual = new ClosestPairSolver().solve(points);
        assertEquals(expected.distance(), actual.distance(), EPS);
        // the reported pair must really be at the reported distance
        assertEquals(actual.distance(), Point.distance(actual.a(), actual.b()), EPS);
    }

    @Test
    void twoPoints() {
        Point[] p = {new Point(0, 0), new Point(3, 4)};
        ClosestPairSolver.Result r = new ClosestPairSolver().solve(p);
        assertEquals(5.0, r.distance(), EPS);
    }

    @Test
    void threeAndFourPoints() {
        assertMatchesBruteForce(new Point[] {new Point(0, 0), new Point(10, 10), new Point(10, 11)});
        assertMatchesBruteForce(new Point[] {new Point(0, 0), new Point(5, 5), new Point(9, 9), new Point(9.5, 9.5)});
    }

    @Test
    void duplicatePointsGiveDistanceZero() {
        Point[] p = {new Point(1, 1), new Point(5, 5), new Point(1, 1), new Point(7, 2), new Point(9, 9)};
        assertEquals(0.0, new ClosestPairSolver().solve(p).distance(), EPS);
    }

    @Test
    void allPointsIdentical() {
        Point[] p = new Point[500];
        for (int i = 0; i < p.length; i++) {
            p[i] = new Point(2.5, -1.5);
        }
        assertEquals(0.0, new ClosestPairSolver().solve(p).distance(), EPS);
    }

    @Test
    void pairStraddlingTheDividingLineIsFound() {
        // the closest pair (49.9, 50) - (50.1, 50) lies on different sides of the median split
        Point[] p = new Point[100];
        for (int i = 0; i < 100; i++) {
            p[i] = new Point(i, (i % 2 == 0) ? 0 : 100);
        }
        p[49] = new Point(49.9, 50);
        p[50] = new Point(50.1, 50);
        assertMatchesBruteForce(p);
        assertEquals(0.2, new ClosestPairSolver().solve(p).distance(), EPS);
    }

    @Test
    void degenerateLayouts() {
        Random rnd = new Random(3);
        Point[] vertical = new Point[400];
        Point[] horizontal = new Point[400];
        Point[] diagonal = new Point[400];
        for (int i = 0; i < 400; i++) {
            vertical[i] = new Point(5, rnd.nextDouble() * 1000);   // same x for all points
            horizontal[i] = new Point(rnd.nextDouble() * 1000, 5); // same y for all points
            diagonal[i] = new Point(i * 1.5, i * 1.5);
        }
        assertMatchesBruteForce(vertical);
        assertMatchesBruteForce(horizontal);
        assertMatchesBruteForce(diagonal);
    }

    @Test
    void manyRandomTestsUpTo2000PointsAgainstBruteForce() {
        Random rnd = new Random(11);
        for (int test = 0; test < 200; test++) {
            int n = 2 + rnd.nextInt(1999); // n <= 2000, as required by the assignment
            Point[] p = new Point[n];
            for (int i = 0; i < n; i++) {
                p[i] = new Point(rnd.nextDouble() * 1000 - 500, rnd.nextDouble() * 1000 - 500);
            }
            assertMatchesBruteForce(p);
        }
    }

    @Test
    void integerGridWithManyTies() {
        Random rnd = new Random(12);
        for (int test = 0; test < 100; test++) {
            int n = 2 + rnd.nextInt(1000);
            int side = 2 + rnd.nextInt(60);
            Point[] p = new Point[n];
            for (int i = 0; i < n; i++) {
                p[i] = new Point(rnd.nextInt(side), rnd.nextInt(side));
            }
            assertMatchesBruteForce(p);
        }
    }

    @Test
    void allInputTypes() {
        for (Experiment.InputType type : Experiment.InputType.values()) {
            for (int n : new int[] {2, 3, 4, 5, 10, 100, 1_000, 2_000}) {
                assertMatchesBruteForce(Experiment.generatePoints(type, n, 55L + n));
            }
        }
    }

    @Test
    void inputArrayIsNotModified() {
        Point[] p = Experiment.generatePoints(Experiment.InputType.RANDOM, 500, 1L);
        Point[] copy = p.clone();
        new ClosestPairSolver().solve(p);
        assertArrayEquals(copy, p);
    }

    @Test
    void fewerThanTwoPointsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ClosestPairSolver().solve(new Point[0]));
        assertThrows(IllegalArgumentException.class, () -> new ClosestPairSolver().solve(new Point[] {new Point(1, 1)}));
    }

    @Test
    void largeInputRunsWithFastImplementationOnly() {
        Point[] p = Experiment.generatePoints(Experiment.InputType.RANDOM, 200_000, 9L);
        ClosestPairSolver.Result r = new ClosestPairSolver().solve(p);
        assertEquals(r.distance(), Point.distance(r.a(), r.b()), EPS);
    }
}
