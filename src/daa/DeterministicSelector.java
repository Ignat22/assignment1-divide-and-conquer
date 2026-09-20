package daa;

/**
 * Deterministic Select (median of medians, Blum-Floyd-Pratt-Rivest-Tarjan).
 * <ol>
 *   <li>split the range into groups of 5 and sort each group (Insertion Sort);</li>
 *   <li>move the group medians to the front of the range and find their median recursively;</li>
 *   <li>use that "median of medians" as pivot for an in-place three-way partition;</li>
 *   <li>recurse only into the part that contains the k-th element.</li>
 * </ol>
 * Recurrence: T(n) &lt;= T(n/5) + T(7n/10 + 6) + O(n), which solves to Theta(n) in the worst case.
 * The array is rearranged in place (no auxiliary arrays).
 */
public final class DeterministicSelector {
    private static final int GROUP = 5;

    private final Metrics metrics;

    public DeterministicSelector() {
        this(new Metrics());
    }

    public DeterministicSelector(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Returns the k-th smallest element (0-based: k = 0 is the minimum, k = n-1 the maximum).
     * The array is permuted in place.
     *
     * @throws IllegalArgumentException if the array is empty or k is out of range
     */
    public int select(int[] a, int k) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("array must not be empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException("k must be in [0, " + (a.length - 1) + "], got " + k);
        }
        return select(a, 0, a.length - 1, k);
    }

    /** k-th smallest (absolute index k in [lo, hi]) of a[lo..hi] (both inclusive). */
    private int select(int[] a, int lo, int hi, int k) {
        metrics.enter();
        int result;
        if (hi - lo + 1 <= GROUP) {
            MergeSorter.insertionSort(a, lo, hi + 1, metrics);
            result = a[k];
        } else {
            int pivot = medianOfMedians(a, lo, hi);

            // three-way partition around the pivot value:
            // a[lo..lt-1] < pivot, a[lt..gt] == pivot, a[gt+1..hi] > pivot
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

            if (k < lt) {
                result = select(a, lo, lt - 1, k);
            } else if (k > gt) {
                result = select(a, gt + 1, hi, k);
            } else {
                result = pivot;
            }
        }
        metrics.exit();
        return result;
    }

    /** Returns the median of the group medians of a[lo..hi]. */
    private int medianOfMedians(int[] a, int lo, int hi) {
        int n = hi - lo + 1;
        int groups = (n + GROUP - 1) / GROUP;
        for (int g = 0; g < groups; g++) {
            int left = lo + g * GROUP;
            int right = Math.min(left + GROUP - 1, hi);
            MergeSorter.insertionSort(a, left, right + 1, metrics);
            int median = left + (right - left) / 2;
            swap(a, lo + g, median); // collect medians at a[lo..lo+groups-1]
        }
        return select(a, lo, lo + groups - 1, lo + (groups - 1) / 2);
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
