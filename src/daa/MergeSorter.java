package daa;

/**
 * Top-down MergeSort.
 * <ul>
 *   <li>linear merge of two sorted halves;</li>
 *   <li>one reusable auxiliary buffer, allocated once per sort() call;</li>
 *   <li>small-input cutoff: ranges of at most {@link #CUTOFF} elements use Insertion Sort;</li>
 *   <li>stable, Theta(n log n) time, O(n) extra space.</li>
 * </ul>
 */
public final class MergeSorter {
    /** Ranges of at most this size are sorted with Insertion Sort. */
    public static final int CUTOFF = 16;

    private final Metrics metrics;

    public MergeSorter() {
        this(new Metrics());
    }

    public MergeSorter(Metrics metrics) {
        this.metrics = metrics;
    }

    public void sort(int[] a) {
        if (a == null || a.length < 2) {
            return;
        }
        int[] buffer = new int[a.length]; // the only allocation: reused by every merge
        metrics.allocations++;
        sort(a, buffer, 0, a.length);
    }

    /** Sorts a[lo, hi) (hi exclusive). */
    private void sort(int[] a, int[] buffer, int lo, int hi) {
        metrics.enter();
        if (hi - lo <= CUTOFF) {
            insertionSort(a, lo, hi, metrics);
            metrics.exit();
            return;
        }
        int mid = (lo + hi) >>> 1;
        sort(a, buffer, lo, mid);
        sort(a, buffer, mid, hi);
        metrics.comparisons++;
        if (a[mid - 1] > a[mid]) { // halves already in order -> nothing to merge
            merge(a, buffer, lo, mid, hi);
        }
        metrics.exit();
    }

    /** Linear merge of sorted a[lo, mid) and a[mid, hi). Only the left half is copied to the buffer. */
    private void merge(int[] a, int[] buffer, int lo, int mid, int hi) {
        int leftLen = mid - lo;
        System.arraycopy(a, lo, buffer, lo, leftLen);
        int i = lo;          // reads the buffered left half
        int iEnd = mid;
        int j = mid;         // reads the right half directly from a
        int k = lo;          // write position (never overtakes j)
        long cmp = 0;
        while (i < iEnd && j < hi) {
            cmp++;
            if (buffer[i] <= a[j]) { // <= keeps the sort stable
                a[k++] = buffer[i++];
            } else {
                a[k++] = a[j++];
            }
        }
        while (i < iEnd) {
            a[k++] = buffer[i++];
        }
        // leftovers of the right half are already at their final positions
        metrics.comparisons += cmp;
        metrics.swaps += leftLen + (k - lo);
    }

    /** Insertion Sort of a[from, to) (to exclusive). Also used by DeterministicSelector for groups of 5. */
    static void insertionSort(int[] a, int from, int to, Metrics metrics) {
        long cmp = 0;
        long moves = 0;
        for (int i = from + 1; i < to; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= from) {
                cmp++;
                if (a[j] > key) {
                    a[j + 1] = a[j];
                    moves++;
                    j--;
                } else {
                    break;
                }
            }
            a[j + 1] = key;
            moves++;
        }
        metrics.comparisons += cmp;
        metrics.swaps += moves;
    }
}
