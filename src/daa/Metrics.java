package daa;

/**
 * Counters shared by all algorithms. One instance is used per single run.
 * <ul>
 *   <li>comparisons - element comparisons (for Closest Pair: distance checks + y-merge comparisons)</li>
 *   <li>swaps       - swaps / element moves (writes to an array)</li>
 *   <li>calls       - number of recursive invocations</li>
 *   <li>allocations - number of auxiliary arrays allocated</li>
 *   <li>maxDepth    - maximum recursion depth reached</li>
 * </ul>
 */
public final class Metrics {
    public long comparisons;
    public long swaps;
    public long calls;
    public long allocations;
    private int depth;
    private int maxDepth;

    /** Call at the start of every recursive invocation. */
    public void enter() {
        calls++;
        if (++depth > maxDepth) {
            maxDepth = depth;
        }
    }

    /** Call right before a recursive invocation returns. */
    public void exit() {
        depth--;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public void reset() {
        comparisons = swaps = calls = allocations = 0;
        depth = maxDepth = 0;
    }
}
