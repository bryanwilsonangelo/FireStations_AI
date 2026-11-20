import csv
import glob
import math
import os
import sys
from collections import defaultdict


def parse_float(val, default=None):
    if val is None:
        return default
    s = str(val).strip()
    if not s or s.lower() in ("nan", "inf", "-inf", "null", "none"):
        return default
    try:
        return float(s)
    except ValueError:
        return default


def parse_int(val, default=None):
    f = parse_float(val, None)
    if f is None:
        return default
    return int(round(f))


def detect_column(fieldnames, candidates):
    lower_map = {name.lower().strip(): name for name in fieldnames}
    for cand in candidates:
        if cand.lower() in lower_map:
            return lower_map[cand.lower()]
    for name in fieldnames:
        low = name.lower()
        for cand in candidates:
            if cand.lower() in low:
                return name
    return None


def mean(values):
    return sum(values) / len(values) if values else float("inf")


def std(values):
    if len(values) < 2:
        return 0.0
    m = mean(values)
    variance = sum((x - m) ** 2 for x in values) / (len(values) - 1)
    return math.sqrt(variance)


def load_tahap2b_rows(pattern="results/m*_tahap2b.csv"):
    files = sorted(glob.glob(pattern))
    if not files:
        raise FileNotFoundError(f"Tidak ditemukan file yang cocok dengan: {pattern}")

    grouped = defaultdict(lambda: {"costs": [], "fitnesses": [], "evals": [], "times": [], "files": set()})
    loaded_files = []

    for path in files:
        with open(path, mode="r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)
            if not reader.fieldnames:
                continue

            col_model = detect_column(reader.fieldnames, ["model", "model_id", "id_model", "mode"])
            col_pop = detect_column(reader.fieldnames, ["pop", "pop_size", "population", "ukuran_pop"])
            col_pc = detect_column(reader.fieldnames, ["pc", "crossover_rate", "cr"])
            col_pm = detect_column(reader.fieldnames, ["pm", "mutation_rate", "mr"])
            col_elit = detect_column(reader.fieldnames, ["elit", "elitism", "elit_rate", "e"])
            col_cost = detect_column(reader.fieldnames, ["best_cost", "cost", "objective", "objektif"])
            col_fit = detect_column(reader.fieldnames, ["best_fitness", "fitness"])
            col_eval = detect_column(reader.fieldnames, ["evals", "evaluations", "eval_count", "n_eval"])
            col_time = detect_column(reader.fieldnames, ["time_ms", "duration_ms", "elapsed_ms", "waktu_ms", "time"])

            if not col_cost:
                raise ValueError(
                    f"Kolom cost tidak ditemukan di {path}. Kolom tersedia: {reader.fieldnames}"
                )

            # Fallback nomor model dari nama file jika kolom model tidak ada (misal m1_tahap2b.csv -> 1)
            fname_model = None
            base = os.path.basename(path).lower()
            if base.startswith("m") and len(base) > 1 and base[1].isdigit():
                fname_model = int(base[1])

            count_rows = 0
            for row in reader:
                cost = parse_float(row.get(col_cost))
                if cost is None:
                    continue

                model_val = parse_int(row.get(col_model), fname_model) if col_model else fname_model
                pop_val = parse_int(row.get(col_pop), -1) if col_pop else -1
                pc_val = parse_float(row.get(col_pc), -1.0) if col_pc else -1.0
                pm_val = parse_float(row.get(col_pm), -1.0) if col_pm else -1.0
                elit_val = parse_float(row.get(col_elit), -1.0) if col_elit else -1.0

                # Normalisasi elit jika tertulis dalam persen (misal 20 atau 20.0 -> 0.2)
                if elit_val > 1.0:
                    elit_val = elit_val / 100.0

                key = (
                    model_val if model_val is not None else -1,
                    pop_val,
                    round(pc_val, 6) if pc_val >= 0 else -1.0,
                    round(pm_val, 6) if pm_val >= 0 else -1.0,
                    round(elit_val, 6) if elit_val >= 0 else -1.0,
                )

                entry = grouped[key]
                entry["costs"].append(cost)
                entry["files"].add(os.path.basename(path))

                fit_val = parse_float(row.get(col_fit)) if col_fit else None
                if fit_val is None and cost >= 0:
                    fit_val = 1.0 / (1.0 + cost)
                if fit_val is not None:
                    entry["fitnesses"].append(fit_val)

                eval_val = parse_float(row.get(col_eval)) if col_eval else None
                if eval_val is not None:
                    entry["evals"].append(eval_val)

                time_val = parse_float(row.get(col_time)) if col_time else None
                if time_val is not None:
                    entry["times"].append(time_val)

                count_rows += 1

            loaded_files.append((path, count_rows))

    summary = []
    for key, data in grouped.items():
        costs = data["costs"]
        summary.append({
            "model": key[0],
            "pop": key[1],
            "pc": key[2],
            "pm": key[3],
            "elit": key[4],
            "n_runs": len(costs),
            "mean_cost": mean(costs),
            "std_cost": std(costs),
            "min_cost": min(costs),
            "max_cost": max(costs),
            "mean_fitness": mean(data["fitnesses"]) if data["fitnesses"] else 0.0,
            "mean_evals": mean(data["evals"]) if data["evals"] else None,
            "mean_time": mean(data["times"]) if data["times"] else None,
            "sources": sorted(data["files"]),
        })

    # Urutkan dari rata-rata cost terendah; jika seri, pakai std_cost terkecil lalu min_cost terkecil
    summary.sort(key=lambda x: (x["mean_cost"], x["std_cost"], x["min_cost"]))
    return loaded_files, summary


def format_num(val):
    if abs(val - round(val)) < 1e-9:
        return str(int(round(val)))
    return f"{val:g}"


def main():
    pattern = sys.argv[1] if len(sys.argv) > 1 else "results/m*_tahap2b.csv"
    top_n = int(sys.argv[2]) if len(sys.argv) > 2 else 10

    try:
        loaded_files, summary = load_tahap2b_rows(pattern)
    except Exception as e:
        print(f"[ERROR] {e}")
        sys.exit(1)

    print("=== FILE TAHAP 2B YANG DIBACA ===")
    for path, cnt in loaded_files:
        print(f"  - {path} ({cnt} baris data)")
    print(f"Total kombinasi unik ditemukan: {len(summary)}\n")

    if not summary:
        print("Tidak ada data valid untuk dibandingkan.")
        sys.exit(1)

    print(f"=== TOP {min(top_n, len(summary))} KOMBINASI TAHAP 2B (BERDASARKAN MEAN COST TERKECIL) ===")
    header = (
        f"{'Rank':>4} | {'Model':>5} | {'Pop':>4} | {'Pc':>5} | {'Pm':>5} | {'Elit':>6} | "
        f"{'Runs':>4} | {'MeanCost':>10} | {'StdCost':>9} | {'MinCost':>9} | {'MeanFit':>8}"
    )
    print(header)
    print("-" * len(header))

    for idx, item in enumerate(summary[:top_n], start=1):
        print(
            f"{idx:>4} | {item['model']:>5} | {item['pop']:>4} | "
            f"{format_num(item['pc']):>5} | {format_num(item['pm']):>5} | {format_num(item['elit']):>6} | "
            f"{item['n_runs']:>4} | {item['mean_cost']:>10.4f} | {item['std_cost']:>9.4f} | "
            f"{item['min_cost']:>9.4f} | {item['mean_fitness']:>8.6f}"
        )

    best = summary[0]
    print("\n=== KONFIGURASI TERBAIK (PEMENANG TAHAP 2B) ===")
    print(f"  Model      : {best['model']}")
    print(f"  Populasi   : {best['pop']}")
    print(f"  Pc         : {format_num(best['pc'])}")
    print(f"  Pm         : {format_num(best['pm'])}")
    print(f"  Elitism    : {format_num(best['elit'])}")
    print(f"  Mean Cost  : {best['mean_cost']:.4f} (Std: {best['std_cost']:.4f}, Min: {best['min_cost']:.4f})")
    print(f"  Mean Fit   : {best['mean_fitness']:.6f}")
    if best["mean_evals"] is not None:
        print(f"  Avg Evals  : {best['mean_evals']:.1f}")
    if best["mean_time"] is not None:
        print(f"  Avg Time   : {best['mean_time']:.2f}")

    print("\n=== PERINTAH TAHAP PENGUJIAN (10 SEED, 6 PETA LARGE) ===")
    cmd_pengujian = (
        f"java -cp out experiment.ExperimentRunnerTahap2 pengujian results/pengujian.csv 10 "
        f"{best['model']} {best['pop']} {format_num(best['pc'])} {format_num(best['pm'])} "
        f"{format_num(best['elit'])} maps/daftar_pengujian.txt"
    )
    print(cmd_pengujian)


if __name__ == "__main__":
    main()