package daa;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point.
 * <pre>
 *   java -cp target/classes daa.Main             full experiment  -> results/results.csv
 *   java -cp target/classes daa.Main --quick     small sizes only (a few seconds)
 *   java -cp target/classes daa.Main --out file  custom CSV path
 * </pre>
 */
public final class Main {

    private static final int[] FULL_SIZES =
            {1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 1_000_000};
    private static final int[] QUICK_SIZES = {1_000, 10_000, 100_000};

    public static void main(String[] args) throws IOException {
        List<String> argList = Arrays.asList(args);
        boolean quick = argList.contains("--quick");
        int outIndex = argList.indexOf("--out");
        Path out = Path.of(outIndex >= 0 && outIndex + 1 < args.length ? args[outIndex + 1] : "results/results.csv");

        System.out.println("=== Assignment 1: Divide-and-Conquer Algorithm Analysis ===");
        System.out.println("Java " + System.getProperty("java.version") + ", " + System.getProperty("os.name"));
        System.out.println();
        demo();

        System.out.println();
        System.out.println("=== Experiments (median of several runs, time in ms) ===");
        Experiment experiment = new Experiment(42L, System.out);
        List<Experiment.Row> rows = experiment.runAll(quick ? QUICK_SIZES : FULL_SIZES);

        Experiment.writeCsv(rows, out);
        System.out.println();
        System.out.println("Saved " + rows.size() + " rows to " + out);
    }

    /** Small, human-readable demonstration of all four algorithms. */
    private static void demo() {
        int[] data = {38, 27, 43, 3, 9, 82, 10, 3, 55, 1, 27, 64, 19, 8, 90, 42, 5, 77, 31, 12};
        System.out.println("Input array      : " + Arrays.toString(data));

        int[] merged = data.clone();
        new MergeSorter().sort(merged);
        System.out.println("MergeSort        : " + Arrays.toString(merged));

        int[] quick = data.clone();
        new QuickSorter().sort(quick);
        System.out.println("QuickSort        : " + Arrays.toString(quick));

        int k = data.length / 2;
        int kth = new DeterministicSelector().select(data.clone(), k);
        System.out.println("Select (k=" + k + ")    : " + kth + "   (sorted[" + k + "] = " + merged[k] + ")");

        Point[] points = Experiment.generatePoints(Experiment.InputType.RANDOM, 12, 7L);
        ClosestPairSolver.Result fast = new ClosestPairSolver().solve(points);
        ClosestPairSolver.Result slow = ClosestPairSolver.bruteForce(points, new Metrics());
        System.out.printf("Closest pair     : %s <-> %s, distance = %.4f%n", fast.a(), fast.b(), fast.distance());
        System.out.printf("Brute force check: distance = %.4f  (%s)%n", slow.distance(),
                Math.abs(fast.distance() - slow.distance()) < 1e-9 ? "match" : "MISMATCH");
    }

    private Main() {
    }
}
