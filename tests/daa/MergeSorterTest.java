package daa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MergeSorterTest {

    private static void assertSortsLikeJdk(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);
        int[] actual = input.clone();
        new MergeSorter().sort(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    void emptyArray() {
        assertSortsLikeJdk(new int[0]);
    }

    @Test
    void singleElement() {
        assertSortsLikeJdk(new int[] {42});
    }

    @Test
    void twoElements() {
        assertSortsLikeJdk(new int[] {2, 1});
        assertSortsLikeJdk(new int[] {1, 2});
        assertSortsLikeJdk(new int[] {5, 5});
    }

    @Test
    void allEqual() {
        int[] a = new int[1000];
        Arrays.fill(a, 7);
        assertSortsLikeJdk(a);
    }

    @Test
    void extremeAndNegativeValues() {
        assertSortsLikeJdk(new int[] {Integer.MAX_VALUE, -1, Integer.MIN_VALUE, 0, -5, 5, Integer.MAX_VALUE, Integer.MIN_VALUE});
    }

    @Test
    void randomArraysOfManySizes() {
        Random rnd = new Random(1);
        for (int n = 0; n <= 300; n++) { // covers sizes below, at and above the insertion-sort cutoff
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = rnd.nextInt(2001) - 1000;
            }
            assertSortsLikeJdk(a);
        }
    }

    @Test
    void allInputTypesAndSizes() {
        for (Experiment.InputType type : Experiment.InputType.values()) {
            for (int n : new int[] {1, 2, 15, 16, 17, 100, 1_000, 10_000, 100_000}) {
                assertSortsLikeJdk(Experiment.generateInts(type, n, 123L + n));
            }
        }
    }

    @Test
    void usesOneReusableBuffer() {
        Metrics m = new Metrics();
        new MergeSorter(m).sort(Experiment.generateInts(Experiment.InputType.RANDOM, 50_000, 5L));
        assertEquals(1, m.allocations);
    }

    @Test
    void recursionDepthIsLogarithmic() {
        int n = 100_000;
        Metrics m = new Metrics();
        new MergeSorter(m).sort(Experiment.generateInts(Experiment.InputType.RANDOM, n, 9L));
        int log2 = 32 - Integer.numberOfLeadingZeros(n - 1); // ceil(log2 n)
        assertTrue(m.maxDepth() <= log2 + 1, "depth " + m.maxDepth());
    }

    @Test
    void comparisonsAreLinearithmic() {
        int n = 100_000;
        Metrics m = new Metrics();
        new MergeSorter(m).sort(Experiment.generateInts(Experiment.InputType.RANDOM, n, 11L));
        double nLogN = n * (Math.log(n) / Math.log(2));
        assertTrue(m.comparisons < 1.1 * nLogN, "comparisons " + m.comparisons);
    }
}
