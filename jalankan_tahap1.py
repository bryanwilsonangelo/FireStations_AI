"""
Menjalankan tahap 1 dari awal sampai selesai, lalu merangkum hasilnya.

Yang dilakukan otomatis:
  1. Compile seluruh source Java ke folder out\
  2. Menjalankan eksperimen tahap 1 sesuai maps\daftar_tahap1.txt
  3. Memantau progres dan menampilkannya
  4. Setelah selesai, merangkum rata-rata cost ke results\rata_cost_tahap1.csv

Yang TIDAK dilakukan otomatis:
  Memilih model terbaik. Itu tetap keputusan Anda, lewat keputusan.bat.

Pemakaian (dari folder proyek):
  python jalankan_tahap1.py                 jalankan tahap 1 penuh (30 seed)
  python jalankan_tahap1.py --seed 2        tes cepat dengan 2 seed
  python jalankan_tahap1.py --hanya-rangkum hanya rangkum results\tahap1.csv yang sudah ada
"""

import argparse
import datetime
import glob
import os
import subprocess
import sys

FOLDER = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(FOLDER, "out")
DAFTAR = os.path.join(FOLDER, "maps", "daftar_tahap1.txt")
HASIL = os.path.join(FOLDER, "results", "tahap1.csv")
LOG = os.path.join(FOLDER, "results", "log_tahap1.txt")
JUMLAH_KOMBINASI = 16  # 2 representasi x 2 seleksi x 2 crossover x 2 mutasi


def waktu():
    return datetime.datetime.now().strftime("%H:%M:%S")


def hitung_peta_aktif():
    """Jumlah baris peta yang tidak dikomentari di daftar_tahap1.txt."""
    jumlah = 0
    with open(DAFTAR, encoding="utf-8") as f:
        for baris in f:
            baris = baris.strip()
            if baris and not baris.startswith("#"):
                jumlah += 1
    return jumlah


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
        sys.exit("Compile GAGAL. Perbaiki error di atas dulu.")
    print(f"[{waktu()}] Compile berhasil.")


def jalankan_tahap1(seed):
    peta = hitung_peta_aktif()
    total = peta * JUMLAH_KOMBINASI * seed
    print(f"[{waktu()}] Tahap 1: {peta} peta x {JUMLAH_KOMBINASI} kombinasi x {seed} seed = {total} percobaan")
    if total == 0:
        sys.exit("Tidak ada peta aktif di maps\\daftar_tahap1.txt.")

    perintah = ["java", "-cp", OUT, "experiment.ExperimentRunner",
                "tahap1", HASIL, str(seed)]
    os.makedirs(os.path.dirname(LOG), exist_ok=True)
    with open(LOG, "w", encoding="utf-8") as log:
        proses = subprocess.Popen(perintah, cwd=FOLDER, stdout=subprocess.PIPE,
                                  stderr=subprocess.STDOUT, text=True, encoding="utf-8",
                                  errors="replace")
        terakhir_dicetak = -1
        for baris in proses.stdout:
            log.write(baris)
            log.flush()
            # Cetak progres setiap 5%.
            if baris.startswith("[") and "/" in baris:
                try:
                    n = int(baris[1:baris.index("/")])
                except ValueError:
                    continue
                persen = int(n * 100 / total)
                if persen // 5 > terakhir_dicetak // 5:
                    terakhir_dicetak = persen
                    print(f"[{waktu()}] Progres: {n}/{total} ({persen}%)")
        kode = proses.wait()

    if kode != 0:
        sys.exit(f"Tahap 1 berhenti dengan kode {kode}. Lihat {LOG}")
    print(f"[{waktu()}] Tahap 1 selesai.")


def cek_hasil(seed):
    """Memastikan jumlah baris CSV sesuai dengan jumlah percobaan yang diharapkan."""
    if not os.path.exists(HASIL):
        sys.exit(f"File hasil tidak ditemukan: {HASIL}")
    with open(HASIL, encoding="utf-8") as f:
        baris = sum(1 for _ in f) - 1  # dikurangi header
    diharapkan = hitung_peta_aktif() * JUMLAH_KOMBINASI * seed
    if baris != diharapkan:
        sys.exit(f"Jumlah baris {baris}, seharusnya {diharapkan}. Hasil tidak lengkap.")
    print(f"[{waktu()}] Cek hasil: {baris} percobaan, sesuai harapan.")


def rangkum():
    print(f"[{waktu()}] Merangkum hasil...")
    hasil = subprocess.run(
        ["java", "-cp", OUT, "experiment.PilihModel", HASIL],
        cwd=FOLDER, capture_output=True, text=True, encoding="utf-8", errors="replace")
    print(hasil.stdout)
    if hasil.returncode != 0:
        print(hasil.stderr)
        sys.exit("Rangkuman gagal.")
    print(f"[{waktu()}] Selesai. Rangkuman ada di results\\rata_cost_tahap1.csv")
    print("Langkah berikutnya: pilih model terbaik sendiri, lalu jalankan keputusan.bat.")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--seed", type=int, default=30, help="jumlah seed (default 30)")
    ap.add_argument("--hanya-rangkum", action="store_true",
                    help="lewati tahap run, langsung rangkum results\\tahap1.csv")
    args = ap.parse_args()

    if not 1 <= args.seed <= 30:
        sys.exit("--seed harus antara 1 dan 30")

    if not args.hanya_rangkum:
        compile_java()
        jalankan_tahap1(args.seed)
        cek_hasil(args.seed)

    rangkum()


if __name__ == "__main__":
    main()
