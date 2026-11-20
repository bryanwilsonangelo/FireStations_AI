package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

// Menjalankan eksperimen Tahap 2 (2A, 2B) dan Pengujian.
//
// Pemakaian (dari folder proyek):
//   java -cp out experiment.ExperimentRunnerTahap2 <mode> [file_csv] [jumlah_seed] [opsi_tambahan...] [--model 1|2|3]
public class ExperimentRunnerTahap2 {

    static class Peta {
        final String ukuran;
        final String path;

        Peta(String ukuran, String path) {
            this.ukuran = ukuran;
            this.path = path;
        }
    }

    static class ModelSpec {
        final Chromosome.Representation repr;
        final GeneticAlgorithm.SelectionType sel;
        final GeneticAlgorithm.CrossoverType cx;
        final GeneticAlgorithm.MutationType mut;

        ModelSpec(Chromosome.Representation repr, GeneticAlgorithm.SelectionType sel,
                  GeneticAlgorithm.CrossoverType cx, GeneticAlgorithm.MutationType mut) {
            this.repr = repr;
            this.sel = sel;
            this.cx = cx;
            this.mut = mut;
        }

        static ModelSpec parse(String teks) {
            String[] bagian = teks.trim().split("/");
            if (bagian.length != 4) {
                throw new IllegalArgumentException("Format model salah (harus REPR/SEL/CX/MUT): " + teks);
            }
            return new ModelSpec(
                Chromosome.Representation.valueOf(bagian[0].trim()),
                GeneticAlgorithm.SelectionType.valueOf(bagian[1].trim()),
                GeneticAlgorithm.CrossoverType.valueOf(bagian[2].trim()),
                GeneticAlgorithm.MutationType.valueOf(bagian[3].trim())
            );
        }

        String nama() {
            return repr + "/" + sel + "/" + cx + "/" + mut;
        }
    }

    static class Skenario {
        final ModelSpec model;
        final int pop;
        final double pc;
        final double pm;
        final double elit;

        Skenario(ModelSpec model, int pop, double pc, double pm, double elit) {
            this.model = model;
            this.pop = pop;
            this.pc = pc;
            this.pm = pm;
            this.elit = elit;
        }

        GAConfig toConfig(long budget) {
            GAConfig cfg = new GAConfig();
            cfg.representation = model.repr;
            cfg.selectionType = model.sel;
            cfg.crossoverType = model.cx;
            cfg.mutationType = model.mut;
            cfg.popSize = pop;
            cfg.evaluationBudget = budget;
            cfg.crossoverRate = pc;
            cfg.mutationRate = pm;
            cfg.elitismPct = elit;
            cfg.tournamentSize = ExperimentConfig.TOURNAMENT_SIZE;
            cfg.stagnationLimit = ExperimentConfig.STAGNASI;
            cfg.timeLimitMs = ExperimentConfig.WAKTU_MS;
            return cfg;
        }

        String ringkas() {
            return String.format(Locale.US, "%s [pop=%d, pc=%.2f, pm=%.3f, elit=%.2f]",
                model.nama(), pop, pc, pm, elit);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            cetakPanduan();
            System.exit(1);
        }

        // Pisahkan flag opsional --model <1|2|3> dari argumen posisional
        int filterModelIndex = -1; // -1 = semua model terpilih
        List<String> posArgsList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--model") && i + 1 < args.length) {
                filterModelIndex = Integer.parseInt(args[i + 1]) - 1;
                i++;
            } else {
                posArgsList.add(args[i]);
            }
        }
        String[] posArgs = posArgsList.toArray(new String[0]);

        String mode = posArgs[0].toLowerCase(Locale.ROOT);
        String fileCsv = posArgs.length > 1 ? posArgs[1] : "results/" + mode + ".csv";

        int defaultSeed = (mode.startsWith("tahap2a") || mode.equals("pengujian"))
            ? ExperimentConfig.JUMLAH_SEED_TAHAP2A
            : ExperimentConfig.JUMLAH_SEED;

        int jumlahSeed = posArgs.length > 2
            ? Integer.parseInt(posArgs[2])
            : defaultSeed;

        int[] seeds = Seeds.generate();
        if (jumlahSeed < 1 || jumlahSeed > seeds.length) {
            System.err.println("jumlah_seed harus antara 1 dan " + seeds.length);
            System.exit(1);
        }

        List<Skenario> daftarSkenario = new ArrayList<>();
        String fileDaftarPeta = "maps/daftar_tahap2.txt";

        if (mode.equals("tahap2a_pop")) {
            List<ModelSpec> models = ambilModel(filterModelIndex);
            for (int i = 0; i < models.size(); i++) {
                for (int j = 0; j < ExperimentConfig.KANDIDAT_POP.length; j++) {
                    daftarSkenario.add(new Skenario(
                        models.get(i),
                        ExperimentConfig.KANDIDAT_POP[j],
                        ExperimentConfig.PC,
                        ExperimentConfig.PM,
                        ExperimentConfig.ELIT
                    ));
                }
            }
        } else if (mode.equals("tahap2a_pc")) {
            if (posArgs.length < 4) {
                System.err.println("Butuh argumen <bestPop> untuk tahap2a_pc");
                System.exit(1);
            }
            int bestPop = Integer.parseInt(posArgs[3]);
            List<ModelSpec> models = ambilModel(filterModelIndex);
            for (int i = 0; i < models.size(); i++) {
                for (int j = 0; j < ExperimentConfig.KANDIDAT_PC.length; j++) {
                    daftarSkenario.add(new Skenario(
                        models.get(i),
                        bestPop,
                        ExperimentConfig.KANDIDAT_PC[j],
                        ExperimentConfig.PM,
                        ExperimentConfig.ELIT
                    ));
                }
            }
        } else if (mode.equals("tahap2a_pm")) {
            if (posArgs.length < 5) {
                System.err.println("Butuh argumen <bestPop> <bestPc> untuk tahap2a_pm");
                System.exit(1);
            }
            int bestPop = Integer.parseInt(posArgs[3]);
            double bestPc = Double.parseDouble(posArgs[4]);
            List<ModelSpec> models = ambilModel(filterModelIndex);
            for (int i = 0; i < models.size(); i++) {
                for (int j = 0; j < ExperimentConfig.KANDIDAT_PM.length; j++) {
                    daftarSkenario.add(new Skenario(
                        models.get(i),
                        bestPop,
                        bestPc,
                        ExperimentConfig.KANDIDAT_PM[j],
                        ExperimentConfig.ELIT
                    ));
                }
            }
        } else if (mode.equals("tahap2a_elit")) {
            if (posArgs.length < 6) {
                System.err.println("Butuh argumen <bestPop> <bestPc> <bestPm> untuk tahap2a_elit");
                System.exit(1);
            }
            int bestPop = Integer.parseInt(posArgs[3]);
            double bestPc = Double.parseDouble(posArgs[4]);
            double bestPm = Double.parseDouble(posArgs[5]);
            List<ModelSpec> models = ambilModel(filterModelIndex);
            for (int i = 0; i < models.size(); i++) {
                for (int j = 0; j < ExperimentConfig.KANDIDAT_ELIT.length; j++) {
                    daftarSkenario.add(new Skenario(
                        models.get(i),
                        bestPop,
                        bestPc,
                        bestPm,
                        ExperimentConfig.KANDIDAT_ELIT[j]
                    ));
                }
            }
        } else if (mode.equals("tahap2b")) {
            if (posArgs.length < 9) {
                System.err.println("Butuh argumen <pop1> <pop2> <pc1> <pc2> <bestPm> <bestElit> untuk tahap2b");
                System.exit(1);
            }
            int[] topPop = new int[]{ Integer.parseInt(posArgs[3]), Integer.parseInt(posArgs[4]) };
            double[] topPc = new double[]{ Double.parseDouble(posArgs[5]), Double.parseDouble(posArgs[6]) };
            double bestPm = Double.parseDouble(posArgs[7]);
            double bestElit = Double.parseDouble(posArgs[8]);

            List<ModelSpec> models = ambilModel(filterModelIndex);
            for (int i = 0; i < models.size(); i++) {
                for (int pIdx = 0; pIdx < topPop.length; pIdx++) {
                    for (int cIdx = 0; cIdx < topPc.length; cIdx++) {
                        daftarSkenario.add(new Skenario(
                            models.get(i),
                            topPop[pIdx],
                            topPc[cIdx],
                            bestPm,
                            bestElit
                        ));
                    }
                }
            }
        } else if (mode.equals("pengujian")) {
            if (posArgs.length < 9) {
                System.err.println("Butuh argumen <model_str> <pop> <pc> <pm> <elit> <daftar_peta_large>");
                System.exit(1);
            }
            ModelSpec modelFinal = ModelSpec.parse(posArgs[3]);
            int popFinal = Integer.parseInt(posArgs[4]);
            double pcFinal = Double.parseDouble(posArgs[5]);
            double pmFinal = Double.parseDouble(posArgs[6]);
            double elitFinal = Double.parseDouble(posArgs[7]);
            fileDaftarPeta = posArgs[8];

            daftarSkenario.add(new Skenario(modelFinal, popFinal, pcFinal, pmFinal, elitFinal));
        } else {
            cetakPanduan();
            System.exit(1);
        }

        List<Peta> daftarPeta = bacaDaftar(fileDaftarPeta);
        List<Peta> tersedia = new ArrayList<>();
        for (int i = 0; i < daftarPeta.size(); i++) {
            Peta p = daftarPeta.get(i);
            if (new File(p.path).exists()) {
                tersedia.add(p);
            } else {
                System.out.println("PERINGATAN: peta tidak ditemukan, dilewati: " + p.path);
            }
        }

        long total = (long) tersedia.size() * daftarSkenario.size() * jumlahSeed;
        System.out.println("Mode " + mode + ": " + tersedia.size() + " peta x "
            + daftarSkenario.size() + " skenario x " + jumlahSeed + " seed = " + total + " percobaan");
        System.out.println("Hasil disimpan ke: " + fileCsv);

        int nomor = 0;
        try (CSVWriter csv = new CSVWriter(fileCsv)) {
            for (int pIdx = 0; pIdx < tersedia.size(); pIdx++) {
                Peta p = tersedia.get(pIdx);
                MapData map = MapReader.readMap(p.path);
                DistanceMatrix matrix = new DistanceMatrix(map, map.getKosong());

                for (int kIdx = 0; kIdx < daftarSkenario.size(); kIdx++) {
                    Skenario sk = daftarSkenario.get(kIdx);

                    for (int s = 0; s < jumlahSeed; s++) {
                        GAConfig cfg = sk.toConfig(ExperimentConfig.budgetUntuk(p.ukuran));
                        Random rnd = new Random(seeds[s]);
                        GeneticAlgorithm ga = new GeneticAlgorithm(matrix, map.getP(), cfg, rnd);

                        long t0 = System.nanoTime();
                        Individual best = ga.run();
                        long ms = (System.nanoTime() - t0) / 1_000_000L;

                        nomor++;

                        csv.tulis(
                            mode, p.ukuran, p.path,
                            sk.model.repr.name(), sk.model.sel.name(), sk.model.cx.name(), sk.model.mut.name(),
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
                            nomor, total, p.path, sk.ringkas(), s, best.getCost());
                    }
                }
            }
        }

        System.out.println("Selesai. " + nomor + " percobaan dicatat.");
    }

    static List<ModelSpec> ambilModel(int filterIndex) throws IOException {
        List<ModelSpec> semua = bacaModelTerpilih("results/keputusan_tahap1.csv");
        if (filterIndex < 0) {
            return semua;
        }
        if (filterIndex >= semua.size()) {
            throw new IllegalArgumentException("Indeks model di luar batas (1.." + semua.size() + ")");
        }
        List<ModelSpec> satu = new ArrayList<>();
        satu.add(semua.get(filterIndex));
        return satu;
    }

    static List<ModelSpec> bacaModelTerpilih(String pathCsv) throws IOException {
        List<ModelSpec> hasil = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(pathCsv), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                throw new IOException("File kosong: " + pathCsv);
            }
            String[] h = header.split(",");
            int iModel = -1;
            int iDipilih = -1;
            for (int i = 0; i < h.length; i++) {
                String col = h[i].trim().toLowerCase(Locale.ROOT);
                if (col.equals("model")) iModel = i;
                if (col.equals("dipilih")) iDipilih = i;
            }
            if (iModel < 0 || iDipilih < 0) {
                throw new IOException("Kolom 'model' atau 'dipilih' tidak ditemukan di " + pathCsv);
            }

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] k = line.split(",");
                if (k[iDipilih].trim().equalsIgnoreCase("ya")) {
                    hasil.add(ModelSpec.parse(k[iModel]));
                }
            }
        }
        if (hasil.isEmpty()) {
            throw new IOException("Tidak ada model terpilih (dipilih=ya) di " + pathCsv);
        }
        return hasil;
    }

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

    static void cetakPanduan() {
        System.err.println("Pemakaian (tambah --model 1|2|3 di akhir jika dibagi per anggota):");
        System.err.println("  java -cp out experiment.ExperimentRunnerTahap2 tahap2a_pop  [csv] [seed]");
        System.err.println("  java -cp out experiment.ExperimentRunnerTahap2 tahap2a_pc   [csv] [seed] <bestPop>");
        System.err.println("  java -cp out experiment.ExperimentRunnerTahap2 tahap2a_pm   [csv] [seed] <bestPop> <bestPc>");
        System.err.println("  java -cp out experiment.ExperimentRunnerTahap2 tahap2a_elit [csv] [seed] <bestPop> <bestPc> <bestPm>");
        System.err.println("  java -cp out experiment.ExperimentRunnerTahap2 tahap2b      [csv] [seed] <pop1> <pop2> <pc1> <pc2> <bestPm> <bestElit>");
        System.err.println("  java -cp out experiment.ExperimentRunnerTahap2 pengujian    [csv] [seed] <model> <pop> <pc> <pm> <elit> <daftar_peta>");
    }
}