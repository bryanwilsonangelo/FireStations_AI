
import input.MapReader;
import model.MapData;
import model.Cell;

import algorithm.DistanceMatrix;
import algorithm.CostCalculator;

public class Main {

    public static void main(String[] args) {

        try {

            // 1. Membaca file peta.
            MapData map = MapReader.readMap(
                "maps/n20_klaster_04.txt"
            );

            System.out.println("=== INFORMASI PETA ===");

            System.out.println(
                "Ukuran: " + map.getBaris()
                + " x " + map.getKolom()
            );

            System.out.println(
                "Jumlah rumah: " + map.getJumlahRumah()
            );

            System.out.println(
                "Jumlah pohon: " + map.getJumlahPohon()
            );

            System.out.println(
                "Jumlah sel kosong: "
                + map.getJumlahSelKosong()
            );

            System.out.println(
                "Jumlah FS: " + map.getP()
            );

            // 2. Membuat matriks jarak.
            DistanceMatrix matrix =
                new DistanceMatrix(map, map.getKosong());

            System.out.println("\n=== DISTANCE MATRIX ===");

            System.out.println(
                "Jumlah baris: " + matrix.getHouseCount()
            );

            System.out.println(
                "Jumlah kolom: " + matrix.getEmptyCount()
            );

            // 3. Contoh posisi FS sementara.
            // Nanti posisi ini berasal dari kromosom GA.
            int[] stationIndices = new int[map.getP()];

            for (int i = 0; i < map.getP(); i++) {
                stationIndices[i] = i;
            }

            // 4. Menghitung cost.
            double cost = CostCalculator.calculateCost(
                matrix,
                stationIndices
            );

            System.out.println("\n=== HASIL PERHITUNGAN ===");

            System.out.printf(
                "Cost: %.5f%n", cost
            );

            // 5. Menampilkan koordinat FS sementara.
            System.out.println("\nPosisi FS sementara:");

            for (int index : stationIndices) {

                Cell cell = map.getKosong().get(index);

                System.out.println(cell);
            }

        }
        catch (Exception e) {

            System.out.println(
                "Terjadi kesalahan: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
