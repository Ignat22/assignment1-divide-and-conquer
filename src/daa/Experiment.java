package daa;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Runs the benchmarks: every algorithm on every input type and size, measuring time with
 * System.nanoTime(), the maximum recursion depth and extra counters (see {@link Metrics}).
 * The reported time is the median of several repetitions (input copying is not timed).
 */
public final class Experiment {

    public enum InputType {
        RANDOM, SORTED, REVERSE_SORTED, DUPLICATE_HEAVY
    }

    /** One line of results.csv. */
    public record Row(String algorithm, InputType inputType, int n, double timeMs, int maxDepth,
                      long comparisons, long swaps, long calls, long allocations) {
        String toCsv() {
            return String.format(Locale.ROOT, "%s,%s,%d,%.4f,%d,%d,%d,%d,%d",
                    algorithm, inputType, n, timeMs, maxDepth, comparisons, swaps, calls, allocations);
        }
    }

    public static final String CSV_HEADER =
            "algorithm,inputType,n,timeMs,maxDepth,comparisons,swaps,calls,allocations";

    /** Largest n for which the O(n^2) brute-force closest pair is also measured. */
    private static final int BRUTE_FORCE_MAX_N = 20_000;

    private interface IntAlgorithm {
        void run(int[] a, Metrics m, long seed);
    }

    private interface PointAlgorithm {
        void run(Point[] p, Metrics m);
    }

    private static volatile long sink; // keeps the JIT from discarding results

    private final long seed;
    private final PrintStream log;

    public Experiment(long seed, PrintStream log) {
        this.seed = seed;
        this.log = log;
    }

    // ------------------------------------------------------------------ input generation

    static int[] generateInts(InputType type, int n, long seed) {
        Random rnd = new Random(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = type == InputType.DUPLICATE_HEAVY ? rnd.nextInt(100) : rnd.nextInt(1_000_000_000);
        }
        if (type == InputType.SORTED) {
            Arrays.sort(a);
        } else if (type == InputType.REVERSE_SORTED) {
            Arrays.sort(a);
            for (int i = 0, j = n - 1; i < j; i++, j--) {
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        return a;
    }

    static Point[] generatePoints(InputType type, int n, long seed) {
        Random rnd = new Random(seed);
        Point[] p = new Point[n];
        for (int i = 0; i < n; i++) {
            if (type == InputType.DUPLICATE_HEAVY) {
                p[i] = new Point(rnd.nextInt(1000), rnd.nextInt(1000)); // small integer grid -> many equal points
            } else {
                p[i] = new Point(rnd.nextDouble() * 1_000_000, rnd.nextDouble() * 1_000_000);
            }
        }
        if (type == InputType.SORTED) {
            Arrays.sort(p, Comparator.comparingDouble(Point::x));
        } else if (type == InputType.REVERSE_SORTED) {
            Arrays.sort(p, Comparator.comparingDouble(Point::x).reversed());
        }
        return p;
    }

    // ------------------------------------------------------------------ running

    public List<Row> runAll(int[] sizes) {
        warmUp();
        // extra sweep over the smaller sizes whose results are discarded: lets the JIT settle on all code paths
        sweep(Arrays.stream(sizes).filter(s -> s <= 100_000).toArray(), false);
        return sweep(sizes, true);
    }

    private List<Row> sweep(int[] sizes, boolean report) {
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            for (InputType type : InputType.values()) {
                int[] ints = generateInts(type, n, seed + n);
                add(rows, report, measureInts("ArraysSort", type, ints, false, (a, m, s) -> Arrays.sort(a)));
                add(rows, report, measureInts("MergeSort", type, ints, true, (a, m, s) -> new MergeSorter(m).sort(a)));
                add(rows, report, measureInts("QuickSort", type, ints, true, (a, m, s) -> new QuickSorter(m, s).sort(a)));
                add(rows, report, measureInts("DeterministicSelect", type, ints, false,
                        (a, m, s) -> sink = new DeterministicSelector(m).select(a, a.length / 2)));

                Point[] points = generatePoints(type, n, seed + n);
                add(rows, report, measurePoints("ClosestPair", type, points,
                        (p, m) -> sink = Double.doubleToLongBits(new ClosestPairSolver(m).solve(p).distance())));
                if (n <= BRUTE_FORCE_MAX_N) {
                    add(rows, report, measurePoints("ClosestPairBrute", type, points,
                            (p, m) -> sink = Double.doubleToLongBits(ClosestPairSolver.bruteForce(p, m).distance())));
                }
            }
        }
        return rows;
    }

    private void add(List<Row> rows, boolean report, Row row) {
        rows.add(row);
        if (report && log != null) {
            log.printf(Locale.ROOT, "%-20s %-15s n=%-8d time=%10.3f ms  depth=%-3d cmp=%-12d swaps=%-12d calls=%d%n",
                    row.algorithm(), row.inputType(), row.n(), row.timeMs(), row.maxDepth(),
                    row.comparisons(), row.swaps(), row.calls());
        }
    }

    /** Lets the JIT compile all algorithms before anything is measured. */
    private void warmUp() {
        for (int i = 0; i < 15; i++) {
            int[] a = generateInts(InputType.RANDOM, 20_000, seed + i);
            new MergeSorter().sort(a.clone());
            new QuickSorter(new Metrics(), i).sort(a.clone());
            sink = new DeterministicSelector().select(a.clone(), a.length / 2);
            Arrays.sort(a.clone());
            Point[] p = generatePoints(InputType.RANDOM, 5_000, seed + i);
            sink = Double.doubleToLongBits(new ClosestPairSolver().solve(p).distance());
            sink = Double.doubleToLongBits(ClosestPairSolver.bruteForce(p, new Metrics()).distance());
        }
    }

    private static int repetitions(int n) {
        if (n <= 20_000) {
            return 9;
        }
        if (n <= 200_000) {
            return 7;
        }
        return n <= 500_000 ? 5 : 3;
    }

    private Row measureInts(String name, InputType type, int[] input, boolean verifySorted, IntAlgorithm algo) {
        int reps = repetitions(input.length);
        double[] times = new double[reps];
        Metrics last = null;
        System.gc();
        for (int r = 0; r < reps; r++) {
            int[] a = input.clone();          // copying is not timed
            Metrics m = new Metrics();
            long start = System.nanoTime();
            algo.run(a, m, seed + r);
            long end = System.nanoTime();
            times[r] = (end - start) / 1e6;
            last = m;
            if (verifySorted && !isSorted(a)) {
                throw new IllegalStateException(name + " produced an unsorted array (n=" + a.length + ")");
            }
        }
        return toRow(name, type, input.length, times, last);
    }

    private Row measurePoints(String name, InputType type, Point[] input, PointAlgorithm algo) {
        int reps = repetitions(input.length);
        double[] times = new double[reps];
        Metrics last = null;
        System.gc();
        for (int r = 0; r < reps; r++) {
            Metrics m = new Metrics();
            long start = System.nanoTime();
            algo.run(input, m);               // solve() clones the input itself (counted as allocation)
            long end = System.nanoTime();
            times[r] = (end - start) / 1e6;
            last = m;
        }
        return toRow(name, type, input.length, times, last);
    }

    private static Row toRow(String name, InputType type, int n, double[] times, Metrics m) {
        Arrays.sort(times);
        double median = times[times.length / 2];
        return new Row(name, type, n, median, m.maxDepth(), m.comparisons, m.swaps, m.calls, m.allocations);
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ output

    public static void writeCsv(List<Row> rows, Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Row row : rows) {
            lines.add(row.toCsv());
        }
        Files.write(file, lines);
    }
}
