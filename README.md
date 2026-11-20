# Fire Stations - Algoritma Genetik

## Struktur
- `src/model`      : `Cell`, `MapData`, `Chromosome`, `Individual`
- `src/input`      : `MapReader` (baca file peta)
- `src/algorithm`  : `BFS`, `DistanceMatrix`, `CostCalculator`, `FitnessCalculator`, `Selection`, `Crossover`, `Mutation`, `GeneticAlgorithm`, `GAConfig`
- `src/experiment` : `ExperimentConfig`, `ExperimentRunner`, `ExperimentRunnerTahap2`, `Seeds`, `CSVWriter`, `ValidasiJarak`, `PilihModel`, `KeputusanTahap1`[cite: 3]
- `src/Main.java`  : entry point
- `maps/`          : file peta (`tiny`, `small`, `normal`, `large`) dan daftar peta (`daftar_tahap1.txt`, `daftar_tahap2.txt`, `daftar_pengujian.txt`)[cite: 3]
- `results/`       : hasil eksperimen (`.csv` dan file keputusan/ringkasan)
- `pilih_terbaik_tahap2b.py` : rekap hasil Tahap 2B dan cetak perintah Tahap Pengujian[cite: 4]

## Compile
Dari folder root proyek (`FireStations_AI`):

    javac -d out -encoding UTF-8 -cp src src\model\*.java src\input\*.java src\algorithm\*.java src\experiment\*.java src\Main.java

## Menjalankan Single Run
    java -cp out Main maps\tiny\n20_acak_01.txt pop=100 budget=10000 pc=0.8 pm=0.05 elit=0.2 seed=0

Key opsional: `pop`, `budget`, `pc`, `pm`, `elit`, `seed` (0-29), `repr`, `sel`, `tour`, `cx`, `mut`, `stag`, `waktu`.

## Output
Baris pertama: `p f` (`f` dengan 5 desimal). Lalu `p` baris `x y` (`x` = kolom, `y` = baris, `(1,1)` kiri atas).
Konvensi ini sesuai contoh spesifikasi: koordinat `x = 10` muncul pada grid 10 kolom × 7 baris, jadi `x` harus kolom.

## Eksperimen

### Tahap 1 (16 kombinasi operator × peta × 30 seed)
    java -cp out experiment.ExperimentRunner tahap1 results\tahap1.csv

- Peta diambil dari `maps/daftar_tahap1.txt`. Peta yang belum ada dilewati dengan peringatan.
- Argumen ketiga (jumlah seed) opsional (default 30).
- Budget per ukuran: `tiny` 10.000, `small` 50.000, `normal` 200.000, `large` 500.000.
- Setiap baris CSV memuat konfigurasi lengkap dan seed yang dipakai.

Catatan: `generasi` di CSV bisa lebih besar dari `budget/pop`. Anak yang tidak mengalami crossover maupun mutasi tidak dihitung ulang fitness-nya, sehingga budget habis dalam lebih banyak generasi.

### Merangkum & Mencatat Keputusan Tahap 1
Setelah `results\tahap1.csv` selesai:

    java -cp out experiment.PilihModel
    java -cp out experiment.KeputusanTahap1 rasio 3

Menghasilkan `results\rata_cost_tahap1.csv` dan `results\keputusan_tahap1.csv` (memuat peringkat seluruh model dan kolom `dipilih` `ya`/`tidak`).

### Tahap 2A & 2B (Tuning Parameter per Model)
Jalankan `ExperimentRunnerTahap2` per model (`--model 1|2|3`) memakai daftar peta di `maps/daftar_tahap2.txt`[cite: 3]:

    java -cp out experiment.ExperimentRunnerTahap2 tahap2a_pop results\m1_tahap2a_pop.csv --model 1
    java -cp out experiment.ExperimentRunnerTahap2 tahap2b results\m1_tahap2b.csv 30 <pop1> <pop2> <pc1> <pc2> <bestPm> <bestElit> --model 1

*(Ulangi pola yang sama untuk `--model 2` -> `results\m2_tahap2b.csv` dan `--model 3` -> `results\m3_tahap2b.csv`)*.

### Memilih Konfigurasi Terbaik Tahap 2B
Setelah `m1_tahap2b.csv`, `m2_tahap2b.csv`, dan `m3_tahap2b.csv` ter-merge ke `main`:

    python pilih_terbaik_tahap2b.py results/m*_tahap2b.csv 12

Program mengurutkan kombinasi berdasarkan rata-rata `cost` terkecil (`MeanCost`), konsistensi (`StdCost`), nilai terbaik (`MinCost`), dan rata-rata `fitness` (`MeanFit`)[cite: 4].

### Tahap Pengujian (6 peta large × 10 seed)
Pastikan `maps/daftar_pengujian.txt` berisi 6 baris `large <path_peta>` (2 acak, 2 klaster, 2 labirin), lalu jalankan[cite: 3]:

    java -cp out experiment.ExperimentRunnerTahap2 pengujian results\pengujian.csv 10 BINARY/ROULETTE/ONE_POINT/SINGLE 250 0.5 0.15 0.3 maps/daftar_pengujian.txt

## Validasi
Tes otomatis untuk jarak BFS, penalti, dan cost (sesuai Gambar 2 dan 3 spesifikasi):

    java -cp out experiment.ValidasiJarak

Output yang benar: `Lulus: 26, Gagal: 0`.
Catatan: teks Gambar 3 menyebut jarak H2 ke sel 5 = 4, tetapi tabelnya 6. Tes memakai nilai tabel.