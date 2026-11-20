# Inventaris Peta

Setiap folder berisi 12 peta TestCases: 4 acak, 4 klaster, 4 labirin.
Label folder menentukan evaluation budget (lihat `ExperimentConfig.budgetUntuk`).

| Folder  | Ukuran grid | p (header) | Budget  | Tabel 1 (p) |
|---------|-------------|------------|---------|-------------|
| tiny/   | 20×20       | 3          | 10.000  | Tiny: 3     |
| small/  | 40×40       | 10         | 50.000  | Small: 10   |
| normal/ | 60×60       | 24         | 200.000 | Normal: 24  |
| large/  | 80×80       | 40         | 500.000 | Large: 40   |

Catatan:
- Ukuran grid pada tiap label berbeda dari Tabel 1 (Tabel 1: Tiny 10×10, Small 20×20, Normal 40×40), tetapi jumlah FS (p) sama.
- Jumlah FS (p) diambil dari header file, bukan dari label.
- `tes/` berisi peta uji untuk `ValidasiJarak`, bukan peta eksperimen.
- `daftar_tahap1.txt` semua barisnya masih dikomentari. Aktifkan baris yang dipilih.
