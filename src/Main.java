import java.util.Locale;
import java.util.Random;

import input.MapReader;
import model.Cell;
import model.Chromosome;
import model.Individual;
import model.MapData;
import algorithm.DistanceMatrix;
import algorithm.GAConfig;
import algorithm.GeneticAlgorithm;
import experiment.Seeds;

// Program utama: membaca peta, menjalankan GA, dan menampilkan hasil.
//
// Pemakaian:
//   java Main <file_peta> [key=value ...]
// Contoh:
//   java Main maps/n20_acak_01.txt pop=100 budget=10000 seed=0
//
// Key yang tersedia (semua opsional):
//   pop, budget, pc, pm, elit, seed (indeks 0-29), repr (BINARY/INTEGER),
//   sel (ROULETTE/TOURNAMENT), tour (ukuran tournament),
//   cx (ONE_POINT/UNIFORM), mut (SINGLE/MULTIPLE),
//   stag (batas stagnasi, 0=nonaktif), waktu (ms, 0=nonaktif)
public class Main {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Pemakaian: java Main <file_peta> [key=value ...]");
            System.exit(1);
        }

        try {
            GAConfig cfg = new GAConfig();
            int seedIndex = 0;

            // Membaca argumen opsional key=value.
            for (int i = 1; i < args.length; i++) {

                String[] kv = args[i].split("=", 2);
                if (kv.length != 2) {
                    throw new IllegalArgumentException("Format argumen salah: " + args[i]);
                }

                String key = kv[0].trim().toLowerCase(Locale.ROOT);
                String val = kv[1].trim();

                switch (key) {
                    case "pop":    cfg.popSize = Integer.parseInt(val); break;
                    case "budget": cfg.evaluationBudget = Long.parseLong(val); break;
                    case "pc":     cfg.crossoverRate = Double.parseDouble(val); break;
                    case "pm":     cfg.mutationRate = Double.parseDouble(val); break;
                    case "elit":   cfg.elitismPct = Double.parseDouble(val); break;
                    case "seed":   seedIndex = Integer.parseInt(val); break;
                    case "repr":   cfg.representation = Chromosome.Representation.valueOf(val.toUpperCase(Locale.ROOT)); break;
                    case "sel":    cfg.selectionType = GeneticAlgorithm.SelectionType.valueOf(val.toUpperCase(Locale.ROOT)); break;
                    case "tour":   cfg.tournamentSize = Integer.parseInt(val); break;
                    case "cx":     cfg.crossoverType = GeneticAlgorithm.CrossoverType.valueOf(val.toUpperCase(Locale.ROOT)); break;
                    case "mut":    cfg.mutationType = GeneticAlgorithm.MutationType.valueOf(val.toUpperCase(Locale.ROOT)); break;
                    case "stag":   cfg.stagnationLimit = Integer.parseInt(val); break;
                    case "waktu":  cfg.timeLimitMs = Long.parseLong(val); break;
                    default:
                        throw new IllegalArgumentException("Key tidak dikenal: " + key);
                }
            }

            if (seedIndex < 0 || seedIndex >= Seeds.JUMLAH) {
                throw new IllegalArgumentException("seed harus antara 0 dan " + (Seeds.JUMLAH - 1));
            }

            // Membaca peta dan membuat matriks jarak (BFS sekali di awal).
            MapData map = MapReader.readMap(args[0]);
            DistanceMatrix matrix = new DistanceMatrix(map, map.getKosong());

            // Random generator dari seed yang tetap.
            Random random = new Random(Seeds.generate()[seedIndex]);

            // Menjalankan GA.
            GeneticAlgorithm ga = new GeneticAlgorithm(matrix, map.getP(), cfg, random);
            Individual best = ga.run();

            // Output: baris pertama "p f", lalu p baris "x y".
            // x = kolom, y = baris, (1,1) di kiri atas (konvensi sementara).
            StringBuilder out = new StringBuilder();
            out.append(String.format(Locale.US, "%d %.5f%n", map.getP(), best.getCost()));

            for (int index : best.getChromosome().getStationIndices()) {
                Cell cell = matrix.getEmptyCells().get(index);
                out.append(cell.getY() + 1).append(' ').append(cell.getX() + 1).append('\n');
            }

            System.out.print(out);

        } catch (Exception e) {
            System.err.println("Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
