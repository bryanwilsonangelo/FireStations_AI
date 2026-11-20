package experiment;

import input.MapReader;
import model.MapData;
import algorithm.CostCalculator;
import algorithm.DistanceMatrix;

// Tes validasi perhitungan jarak (BFS + DistanceMatrix) terhadap spesifikasi.
// Jalankan dari folder proyek: java -cp out experiment.ValidasiJarak
public class ValidasiJarak {

    private static int lulus = 0;
    private static int gagal = 0;

    private static void cek(String nama, int hasil, int harapan) {
        if (hasil == harapan) {
            lulus++;
        } else {
            gagal++;
            System.out.println("GAGAL: " + nama + " hasil=" + hasil + " harapan=" + harapan);
        }
    }

    public static void main(String[] args) throws Exception {

        // Tes 1: Gambar 2 & 3 spesifikasi.
        // Nomor sel = urutan baris-per-baris, dimulai dari 1 (kiri atas).
        // Indeks sel kosong di DistanceMatrix = nomor sel - 1.
        MapData map = MapReader.readMap("maps/tes/gambar2.txt");
        DistanceMatrix m = new DistanceMatrix(map, map.getKosong());

        // Indeks rumah: 0 = H1 (2,2), 1 = H2 (3,5), sesuai urutan baca peta.
        // Format: {nomor sel, jarak H1, jarak H2}, nilai dari Gambar 3.
        int[][] tabel = {
            {1, 2, 6}, {2, 1, 5}, {3, 2, 4}, {4, 3, 5}, {5, 4, 6},
            {18, 4, 2}, {19, 3, 1}, {20, 5, 1}, {21, 6, 2}
        };

        for (int[] baris : tabel) {
            int sel = baris[0] - 1;
            cek("H1 ke sel " + baris[0], m.getDistance(0, sel), baris[1]);
            cek("H2 ke sel " + baris[0], m.getDistance(1, sel), baris[2]);
        }

        // Teks Gambar 3 menyebut jarak H2 ke sel 5 = 4, tetapi tabel = 6.
        // Nilai yang dipakai di atas adalah tabel (6). Yang di bawah hanya dicetak.
        System.out.println("Catatan: jarak H2 ke sel 5 menurut tabel = " + m.getDistance(1, 4)
            + " (teks spesifikasi menulis 4).");

        // Tes 2: sel nomor 7 di (3,2) bersebelahan dengan H1 di (2,2) = jarak 1 (bukan 2).
        cek("bersebelahan = 1", m.getDistance(0, 6), 1);

        // Tes 3: penalti untuk rumah yang tidak bisa dicapai.
        // Rumah di (1,1) dikelilingi pohon di (1,2) dan (2,1).
        MapData pen = MapReader.readMap("maps/tes/penalti.txt");
        DistanceMatrix mp = new DistanceMatrix(pen, pen.getKosong());
        int harapanPenalti = pen.getBaris() * pen.getKolom();
        for (int j = 0; j < mp.getEmptyCount(); j++) {
            cek("penalti sel " + (j + 1), mp.getDistance(0, j), harapanPenalti);
        }

        // Tes 4: cost dengan satu FS di sel nomor 4 (indeks 3).
        // Spesifikasi: jarak H1 = 3 dan H2 = 5, jadi rata-rata = (3+5)/2 = 4.0.
        double cost = CostCalculator.calculateCost(m, new int[]{3});
        if (Math.abs(cost - 4.0) < 1e-9) {
            lulus++;
        } else {
            gagal++;
            System.out.println("GAGAL: cost sel 4 hasil=" + cost + " harapan=4.0");
        }

        System.out.println("Lulus: " + lulus + ", Gagal: " + gagal);
        if (gagal > 0) {
            System.exit(1);
        }
    }
}
