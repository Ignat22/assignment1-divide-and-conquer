package daa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class QuickSorterTest {

    private static void assertSortsLikeJdk(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);
        int[] actual = input.clone();
        new QuickSorter(new Metrics(), 99L).sort(actual);
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
        int[] a = new int[100_000];
        Arrays.fill(a, 7);
        assertSortsLikeJdk(a);
    }

    @Test
    void extremeAndNegativeValues() {
        assertSortsLikeJdk(new int[] {Integer.MAX_VALUE, -1, Integer.MIN_VALUE, 0, -5, 5, Integer.MAX_VALUE, Integer.MIN_VALUE});
    }

    @Test
    void randomArraysOfManySizes() {
        Random rnd = new Random(2);
        for (int n = 0; n <= 300; n++) {
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
                assertSortsLikeJdk(Experiment.generateInts(type, n, 321L + n));
            }
        }
    }

    @Test
    void recursionDepthIsAtMostLog2NPlusOneForEveryInputType() {
        int n = 200_000;
        int bound = (31 - Integer.numberOfLeadingZeros(n)) + 1; // floor(log2 n) + 1
        for (Experiment.InputType type : Experiment.InputType.values()) {
            Metrics m = new Metrics();
            new QuickSorter(m, 5L).sort(Experiment.generateInts(type, n, 17L));
            assertTrue(m.maxDepth() <= bound, type + ": depth " + m.maxDepth() + " > " + bound);
        }
    }

    @Test
    void sortedAndReverseSortedInputsAreNotQuadratic() {
        int n = 100_000;
        double nLogN = n * (Math.log(n) / Math.log(2));
        for (Experiment.InputType type : new Experiment.InputType[] {Experiment.InputType.SORTED, Experiment.InputType.REVERSE_SORTED}) {
            Metrics m = new Metrics();
            new QuickSorter(m, 3L).sort(Experiment.generateInts(type, n, 19L));
            assertTrue(m.comparisons < 6 * nLogN, type + ": comparisons " + m.comparisons);
        }
    }
}
