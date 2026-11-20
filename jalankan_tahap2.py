"""
Otomatisasi penuh Tahap 2A (pop -> pc -> pm -> elit) sesuai ralat dosen (10 seed):
- Menjalankan ExperimentRunnerTahap2 secara berurutan
- Otomatis memilih nilai parameter terbaik di tiap langkah (berdasarkan skor rasio)
- Menulis laporan keputusan otomatis ke results/keputusan_tahap2[_mX].txt
- Mendukung --model 1|2|3 agar 3 anggota bisa jalan paralel tanpa bentrok file CSV

Pemakaian:
  python jalankan_tahap2.py --model 1    (Anggota 1 jalankan Model 1, 10 seed)
  python jalankan_tahap2.py --model 2    (Anggota 2 jalankan Model 2, 10 seed)
  python jalankan_tahap2.py --model 3    (Anggota 3 jalankan Model 3, 10 seed)
  python jalankan_tahap2.py              (Jalankan semua 3 model sekaligus, 10 seed)
"""

import argparse
import csv
import datetime
import glob
import os
import subprocess
import sys
from collections import defaultdict

FOLDER = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(FOLDER, "out")
RESULTS_DIR = os.path.join(FOLDER, "results")


def waktu():
    return datetime.datetime.now().strftime("%H:%M:%S")


def compile_java():
    print(f"[{waktu()}] Compile source Java...")
    os.makedirs(OUT, exist_ok=True)
    sumber = glob.glob(os.path.join(FOLDER, "src", "**", "*.java"), recursive=True)
    daftar_file = os.path.join(FOLDER, "sources_tmp.txt")
    with open(daftar_file, "w", encoding="utf-8") as f:
        for s in sumber:
            f.write('"' + s.replace("\\", "/") + '"\n')
    try:
        hasil = subprocess.run(
            ["javac", "-encoding", "UTF-8", "-d", OUT, "@sources_tmp.txt"],
            cwd=FOLDER, capture_output=True, text=True)
    finally:
        os.remove(daftar_file)
    if hasil.returncode != 0:
        print(hasil.stderr)
        sys.exit("Compile GAGAL.")
    print(f"[{waktu()}] Compile berhasil.")


def jalankan_java(mode, seed, prefix, model_num, extra_args):
    hasil_csv = os.path.join(RESULTS_DIR, f"{prefix}{mode}.csv")
    log_file = os.path.join(RESULTS_DIR, f"log_{prefix}{mode}.txt")
    os.makedirs(RESULTS_DIR, exist_ok=True)
    if os.path.exists(hasil_csv):
        os.remove(hasil_csv)

    cmd = ["java", "-cp", OUT, "experiment.ExperimentRunnerTahap2", mode, hasil_csv, str(seed)] + [str(x) for x in extra_args]
    if model_num is not None:
        cmd += ["--model", str(model_num)]

    print(f"\n[{waktu()}] === Memulai {prefix}{mode} ===")
    print(f"[{waktu()}] Command: {' '.join(cmd)}")

    with open(log_file, "w", encoding="utf-8") as log:
        proses = subprocess.Popen(cmd, cwd=FOLDER, stdout=subprocess.PIPE,
                                  stderr=subprocess.STDOUT, text=True, encoding="utf-8",
                                  errors="replace")
        terakhir_dicetak = -1
        for baris in proses.stdout:
            log.write(baris)
            log.flush()
            if baris.startswith("[") and "/" in baris:
                try:
                    bagian = baris[1:baris.index("]")].split("/")
                    n, total = int(bagian[0]), int(bagian[1])
                    persen = int(n * 100 / total)
                    if persen // 10 > terakhir_dicetak // 10:
                        terakhir_dicetak = persen
                        print(f"[{waktu()}] Progres {prefix}{mode}: {n}/{total} ({persen}%)")
                except ValueError:
                    continue
        kode = proses.wait()

    if kode != 0:
        sys.exit(f"GAGAL pada {mode} (exit code {kode}). Cek {log_file}")
    print(f"[{waktu()}] {prefix}{mode} selesai -> {hasil_csv}")
    return hasil_csv


def evaluasi_parameter(path_csv, nama_kolom_param):
    data_kombinasi = defaultdict(lambda: defaultdict(list))
    data_param = defaultdict(lambda: defaultdict(list))
    semua_peta = set()

    with open(path_csv, encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            peta = row["peta"]
            cost = float(row["cost"])
            model = f"{row['representasi']}/{row['seleksi']}/{row['crossover']}/{row['mutasi']}"
            kunci_komb = (model, row["pop"], row["pc"], row["pm"], row["elit"])
            val_param = row[nama_kolom_param]

            data_kombinasi[kunci_komb][peta].append(cost)
            data_param[val_param][peta].append(cost)
            semua_peta.add(peta)

    rata_komb = {k: {p: sum(v) / len(v) for p, v in pp.items()} for k, pp in data_kombinasi.items()}
    best_peta_komb = {p: min(rata_komb[k][p] for k in rata_komb if p in rata_komb[k]) for p in semua_peta}

    ringkasan_komb = []
    for k, pp in rata_komb.items():
        skor = sum(pp[p] / best_peta_komb[p] for p in pp) / len(pp)
        rcost = sum(pp.values()) / len(pp)
        ringkasan_komb.append((skor, rcost, k))
    ringkasan_komb.sort(key=lambda x: (x[0], x[1]))

    out_summary = path_csv.replace(".csv", "_ringkasan.csv")
    with open(out_summary, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["peringkat", "model", "pop", "pc", "pm", "elit", "skor_rasio", "rata_cost"])
        for idx, (skor, rcost, k) in enumerate(ringkasan_komb, 1):
            w.writerow([idx, k[0], k[1], k[2], k[3], k[4], f"{skor:.6f}", f"{rcost:.5f}"])

    rata_param = {val: {p: sum(v) / len(v) for p, v in pp.items()} for val, pp in data_param.items()}
    best_peta_param = {p: min(rata_param[val][p] for val in rata_param if p in rata_param[val]) for p in semua_peta}

    hasil_param = []
    for val, pp in rata_param.items():
        skor = sum(pp[p] / best_peta_param[p] for p in pp) / len(pp)
        rcost = sum(pp.values()) / len(pp)
        hasil_param.append((val, skor, rcost))
    hasil_param.sort(key=lambda x: (x[1], x[2]))
    return hasil_param


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--seed", type=int, default=10, help="jumlah seed Tahap 2A (default 10 sesuai ralat)")
    ap.add_argument("--model", type=int, choices=[1, 2, 3], default=None,
                    help="pilih model 1, 2, atau 3 untuk dijalankan paralel oleh tiap anggota")
    args = ap.parse_args()

    prefix = f"m{args.model}_" if args.model is not None else ""
    keputusan_file = os.path.join(RESULTS_DIR, f"{prefix}keputusan_tahap2.txt")

    compile_java()

    laporan = []
    label_model = f"Model #{args.model}" if args.model is not None else "Semua 3 Model Terpilih"
    laporan.append(f"KEPUTUSAN OTOMATIS EKSPERIMEN TAHAP 2A ({label_model})")
    laporan.append(f"Waktu selesai: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    laporan.append(f"Jumlah seed per konfigurasi: {args.seed} (sesuai ralat dosen)")
    laporan.append("Kriteria pemilihan: Rata-rata rasio cost terhadap kandidat terbaik per peta (terkecil = terbaik)\n")

    # 1. POPULASI
    csv_pop = jalankan_java("tahap2a_pop", args.seed, prefix, args.model, [])
    rank_pop = evaluasi_parameter(csv_pop, "pop")
    best_pop = rank_pop[0][0]
    second_pop = rank_pop[1][0]
    laporan.append(f"1. Populasi (pop) -> TERPILIH: {best_pop} (runner-up: {second_pop})")
    for val, skor, rcost in rank_pop:
        laporan.append(f"   - pop={val:<5} | skor_rasio={skor:.6f} | rata_cost={rcost:.5f}")
    laporan.append("")

    # 2. PROB. CROSSOVER
    csv_pc = jalankan_java("tahap2a_pc", args.seed, prefix, args.model, [best_pop])
    rank_pc = evaluasi_parameter(csv_pc, "pc")
    best_pc = rank_pc[0][0]
    second_pc = rank_pc[1][0]
    laporan.append(f"2. Probabilitas Crossover (pc) [dengan pop={best_pop}] -> TERPILIH: {best_pc} (runner-up: {second_pc})")
    for val, skor, rcost in rank_pc:
        laporan.append(f"   - pc={val:<5}  | skor_rasio={skor:.6f} | rata_cost={rcost:.5f}")
    laporan.append("")

    # 3. PROB. MUTASI
    csv_pm = jalankan_java("tahap2a_pm", args.seed, prefix, args.model, [best_pop, best_pc])
    rank_pm = evaluasi_parameter(csv_pm, "pm")
    best_pm = rank_pm[0][0]
    laporan.append(f"3. Probabilitas Mutasi (pm) [dengan pop={best_pop}, pc={best_pc}] -> TERPILIH: {best_pm}")
    for val, skor, rcost in rank_pm:
        laporan.append(f"   - pm={val:<6} | skor_rasio={skor:.6f} | rata_cost={rcost:.5f}")
    laporan.append("")

    # 4. ELITISM
    csv_elit = jalankan_java("tahap2a_elit", args.seed, prefix, args.model, [best_pop, best_pc, best_pm])
    rank_elit = evaluasi_parameter(csv_elit, "elit")
    best_elit = rank_elit[0][0]
    laporan.append(f"4. Elitism (elit) [dengan pop={best_pop}, pc={best_pc}, pm={best_pm}] -> TERPILIH: {best_elit}")
    for val, skor, rcost in rank_elit:
        laporan.append(f"   - elit={val:<5} | skor_rasio={skor:.6f} | rata_cost={rcost:.5f}")
    laporan.append("")

    flag_model = f" --model {args.model}" if args.model is not None else ""
    laporan.append("==========================================")
    laporan.append("HYPERPARAMETER TERBAIK TAHAP 2A:")
    laporan.append(f"  pop  = {best_pop} (top 2: {best_pop}, {second_pop})")
    laporan.append(f"  pc   = {best_pc} (top 2: {best_pc}, {second_pc})")
    laporan.append(f"  pm   = {best_pm}")
    laporan.append(f"  elit = {best_elit}")
    laporan.append("\nPerintah siap pakai untuk lanjut ke Tahap 2B (30 seed):")
    laporan.append(f"  java -cp out experiment.ExperimentRunnerTahap2 tahap2b results/{prefix}tahap2b.csv 30 {best_pop} {second_pop} {best_pc} {second_pc} {best_pm} {best_elit}{flag_model}")

    teks_laporan = "\n".join(laporan)
    with open(keputusan_file, "w", encoding="utf-8") as f:
        f.write(teks_laporan + "\n")

    print("\n" + teks_laporan)
    print(f"\nKeputusan otomatis telah disimpan ke: {keputusan_file}")


if __name__ == "__main__":
    main()