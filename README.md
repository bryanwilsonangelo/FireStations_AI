# Fire Stations - Algoritma Genetik

## Struktur
- `src/model`      : Cell, MapData, Chromosome, Individual
- `src/input`      : MapReader (baca file peta)
- `src/algorithm`  : BFS, DistanceMatrix, CostCalculator, FitnessCalculator, Selection, Crossover, Mutation, GeneticAlgorithm, GAConfig
- `src/experiment` : Seeds (30 seed tetap)
- `src/Main.java`  : entry point
- `maps/`          : file peta
- `results/`       : hasil eksperimen (CSV)

## Compile
Windows: jalankan `a.bat` (hasil ke folder `out`).

## Menjalankan
    run.bat maps\tiny\n20_acak_01.txt pop=100 budget=10000 pc=0.8 pm=0.05 elit=0.2 seed=0

Key opsional: pop, budget, pc, pm, elit, seed (0-29), repr, sel, tour, cx, mut, stag, waktu.

## Output
Baris pertama: `p f` (f dengan 5 desimal). Lalu p baris `x y` (x = kolom, y = baris, (1,1) kiri atas).
Konvensi ini sesuai contoh spesifikasi: koordinat x = 10 muncul pada grid 10 kolom × 7 baris, jadi x harus kolom.

## Eksperimen
Tahap 1 (16 kombinasi operator x peta x 30 seed):

    tahap1.bat
    java -cp out experiment.ExperimentRunner tahap1 results\tahap1.csv 2

- Peta diambil dari `maps/daftar_tahap1.txt`. Tambahkan baris di file itu untuk peta baru.
- Peta yang belum ada dilewati dengan peringatan.
- Argumen ketiga (jumlah seed) opsional. Default 30. Gunakan 1 atau 2 untuk tes cepat.
- Budget per ukuran: tiny 10.000, small 50.000, normal 200.000, large 500.000.
- Stagnasi dan batas waktu nonaktif (lihat `ExperimentConfig.java`).
- Setiap baris CSV memuat konfigurasi lengkap dan seed yang dipakai.

Catatan: `generasi` di CSV bisa lebih besar dari budget/pop. Anak yang tidak mengalami crossover maupun mutasi tidak dihitung ulang fitness-nya, sehingga budget habis dalam lebih banyak generasi.

## Validasi
Tes otomatis untuk jarak BFS, penalti, dan cost (sesuai Gambar 2 dan 3 spesifikasi):

    java -cp out experiment.ValidasiJarak

Output yang benar: `Lulus: 26, Gagal: 0`. Peta tes ada di `maps/tes/`.
Catatan: teks Gambar 3 menyebut jarak H2 ke sel 5 = 4, tetapi tabelnya 6. Tes memakai nilai tabel.

## Merangkum hasil tahap 1
Setelah `tahap1.bat` selesai dan menghasilkan `results\tahap1.csv`:

    pilihmodel.bat

Program hanya merangkum rata-rata cost tiap model pada tiap peta, dan tidak memilih model.
Hasil: `results\rata_cost_tahap1.csv` (model, peta, jumlah_seed, rata_cost) dan tabel ringkas di layar.
Pemilihan tiga model terbaik untuk tahap 2 dilakukan kelompok berdasarkan data ini.

## Mencatat keputusan model (tahap 1)
Setelah memilih kriteria dan tiga model, jalankan:

    keputusan.bat rasio 3

Argumen: kriteria (`rasio` atau `rata`) dan jumlah model yang dipilih.
Hasil: `results\keputusan_tahap1.csv` berisi semua model dengan peringkatnya dan kolom `dipilih` (ya/tidak).
Program ini hanya menerapkan kriteria yang Anda tulis. Kriteria dan jumlah pilihan tetap keputusan Anda.
