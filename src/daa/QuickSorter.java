package daa;

import java.util.Random;

/**
 * Randomized QuickSort.
 * <ul>
 *   <li>random pivot;</li>
 *   <li>in-place three-way partitioning (Dijkstra's Dutch national flag), so equal keys are never
 *       recursed into and duplicate-heavy inputs stay fast;</li>
 *   <li>recurses only into the SMALLER part and iterates over the larger one, so the recursion
 *       depth is at most floor(log2 n) + 1 for every input;</li>
 *   <li>expected O(n log n) time, worst case O(n^2) (with negligible probability), O(log n) stack.</li>
 * </ul>
 */
public final class QuickSorter {
    private final Metrics metrics;
    private final Random random;

    public QuickSorter() {
        this(new Metrics(), System.nanoTime());
    }

    public QuickSorter(Metrics metrics, long seed) {
        this.metrics = metrics;
        this.random = new Random(seed);
    }

    public void sort(int[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        sort(a, 0, a.length - 1);
    }

    /** Sorts a[lo..hi] (both inclusive). */
    private void sort(int[] a, int lo, int hi) {
        metrics.enter();
        while (lo < hi) {
            int pivot = a[lo + random.nextInt(hi - lo + 1)];

            // Invariant: a[lo..lt-1] < pivot, a[lt..i-1] == pivot, a[gt+1..hi] > pivot, a[i..gt] unknown
            int lt = lo;
            int i = lo;
            int gt = hi;
            long cmp = 0;
            while (i <= gt) {
                int x = a[i];
                cmp++;
                if (x < pivot) {
                    swap(a, lt++, i++);
                } else {
                    cmp++;
                    if (x > pivot) {
                        swap(a, i, gt--);
                    } else {
                        i++;
                    }
                }
            }
            metrics.comparisons += cmp;

            // a[lt..gt] is final. Recurse into the smaller side, loop over the larger one.
            if (lt - lo < hi - gt) {
                sort(a, lo, lt - 1);
                lo = gt + 1;
            } else {
                sort(a, gt + 1, hi);
                hi = lt - 1;
            }
        }
        metrics.exit();
    }

    private void swap(int[] a, int i, int j) {
        if (i != j) {
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
            metrics.swaps++;
        }
    }
}
