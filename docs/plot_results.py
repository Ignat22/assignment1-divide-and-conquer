"""Builds the plots (docs/plots/*.png) and the Markdown tables (results/tables.md) from results/results.csv
and refreshes the tables inside README.md (between the <!-- BEGIN:name --> / <!-- END:name --> markers).

Usage (from the repository root):
    pip install pandas matplotlib
    python docs/plot_results.py
"""
import re
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

ROOT = Path(__file__).resolve().parent.parent
df = pd.read_csv(ROOT / "results" / "results.csv")
plots = ROOT / "docs" / "plots"
plots.mkdir(parents=True, exist_ok=True)

TYPES = ["RANDOM", "SORTED", "REVERSE_SORTED", "DUPLICATE_HEAVY"]
MAIN = ["MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"]

# ---------------------------------------------------------------- 1. time vs n (per algorithm, per input type)
fig, axes = plt.subplots(2, 2, figsize=(11, 8))
for ax, algo in zip(axes.ravel(), MAIN):
    for t in TYPES:
        sub = df[(df.algorithm == algo) & (df.inputType == t)].sort_values("n")
        ax.plot(sub.n, sub.timeMs, marker="o", ms=3, label=t.lower().replace("_", "-"))
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_title(algo)
    ax.set_xlabel("n")
    ax.set_ylabel("time, ms")
    ax.grid(True, which="both", alpha=0.3)
axes[0, 0].legend(title="input type")
fig.suptitle("Execution time vs n (log-log, median of several runs)")
fig.tight_layout()
fig.savefig(plots / "time_vs_n.png", dpi=150)
plt.close(fig)

# ---------------------------------------------------------------- 2. recursion depth vs n
fig, ax = plt.subplots(figsize=(7.5, 5))
rnd = df[df.inputType == "RANDOM"]
for algo in MAIN:
    sub = rnd[rnd.algorithm == algo].sort_values("n")
    ax.plot(sub.n, sub.maxDepth, marker="o", ms=4, label=algo)
ns = np.array(sorted(rnd.n.unique()))
ax.plot(ns, np.log2(ns), "k--", lw=1, label="log2(n)")
ax.set_xscale("log")
ax.set_xlabel("n")
ax.set_ylabel("maximum recursion depth")
ax.set_title("Maximum recursion depth vs n (random input)")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig(plots / "depth_vs_n.png", dpi=150)
plt.close(fig)

# ---------------------------------------------------------------- 3. time normalised by the theoretical bound
fig, ax = plt.subplots(figsize=(7.5, 5))
for algo in MAIN:
    sub = rnd[rnd.algorithm == algo].sort_values("n")
    bound = sub.n if algo == "DeterministicSelect" else sub.n * np.log2(sub.n)
    label = f"{algo}  (time / n)" if algo == "DeterministicSelect" else f"{algo}  (time / n log2 n)"
    ax.plot(sub.n, sub.timeMs * 1e6 / bound, marker="o", ms=4, label=label)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("ns per unit of the theoretical bound")
ax.set_title("Time divided by the predicted bound (random input)\nflat line = theory confirmed")
ax.grid(True, which="both", alpha=0.3)
ax.legend(fontsize=8)
fig.tight_layout()
fig.savefig(plots / "time_normalized.png", dpi=150)
plt.close(fig)

# ---------------------------------------------------------------- 4. closest pair vs brute force
fig, ax = plt.subplots(figsize=(7.5, 5))
for algo, style in (("ClosestPair", "o-"), ("ClosestPairBrute", "s--")):
    sub = rnd[rnd.algorithm == algo].sort_values("n")
    ax.plot(sub.n, sub.timeMs, style, ms=4, label=algo + (" O(n log n)" if algo == "ClosestPair" else " O(n^2)"))
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlabel("n")
ax.set_ylabel("time, ms")
ax.set_title("Closest pair: divide and conquer vs brute force (random input)")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig(plots / "closest_pair_vs_brute_force.png", dpi=150)
plt.close(fig)

# ---------------------------------------------------------------- Markdown tables
def md(table: pd.DataFrame, floatfmt: str) -> str:
    cols = [table.index.name or ""] + [str(c) for c in table.columns]
    lines = ["| " + " | ".join(cols) + " |", "|" + "|".join(["---"] * len(cols)) + "|"]
    for idx, row in table.iterrows():
        cells = [f"{idx:,}" if isinstance(idx, (int, np.integer)) else str(idx)]
        for v in row:
            cells.append("–" if pd.isna(v) else (floatfmt.format(v) if isinstance(v, (float, np.floating)) else f"{int(v):,}"))
        lines.append("| " + " | ".join(cells) + " |")
    return "\n".join(lines)


order = ["ArraysSort", "MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair", "ClosestPairBrute"]
tables = {}  # marker name -> (heading, markdown table)
t = rnd.pivot(index="n", columns="algorithm", values="timeMs")[order]
tables["time"] = ("Time vs n, random input (ms)", md(t, "{:.3f}"))
d = rnd[rnd.algorithm.isin(MAIN)].pivot(index="n", columns="algorithm", values="maxDepth")[MAIN]
tables["depth"] = ("Max recursion depth vs n, random input", md(d, "{}"))
for n, key in ((100_000, "100k"), (1_000_000, "1m")):
    sub = df[(df.n == n) & df.algorithm.isin(order)]
    tt = sub.pivot(index="algorithm", columns="inputType", values="timeMs")[TYPES].reindex([a for a in order if a in sub.algorithm.values])
    tables["type_" + key] = (f"Time by input type, n = {n:,} (ms)", md(tt, "{:.2f}"))
    dd = sub[sub.algorithm.isin(MAIN)].pivot(index="algorithm", columns="inputType", values="maxDepth")[TYPES].reindex(MAIN)
    tables["depth_type_" + key] = (f"Max recursion depth by input type, n = {n:,}", md(dd, "{}"))
m = df[(df.n == 1_000_000) & (df.inputType == "RANDOM") & df.algorithm.isin(MAIN)].set_index("algorithm")[
    ["comparisons", "swaps", "calls", "allocations"]].reindex(MAIN)
tables["metrics"] = ("Extra metrics, n = 1,000,000, random input", md(m, "{}"))

out = []
for heading, table in tables.values():
    out += [f"### {heading}", table, ""]
(ROOT / "results" / "tables.md").write_text("\n".join(out), encoding="utf-8")

readme = ROOT / "README.md"
if readme.exists():
    text = readme.read_text(encoding="utf-8")
    for name, (_, table) in tables.items():
        pattern = re.compile(r"<!-- BEGIN:%s -->.*?<!-- END:%s -->" % (name, name), re.DOTALL)
        block = "<!-- BEGIN:%s -->\n%s\n<!-- END:%s -->" % (name, table, name)
        text = pattern.sub(lambda _: block, text)
    readme.write_text(text, encoding="utf-8")
print("plots  ->", plots)
print("tables -> results/tables.md and README.md")
