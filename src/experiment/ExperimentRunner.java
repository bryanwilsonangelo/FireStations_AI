package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import algorithm.DistanceMatrix;
import algorithm.GAConfig;
import algorithm.GeneticAlgorithm;
import input.MapReader;
import model.Chromosome;
import model.Individual;
import model.MapData;

// Menjalankan eksperimen tahap 1: 16 kombinasi operator x peta x 30 seed.
//
// Pemakaian (dari folder proyek):
//   java -cp out experiment.ExperimentRunner tahap1 [file_csv] [jumlah_seed]
// Contoh:
//   java -cp out experiment.ExperimentRunner tahap1 results/tahap1.csv 2
public class ExperimentRunner {

    // Satu baris dari maps/daftar_tahap1.txt.
    static class Peta {
        final String ukuran;
        final String path;
        Peta(String ukuran, String path) {
            this.ukuran = ukuran;
            this.path = path;
        }
    }

    // Satu kombinasi operator genetik (16 kombinasi di tahap 1).
    static class Kombinasi {
        final Chromosome.Representation repr;
        final GeneticAlgorithm.SelectionType sel;
        final GeneticAlgorithm.CrossoverType cx;
        final GeneticAlgorithm.MutationType mut;

        Kombinasi(Chromosome.Representation repr, GeneticAlgorithm.SelectionType sel,
                  GeneticAlgorithm.CrossoverType cx, GeneticAlgorithm.MutationType mut) {
            this.repr = repr;
            this.sel = sel;
            this.cx = cx;
            this.mut = mut;
        }

        String nama() {
            return repr + "/" + sel + "/" + cx + "/" + mut;
        }

        // Membuat konfigurasi GA untuk kombinasi ini dengan budget tertentu.
        GAConfig toConfig(long budget) {
            GAConfig cfg = new GAConfig();
            cfg.representation = repr;
            cfg.selectionType = sel;
            cfg.crossoverType = cx;
            cfg.mutationType = mut;
            cfg.popSize = ExperimentConfig.POP;
            cfg.evaluationBudget = budget;
            cfg.crossoverRate = ExperimentConfig.PC;
            cfg.mutationRate = ExperimentConfig.PM;
            cfg.elitismPct = ExperimentConfig.ELIT;
            cfg.tournamentSize = ExperimentConfig.TOURNAMENT_SIZE;
            cfg.stagnationLimit = ExperimentConfig.STAGNASI;
            cfg.timeLimitMs = ExperimentConfig.WAKTU_MS;
            return cfg;
        }
    }

    public static void main(String[] args) throws IOException {

        if (args.length < 1 || !args[0].equals("tahap1")) {
            System.err.println("Pemakaian: java -cp out experiment.ExperimentRunner tahap1 [file_csv] [jumlah_seed]");
            System.err.println("Tahap 2 dan pengujian akan ditambahkan setelah hasil tahap 1 tersedia.");
            System.exit(1);
        }

        String tahap = args[0];
        String fileCsv = args.length > 1 ? args[1] : "results/" + tahap + ".csv";
        int jumlahSeed = args.length > 2
            ? Integer.parseInt(args[2])
            : ExperimentConfig.JUMLAH_SEED;

        int[] seeds = Seeds.generate();
        if (jumlahSeed < 1 || jumlahSeed > seeds.length) {
            System.err.println("jumlah_seed harus antara 1 dan " + seeds.length);
            System.exit(1);
        }

        // Membaca daftar peta dan memeriksa keberadaannya.
        List<Peta> daftar = bacaDaftar("maps/daftar_tahap1.txt");
        List<Peta> tersedia = new ArrayList<>();
        for (Peta p : daftar) {
            if (new File(p.path).exists()) {
                tersedia.add(p);
            } else {
                System.out.println("PERINGATAN: peta tidak ditemukan, dilewati: " + p.path);
            }
        }

        List<Kombinasi> kombinasi = kombinasiTahap1();
        long total = (long) tersedia.size() * kombinasi.size() * jumlahSeed;
        System.out.println("Tahap 1: " + tersedia.size() + " peta x " + kombinasi.size()
            + " kombinasi x " + jumlahSeed + " seed = " + total + " percobaan");
        System.out.println("Hasil disimpan ke: " + fileCsv);

        int nomor = 0;
        try (CSVWriter csv = new CSVWriter(fileCsv)) {

            for (Peta p : tersedia) {

                MapData map = MapReader.readMap(p.path);
                DistanceMatrix matrix = new DistanceMatrix(map, map.getKosong());

                for (Kombinasi k : kombinasi) {

                    for (int s = 0; s < jumlahSeed; s++) {

                        GAConfig cfg = k.toConfig(ExperimentConfig.budgetUntuk(p.ukuran));
                        Random rnd = new Random(seeds[s]);
                        GeneticAlgorithm ga = new GeneticAlgorithm(matrix, map.getP(), cfg, rnd);

                        long t0 = System.nanoTime();
                        Individual best = ga.run();
                        long ms = (System.nanoTime() - t0) / 1_000_000L;

                        nomor++;

                        csv.tulis(
                            tahap, p.ukuran, p.path,
                            k.repr.name(), k.sel.name(), k.cx.name(), k.mut.name(),
                            String.valueOf(cfg.popSize),
                            String.valueOf(cfg.evaluationBudget),
                            String.valueOf(cfg.crossoverRate),
                            String.valueOf(cfg.mutationRate),
                            String.valueOf(cfg.elitismPct),
                            String.valueOf(s),
                            String.valueOf(seeds[s]),
                            String.valueOf(best.getCost()),
                            String.valueOf(best.getFitness()),
                            String.valueOf(ga.getEvaluations()),
                            String.valueOf(ga.getGenerations()),
                            String.valueOf(ms),
                            String.valueOf(map.getP())
                        );

                        System.out.printf(Locale.US, "[%d/%d] %s %s seed=%d cost=%.5f%n",
                            nomor, total, p.path, k.nama(), s, best.getCost());
                    }
                }
            }
        }

        System.out.println("Selesai. " + nomor + " percobaan dicatat.");
    }

    // Membaca maps/daftar_tahap1.txt. Format: "ukuran path". Baris '#' diabaikan.
    static List<Peta> bacaDaftar(String path) throws IOException {
        List<Peta> hasil = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] kolom = line.split("\\s+", 2);
                if (kolom.length != 2) {
                    throw new IOException("Format daftar peta salah: " + line);
                }
                hasil.add(new Peta(kolom[0], kolom[1]));
            }
        }
        return hasil;
    }

    // 16 kombinasi: 2 representasi x 2 seleksi x 2 crossover x 2 mutasi.
    static List<Kombinasi> kombinasiTahap1() {
        List<Kombinasi> hasil = new ArrayList<>();
        for (Chromosome.Representation r : Chromosome.Representation.values()) {
            for (GeneticAlgorithm.SelectionType s : GeneticAlgorithm.SelectionType.values()) {
                for (GeneticAlgorithm.CrossoverType c : GeneticAlgorithm.CrossoverType.values()) {
                    for (GeneticAlgorithm.MutationType m : GeneticAlgorithm.MutationType.values()) {
                        hasil.add(new Kombinasi(r, s, c, m));
                    }
                }
            }
        }
        return hasil;
    }
}
