
import input.MapReader;
import model.MapData;
import model.Cell;
import model.Chromosome;
import model.Individual;
import algorithm.DistanceMatrix;

import java.util.Random;

import algorithm.CostCalculator;

public class Main {

    public static void main(String[] args) {

        try {

            // 1. Membaca file peta.
            MapData map = MapReader.readMap(
                    "maps/n20_acak_01.txt");

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

            // 3. Membuat random generator.
            Random random = new Random(12345);

            // 4. Membuat kromosom binary.
            Chromosome binary =
                Chromosome.createRandom(
                    map.getJumlahSelKosong(),
                    map.getP(),
                    Chromosome.Representation.BINARY,
                    random
                );

            // 5. Membuat kromosom integer.
            Chromosome integer =
                Chromosome.createRandom(
                    map.getJumlahSelKosong(),
                    map.getP(),
                    Chromosome.Representation.INTEGER,
                    random
                );

            // 6. Membuat individu.
            Individual individualBinary =
                new Individual(binary, matrix);

            Individual individualInteger =
                new Individual(integer, matrix);

            // 7. Menampilkan hasil.
            System.out.println("=== BINARY ENCODING ===");

            System.out.println(binary);

            System.out.println(
                "Jumlah FS: " +
                binary.getStationIndices().length
            );

            System.out.println(individualBinary);

            System.out.println("\n=== INTEGER ENCODING ===");

            System.out.println(integer);

            System.out.println(
                "Jumlah FS: " +
                integer.getStationIndices().length
            );

            System.out.println(individualInteger);

            // 8. Menampilkan posisi FS.
            System.out.println("\n=== POSISI FS BINARY ===");

            for (int index : binary.getStationIndices()) {

                System.out.println(
                    "Indeks sel kosong: " + index
                );

                System.out.println(
                    "Koordinat: " +
                    matrix.getEmptyCells().get(index)
                );
            }

            System.out.println("\n=== POSISI FS INTEGER ===");

            for (int index : integer.getStationIndices()) {

                System.out.println(
                    "Indeks sel kosong: " + index
                );

                System.out.println(
                    "Koordinat: " +
                    matrix.getEmptyCells().get(index)
                );
            }

            // 9. Uji kesetaraan representasi.
            // Mengambil posisi FS dari binary.
            int[] samePositions = binary.getStationIndices();

            // Membuat integer dengan posisi yang sama.
            Chromosome sameInteger =
                Chromosome.fromStationIndices(
                    samePositions,
                    map.getJumlahSelKosong(),
                    Chromosome.Representation.INTEGER
                );

            // Membuat individu integer.
            Individual testIndividual =
                new Individual(sameInteger, matrix);

            // Membandingkan cost.
            System.out.println("\n=== UJI KESETARAAN ===");

            System.out.println(
                "Cost Binary: " + individualBinary.getCost()
            );

            System.out.println(
                "Cost Integer: " + testIndividual.getCost()
            );

            System.out.println(
                "Apakah sama: " +
                (individualBinary.getCost() == testIndividual.getCost())
            );
        }
        catch (Exception e) {

            System.out.println(
                "Terjadi kesalahan: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}

