package daa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DeterministicSelectorTest {

    private static int expectedKth(int[] a, int k) {
        int[] sorted = a.clone();
        Arrays.sort(sorted);
        return sorted[k];
    }

    @Test
    void singleElement() {
        assertEquals(5, new DeterministicSelector().select(new int[] {5}, 0));
    }

    @Test
    void hundredsOfRandomTestsAgainstSort() {
        Random rnd = new Random(2024);
        for (int test = 0; test < 300; test++) { // >= 100 random tests required by the assignment
            int n = 1 + rnd.nextInt(500);
            int range = test % 3 == 0 ? 5 : 1_000_000; // every third test is duplicate-heavy
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = rnd.nextInt(range) - range / 2;
            }
            int k = rnd.nextInt(n);
            int expected = expectedKth(a, k);
            assertEquals(expected, new DeterministicSelector().select(a.clone(), k), "test " + test + ", n=" + n + ", k=" + k);
        }
    }

    @Test
    void everyRankOfASmallArray() {
        int[] a = {9, 1, 8, 2, 7, 3, 6, 4, 5, 0, 11, 10, 13, 12, 14};
        for (int k = 0; k < a.length; k++) {
            assertEquals(expectedKth(a, k), new DeterministicSelector().select(a.clone(), k));
        }
    }

    @Test
    void minimumAndMaximum() {
        int[] a = Experiment.generateInts(Experiment.InputType.RANDOM, 10_000, 4L);
        assertEquals(expectedKth(a, 0), new DeterministicSelector().select(a.clone(), 0));
        assertEquals(expectedKth(a, a.length - 1), new DeterministicSelector().select(a.clone(), a.length - 1));
    }

    @Test
    void allInputTypes() {
        for (Experiment.InputType type : Experiment.InputType.values()) {
            for (int n : new int[] {1, 5, 6, 7, 100, 10_001, 100_000}) {
                int[] a = Experiment.generateInts(type, n, 77L + n);
                int k = n / 2;
                assertEquals(expectedKth(a, k), new DeterministicSelector().select(a.clone(), k), type + " n=" + n);
            }
        }
    }

    @Test
    void allEqualElements() {
        int[] a = new int[10_000];
        Arrays.fill(a, 3);
        assertEquals(3, new DeterministicSelector().select(a, 5_000));
    }

    @Test
    void keepsAllElements() { // select only permutes the array
        int[] a = Experiment.generateInts(Experiment.InputType.RANDOM, 5_000, 8L);
        int[] sortedBefore = a.clone();
        Arrays.sort(sortedBefore);
        new DeterministicSelector().select(a, 2_500);
        Arrays.sort(a);
        assertArrayEquals(sortedBefore, a);
    }

    @Test
    void invalidArgumentsAreRejected() {
        DeterministicSelector s = new DeterministicSelector();
        assertThrows(IllegalArgumentException.class, () -> s.select(new int[0], 0));
        assertThrows(IllegalArgumentException.class, () -> s.select(new int[] {1, 2, 3}, 3));
        assertThrows(IllegalArgumentException.class, () -> s.select(new int[] {1, 2, 3}, -1));
    }

    @Test
    void workGrowsLinearly() { // comparisons per element must stay roughly constant as n grows 10x
        double small = comparisonsPerElement(100_000);
        double large = comparisonsPerElement(1_000_000);
        assertTrue(large < small * 1.3, "per-element cost grew from " + small + " to " + large);
    }

    private static double comparisonsPerElement(int n) {
        Metrics m = new Metrics();
        new DeterministicSelector(m).select(Experiment.generateInts(Experiment.InputType.RANDOM, n, 6L), n / 2);
        return (double) m.comparisons / n;
    }
}
