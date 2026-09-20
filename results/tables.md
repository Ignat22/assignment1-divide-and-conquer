### Time vs n, random input (ms)
| n | ArraysSort | MergeSort | QuickSort | DeterministicSelect | ClosestPair | ClosestPairBrute |
|---|---|---|---|---|---|---|
| 1,000 | 0.023 | 0.046 | 0.071 | 0.030 | 0.309 | 0.562 |
| 2,000 | 0.065 | 0.106 | 0.144 | 0.070 | 0.600 | 2.529 |
| 5,000 | 0.193 | 0.296 | 0.395 | 0.176 | 1.702 | 16.237 |
| 10,000 | 0.409 | 0.636 | 0.842 | 0.362 | 3.863 | 68.147 |
| 20,000 | 0.883 | 1.411 | 1.801 | 0.717 | 8.877 | 281.986 |
| 50,000 | 2.504 | 3.872 | 4.758 | 1.775 | 22.367 | – |
| 100,000 | 5.084 | 8.001 | 10.072 | 3.520 | 48.520 | – |
| 200,000 | 10.863 | 17.084 | 21.336 | 6.681 | 116.289 | – |
| 500,000 | 29.967 | 45.688 | 56.871 | 17.384 | 438.515 | – |
| 1,000,000 | 62.009 | 97.452 | 119.435 | 33.755 | 1009.185 | – |

### Max recursion depth vs n, random input
| n | MergeSort | QuickSort | DeterministicSelect | ClosestPair |
|---|---|---|---|---|
| 1,000 | 7 | 7 | 8 | 10 |
| 2,000 | 8 | 8 | 10 | 11 |
| 5,000 | 10 | 9 | 11 | 12 |
| 10,000 | 11 | 10 | 12 | 13 |
| 20,000 | 12 | 10 | 14 | 14 |
| 50,000 | 13 | 11 | 15 | 16 |
| 100,000 | 14 | 11 | 15 | 17 |
| 200,000 | 15 | 12 | 16 | 18 |
| 500,000 | 16 | 14 | 19 | 19 |
| 1,000,000 | 17 | 15 | 18 | 20 |

### Time by input type, n = 100,000 (ms)
| algorithm | RANDOM | SORTED | REVERSE_SORTED | DUPLICATE_HEAVY |
|---|---|---|---|---|
| ArraysSort | 5.08 | 0.02 | 0.07 | 2.07 |
| MergeSort | 8.00 | 0.25 | 2.26 | 5.55 |
| QuickSort | 10.07 | 6.58 | 6.33 | 3.13 |
| DeterministicSelect | 3.52 | 1.98 | 2.08 | 1.57 |
| ClosestPair | 48.52 | 23.43 | 23.18 | 45.84 |

### Max recursion depth by input type, n = 100,000
| algorithm | RANDOM | SORTED | REVERSE_SORTED | DUPLICATE_HEAVY |
|---|---|---|---|---|
| MergeSort | 14 | 14 | 14 | 14 |
| QuickSort | 11 | 12 | 12 | 6 |
| DeterministicSelect | 15 | 16 | 15 | 10 |
| ClosestPair | 17 | 17 | 17 | 17 |

### Time by input type, n = 1,000,000 (ms)
| algorithm | RANDOM | SORTED | REVERSE_SORTED | DUPLICATE_HEAVY |
|---|---|---|---|---|
| ArraysSort | 62.01 | 0.27 | 0.77 | 19.97 |
| MergeSort | 97.45 | 2.53 | 25.16 | 58.82 |
| QuickSort | 119.44 | 72.93 | 74.18 | 31.79 |
| DeterministicSelect | 33.75 | 19.27 | 19.99 | 26.07 |
| ClosestPair | 1009.18 | 528.67 | 553.09 | 956.56 |

### Max recursion depth by input type, n = 1,000,000
| algorithm | RANDOM | SORTED | REVERSE_SORTED | DUPLICATE_HEAVY |
|---|---|---|---|---|
| MergeSort | 17 | 17 | 17 | 17 |
| QuickSort | 15 | 14 | 14 | 5 |
| DeterministicSelect | 18 | 19 | 18 | 12 |
| ClosestPair | 20 | 20 | 20 | 20 |

### Extra metrics, n = 1,000,000, random input
| algorithm | comparisons | swaps | calls | allocations |
|---|---|---|---|---|
| MergeSort | 20,286,405 | 28,417,087 | 131,071 | 1 |
| QuickSort | 37,929,875 | 18,518,898 | 666,521 | 0 |
| DeterministicSelect | 10,181,198 | 9,773,627 | 6,725 | 0 |
| ClosestPair | 18,689,972 | 37,708,544 | 951,423 | 3 |
