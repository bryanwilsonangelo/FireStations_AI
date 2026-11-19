
import input.MapReader;
import model.MapData;
import model.Cell;
import model.Chromosome;
import model.Individual;
import model.Population;
import algorithm.DistanceMatrix;
import algorithm.GeneticAlgorithm;
import algorithm.Mutation;
import algorithm.Selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import algorithm.CostCalculator;
import algorithm.Crossover;

public class Main {

        public static void main(String[] args) {

                try {

                        // 1. Membaca file peta.
                        MapData map = MapReader.readMap(
                                        "maps/testing_kecil.txt");

                        System.out.println("=== INFORMASI PETA ===");

                        System.out.println(
                                        "Ukuran: " + map.getBaris()
                                                        + " x " + map.getKolom());

                        System.out.println(
                                        "Jumlah rumah: " + map.getJumlahRumah());

                        System.out.println(
                                        "Jumlah pohon: " + map.getJumlahPohon());

                        System.out.println(
                                        "Jumlah sel kosong: "
                                                        + map.getJumlahSelKosong());

                        System.out.println(
                                        "Jumlah FS: " + map.getP());

                        // 2. Membuat matriks jarak.
                        DistanceMatrix matrix = new DistanceMatrix(map, map.getKosong());

                        // 3. Membuat random generator.
                        Random random = new Random(12345);

                        // 4. Membuat kromosom binary.
                        Chromosome binary = Chromosome.createRandom(
                                        map.getJumlahSelKosong(),
                                        map.getP(),
                                        Chromosome.Representation.BINARY,
                                        random);

                        // 5. Membuat kromosom integer.
                        Chromosome integer = Chromosome.createRandom(
                                        map.getJumlahSelKosong(),
                                        map.getP(),
                                        // jika mau cek binery Chromosome.Representation.BINARY
                                        Chromosome.Representation.INTEGER,
                                        random);

                        // 6. Membuat individu.
                        Individual individualBinary = new Individual(binary, matrix);

                        Individual individualInteger = new Individual(integer, matrix);

                        // 7. Menampilkan hasil.
                        System.out.println("=== BINARY ENCODING ===");

                        System.out.println(binary);

                        System.out.println(
                                        "Jumlah FS: " +
                                                        binary.getStationIndices().length);

                        System.out.println(individualBinary);

                        System.out.println("\n=== INTEGER ENCODING ===");

                        System.out.println(integer);

                        System.out.println(
                                        "Jumlah FS: " +
                                                        integer.getStationIndices().length);

                        System.out.println(individualInteger);

                        // 8. Menampilkan posisi FS.
                        System.out.println("\n=== POSISI FS BINARY ===");

                        for (int index : binary.getStationIndices()) {

                                System.out.println(
                                                "Indeks sel kosong: " + index);

                                System.out.println(
                                                "Koordinat: " +
                                                                matrix.getEmptyCells().get(index));
                        }

                        System.out.println("\n=== POSISI FS INTEGER ===");

                        for (int index : integer.getStationIndices()) {

                                System.out.println(
                                                "Indeks sel kosong: " + index);

                                System.out.println(
                                                "Koordinat: " +
                                                                matrix.getEmptyCells().get(index));
                        }

                        // 9. Uji kesetaraan representasi.
                        // Mengambil posisi FS dari binary.
                        int[] samePositions = binary.getStationIndices();

                        // Membuat integer dengan posisi yang sama.
                        Chromosome sameInteger = Chromosome.fromStationIndices(
                                        samePositions,
                                        map.getJumlahSelKosong(),
                                        Chromosome.Representation.INTEGER);

                        // Membuat individu integer.
                        Individual testIndividual = new Individual(sameInteger, matrix);

                        // Membandingkan cost.
                        System.out.println("\n=== UJI KESETARAAN ===");

                        System.out.println(
                                        "Cost Binary: " + individualBinary.getCost());

                        System.out.println(
                                        "Cost Integer: " + testIndividual.getCost());

                        System.out.println(
                                        "Apakah sama: " +
                                                        (individualBinary.getCost() == testIndividual.getCost()));
                        // Membuat populasi pengujian.
                        List<Individual> population = new ArrayList<>();

                        for (int i = 0; i < 10; i++) {

                                Chromosome chromosome = Chromosome.createRandom(
                                                map.getJumlahSelKosong(),
                                                map.getP(),
                                                Chromosome.Representation.INTEGER,
                                                random);

                                population.add(
                                                new Individual(chromosome, matrix));
                        }

                        System.out.println("=== SELECTION TEST ===");

                        Individual roulette = Selection.rouletteWheel(
                                        population, random);

                        Individual tournament = Selection.tournament(
                                        population, 3, random);

                        System.out.println(
                                        "Roulette: " + roulette);

                        System.out.println(
                                        "Tournament: " + tournament);

                        // Membuat dua parent.
                        Chromosome parent1 = population.get(0).getChromosome();

                        Chromosome parent2 = population.get(1).getChromosome();

                        System.out.println("\n=== CROSSOVER TEST ===");

                        Chromosome child1 = Crossover.onePoint(
                                        parent1, parent2, random);

                        Chromosome child2 = Crossover.uniform(
                                        parent1, parent2, random);

                        System.out.println(
                                        "Parent 1: " + parent1);

                        System.out.println(
                                        "Parent 2: " + parent2);

                        System.out.println(
                                        "One Point: " + child1);

                        System.out.println(
                                        "Uniform: " + child2);

                        System.out.println("\n=== MUTATION TEST ===");

                        Chromosome mutation1 = Mutation.singleRelocation(
                                        parent1, random);

                        Chromosome mutation2 = Mutation.multipleRelocation(
                                        parent1, random);

                        System.out.println(
                                        "Original: " + parent1);

                        System.out.println(
                                        "Single: " + mutation1);

                        System.out.println(
                                        "Multiple: " + mutation2);

                        System.out.println("\n=== VALIDATION ===");

                        checkChromosome(child1);
                        checkChromosome(child2);
                        checkChromosome(mutation1);
                        checkChromosome(mutation2);

                        System.out.println(
                                        "Semua kromosom valid.");

                        // 10. Uji Population dan GeneticAlgorithm.
                        System.out.println("\n=== POPULATION TEST ===");

                        Population testPop = Population.createRandom(
                                        10,
                                        map.getJumlahSelKosong(),
                                        map.getP(),
                                        Chromosome.Representation.INTEGER,
                                        matrix,
                                        random);

                        System.out.println(testPop);
                        System.out.println(
                                        "Best Individual: " + testPop.getBestIndividual());

                        System.out.println("\n=== GENETIC ALGORITHM TEST ===");

                        GeneticAlgorithm ga = new GeneticAlgorithm(
                                        map,
                                        matrix,
                                        Chromosome.Representation.INTEGER,
                                        GeneticAlgorithm.SelectionType.TOURNAMENT,
                                        GeneticAlgorithm.CrossoverType.UNIFORM,
                                        GeneticAlgorithm.MutationType.SINGLE_RELOCATION,
                                        10000,
                                        random);

                        Individual bestSolution = ga.run();

                        System.out.println("Best Solution: " + bestSolution);

                        System.out.println("Generasi dijalankan: " + ga.getGenerationsRun());

                        System.out.println("Evaluasi digunakan: " + ga.getEvaluationsUsed());

                        System.out.println("Alasan berhenti: " + ga.getStopReason());

                        System.out.println("Waktu eksekusi: " + ga.getExecutionTimeMillis() + " ms");

                        System.out.println("\n=== FORMAT OUTPUT SOAL ===");
                        System.out.println(ga.formatOutput());
                } catch (Exception e) {

                        System.out.println(
                                        "Terjadi kesalahan: " + e.getMessage());

                        e.printStackTrace();
                }
        }

        // Memastikan kromosom valid.
        private static void checkChromosome(
                        Chromosome chromosome) {

                int[] stations = chromosome.getStationIndices();

                if (stations.length != chromosome.getStationCount()) {

                        throw new RuntimeException(
                                        "Jumlah FS tidak sesuai");
                }

                boolean[] used = new boolean[chromosome.getEmptyCount()];

                for (int index : stations) {

                        if (index < 0 ||
                                        index >= used.length) {

                                throw new RuntimeException(
                                                "Indeks FS di luar batas");
                        }

                        if (used[index]) {

                                throw new RuntimeException(
                                                "FS duplikat");
                        }

                        used[index] = true;
                }
        }

}
