package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import model.Cell;
import model.Chromosome;
import model.Individual;
import model.MapData;
import model.Population;

// Class utama untuk menjalankan proses Genetic Algorithm (GA).
public class GeneticAlgorithm {

    // Jenis operator parent selection.
    public enum SelectionType {
        ROULETTE_WHEEL,
        TOURNAMENT
    }

    // Jenis operator crossover.
    public enum CrossoverType {
        ONE_POINT,
        UNIFORM
    }

    // Jenis operator mutasi.
    public enum MutationType {
        SINGLE_RELOCATION,
        MULTIPLE_RELOCATION
    }

    // Nilai default hyperparameter sesuai spesifikasi Tahap 1.
    public static final int DEFAULT_POPULATION_SIZE = 100;
    public static final double DEFAULT_CROSSOVER_RATE = 0.8;
    public static final double DEFAULT_MUTATION_RATE = 0.05;
    public static final double DEFAULT_ELITISM_RATE = 0.20;
    public static final int DEFAULT_TOURNAMENT_SIZE = 3;
    public static final int DEFAULT_MAX_STAGNANT_GENERATIONS = 50;
    public static final long DEFAULT_MAX_TIME_MILLIS = 60000L;

    // Matriks jarak dari rumah ke seluruh sel kosong.
    private final DistanceMatrix matrix;

    // Jumlah Fire Station (p) yang harus ditempatkan.
    private final int stationCount;

    // Ukuran populasi (pop).
    private final int populationSize;

    // Batas maksimal evaluasi fitness (evaluation budget).
    private final int evaluationBudget;

    // Jumlah maksimal generasi (evaluationBudget / populationSize).
    private final int maxGenerations;

    // Probabilitas crossover (pc).
    private final double crossoverRate;

    // Probabilitas mutasi (pm).
    private final double mutationRate;

    // Persentase elitism (e).
    private final double elitismRate;

    // Jenis representasi kromosom.
    private final Chromosome.Representation representation;

    // Jenis operator seleksi induk.
    private final SelectionType selectionType;

    // Jenis operator crossover.
    private final CrossoverType crossoverType;

    // Jenis operator mutasi.
    private final MutationType mutationType;

    // Generator angka acak (menggunakan seed eksperimen).
    private final Random random;

    // Ukuran turnamen untuk Tournament Selection.
    private int tournamentSize;

    // Batas generasi stagnan untuk menghentikan algoritma lebih awal.
    private int maxStagnantGenerations;

    // Batas waktu eksekusi dalam milidetik.
    private long maxTimeMillis;

    // Menyimpan populasi pada generasi saat ini.
    private Population currentPopulation;

    // Menyimpan individu terbaik yang ditemukan selama eksekusi GA.
    private Individual bestIndividual;

    // Menyimpan jumlah generasi yang telah dijalankan.
    private int generationsRun;

    // Menyimpan jumlah evaluasi fitness yang telah digunakan.
    private int evaluationsUsed;

    // Menyimpan durasi waktu eksekusi dalam milidetik.
    private long executionTimeMillis;

    // Menyimpan alasan berhentinya algoritma.
    private String stopReason;

    // Menyimpan riwayat cost terbaik pada setiap generasi.
    private final List<Double> bestCostHistory;

    // Menyimpan riwayat rata-rata cost pada setiap generasi.
    private final List<Double> averageCostHistory;

    // Constructor dengan hyperparameter default Tahap 1 menggunakan MapData.
    public GeneticAlgorithm(
            MapData map,
            DistanceMatrix matrix,
            Chromosome.Representation representation,
            SelectionType selectionType,
            CrossoverType crossoverType,
            MutationType mutationType,
            int evaluationBudget,
            Random random) {

        this(
            matrix,
            validateMapAndGetP(map),
            DEFAULT_POPULATION_SIZE,
            evaluationBudget,
            DEFAULT_CROSSOVER_RATE,
            DEFAULT_MUTATION_RATE,
            DEFAULT_ELITISM_RATE,
            representation,
            selectionType,
            crossoverType,
            mutationType,
            random
        );
    }

    // Constructor lengkap menggunakan MapData untuk eksperimen Tahap 1 dan Tahap 2.
    public GeneticAlgorithm(
            MapData map,
            DistanceMatrix matrix,
            int populationSize,
            int evaluationBudget,
            double crossoverRate,
            double mutationRate,
            double elitismRate,
            Chromosome.Representation representation,
            SelectionType selectionType,
            CrossoverType crossoverType,
            MutationType mutationType,
            Random random) {

        this(
            matrix,
            validateMapAndGetP(map),
            populationSize,
            evaluationBudget,
            crossoverRate,
            mutationRate,
            elitismRate,
            representation,
            selectionType,
            crossoverType,
            mutationType,
            random
        );
    }

    // Constructor utama dengan seluruh konfigurasi GA.
    public GeneticAlgorithm(
            DistanceMatrix matrix,
            int stationCount,
            int populationSize,
            int evaluationBudget,
            double crossoverRate,
            double mutationRate,
            double elitismRate,
            Chromosome.Representation representation,
            SelectionType selectionType,
            CrossoverType crossoverType,
            MutationType mutationType,
            Random random) {

        // Memastikan objek utama tidak null.
        if (matrix == null ||
            representation == null ||
            selectionType == null ||
            crossoverType == null ||
            mutationType == null ||
            random == null) {

            throw new IllegalArgumentException(
                "Parameter objek pada GeneticAlgorithm tidak boleh null"
            );
        }

        // Memastikan jumlah FS valid terhadap jumlah sel kosong.
        if (stationCount <= 0 ||
            stationCount > matrix.getEmptyCount()) {

            throw new IllegalArgumentException(
                "Jumlah Fire Station tidak valid"
            );
        }

        // Memastikan ukuran populasi valid.
        if (populationSize <= 0) {
            throw new IllegalArgumentException(
                "Ukuran populasi harus lebih dari 0"
            );
        }

        // Memastikan evaluation budget valid.
        if (evaluationBudget <= 0) {
            throw new IllegalArgumentException(
                "Evaluation budget harus lebih dari 0"
            );
        }

        // Memastikan probabilitas crossover berada pada rentang [0, 1].
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException(
                "Probabilitas crossover harus antara 0.0 hingga 1.0"
            );
        }

        // Memastikan probabilitas mutasi berada pada rentang [0, 1].
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException(
                "Probabilitas mutasi harus antara 0.0 hingga 1.0"
            );
        }

        // Memastikan persentase elitism berada pada rentang [0, 1].
        if (elitismRate < 0.0 || elitismRate > 1.0) {
            throw new IllegalArgumentException(
                "Persentase elitism harus antara 0.0 hingga 1.0"
            );
        }

        this.matrix = matrix;
        this.stationCount = stationCount;
        this.populationSize = populationSize;
        this.evaluationBudget = evaluationBudget;

        // Menghitung jumlah generasi maksimal berdasarkan evaluation budget.
        this.maxGenerations = Math.max(
            1,
            evaluationBudget / populationSize
        );

        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitismRate = elitismRate;

        this.representation = representation;
        this.selectionType = selectionType;
        this.crossoverType = crossoverType;
        this.mutationType = mutationType;
        this.random = random;

        // Mengatur nilai batas default tambahan.
        this.tournamentSize = DEFAULT_TOURNAMENT_SIZE;
        this.maxStagnantGenerations = DEFAULT_MAX_STAGNANT_GENERATIONS;
        this.maxTimeMillis = DEFAULT_MAX_TIME_MILLIS;

        this.bestCostHistory = new ArrayList<>();
        this.averageCostHistory = new ArrayList<>();
        this.stopReason = "Belum dijalankan";
    }

    // Memvalidasi objek MapData dan mengambil nilai p.
    private static int validateMapAndGetP(MapData map) {

        if (map == null) {
            throw new IllegalArgumentException(
                "MapData tidak boleh null"
            );
        }

        return map.getP();
    }

    // Menjalankan proses Genetic Algorithm hingga kondisi berhenti terpenuhi.
    public Individual run() {

        // Mencatat waktu mulai eksekusi.
        long startTime = System.currentTimeMillis();

        // Mereset status eksekusi.
        generationsRun = 0;
        evaluationsUsed = 0;
        bestIndividual = null;
        bestCostHistory.clear();
        averageCostHistory.clear();
        stopReason = "Evaluation budget habis";

        // Membuat populasi awal secara acak (generasi ke-1).
        currentPopulation = Population.createRandom(
            populationSize,
            matrix.getEmptyCount(),
            stationCount,
            representation,
            matrix,
            random
        );

        // Mencatat evaluasi populasi awal.
        evaluationsUsed += currentPopulation.size();
        generationsRun = 1;

        // Mengambil individu terbaik dari populasi awal.
        bestIndividual = currentPopulation.getBestIndividual();

        // Mencatat riwayat cost generasi pertama.
        recordHistory(currentPopulation);

        // Menghitung jumlah individu elit yang dipertahankan setiap generasi.
        int eliteCount = calculateEliteCount();

        // Menyimpan jumlah generasi berturut-turut tanpa perbaikan solusi.
        int stagnantGenerations = 0;

        // Melakukan iterasi evolusi selama kondisi berhenti belum tercapai.
        while (generationsRun < maxGenerations &&
               evaluationsUsed < evaluationBudget) {

            // Memeriksa apakah batas waktu eksekusi telah terlampaui.
            if (maxTimeMillis > 0 &&
                (System.currentTimeMillis() - startTime) >= maxTimeMillis) {

                stopReason = "Melewati batas waktu";
                break;
            }

            // Memeriksa apakah hasil GA sudah stagnan.
            if (maxStagnantGenerations > 0 &&
                stagnantGenerations >= maxStagnantGenerations) {

                stopReason = "Hasil GA stagnan";
                break;
            }

            // Membuat populasi baru untuk generasi berikutnya.
            Population nextPopulation = evolvePopulation(
                currentPopulation,
                eliteCount
            );

            // Memperbarui populasi saat ini dan jumlah generasi.
            currentPopulation = nextPopulation;
            generationsRun++;

            // Mengambil individu terbaik pada generasi terbaru.
            Individual generationBest =
                currentPopulation.getBestIndividual();

            // Memeriksa apakah ditemukan solusi dengan fitness yang lebih baik.
            if (generationBest.getFitness() > bestIndividual.getFitness()) {

                bestIndividual = generationBest;
                stagnantGenerations = 0;
            } else {

                stagnantGenerations++;
            }

            // Mencatat riwayat cost generasi saat ini.
            recordHistory(currentPopulation);
        }

        // Mencatat total waktu eksekusi.
        executionTimeMillis = System.currentTimeMillis() - startTime;

        return bestIndividual;
    }

    // Membentuk populasi generasi berikutnya melalui elitism, selection, crossover, dan mutation.
    private Population evolvePopulation(
            Population population,
            int eliteCount) {

        Population nextPopulation = new Population();

        // 1. Elitism: menyalin individu terbaik langsung ke generasi berikutnya.
        if (eliteCount > 0) {

            List<Individual> elites =
                population.getTopIndividuals(eliteCount);

            nextPopulation.addAll(elites);
        }

        // Mengambil list individu untuk proses parent selection.
        List<Individual> candidates = population.getIndividuals();

        // 2. Mengisi sisa populasi dengan offspring hasil reproduksi.
        while (nextPopulation.size() < populationSize) {

            // Memilih dua induk berdasarkan operator parent selection.
            Individual parent1 = selectParent(candidates);
            Individual parent2 = selectParent(candidates);

            // Melakukan crossover sesuai probabilitas crossover (pc).
            Chromosome childChromosome;

            if (random.nextDouble() < crossoverRate) {
                childChromosome = crossover(
                    parent1.getChromosome(),
                    parent2.getChromosome()
                );
            } else {
                // Jika tidak crossover, salin kromosom dari salah satu induk.
                childChromosome = random.nextBoolean()
                    ? parent1.getChromosome()
                    : parent2.getChromosome();
            }

            // Melakukan mutasi sesuai probabilitas mutasi (pm).
            if (random.nextDouble() < mutationRate) {
                childChromosome = mutate(childChromosome);
            }

            // Mengevaluasi kromosom anak menjadi individu baru.
            Individual child = new Individual(
                childChromosome,
                matrix
            );

            // Menambahkan individu anak ke populasi baru.
            nextPopulation.add(child);
        }

        // Menambahkan jumlah evaluasi fitness pada generasi ini.
        evaluationsUsed += populationSize;

        return nextPopulation;
    }

    // Memilih satu induk menggunakan operator seleksi yang dipilih.
    private Individual selectParent(List<Individual> population) {

        if (selectionType == SelectionType.ROULETTE_WHEEL) {
            return Selection.rouletteWheel(
                population,
                random
            );
        } else {
            return Selection.tournament(
                population,
                tournamentSize,
                random
            );
        }
    }

    // Melakukan crossover pada dua kromosom induk menggunakan operator yang dipilih.
    private Chromosome crossover(
            Chromosome parent1,
            Chromosome parent2) {

        if (crossoverType == CrossoverType.ONE_POINT) {
            return Crossover.onePoint(
                parent1,
                parent2,
                random
            );
        } else {
            return Crossover.uniform(
                parent1,
                parent2,
                random
            );
        }
    }

    // Melakukan mutasi pada kromosom menggunakan operator yang dipilih.
    private Chromosome mutate(Chromosome chromosome) {

        // Jika seluruh sel kosong sudah terisi FS, mutasi tidak dapat memindahkan FS.
        if (chromosome.getStationCount() >= chromosome.getEmptyCount()) {
            return chromosome;
        }

        if (mutationType == MutationType.SINGLE_RELOCATION) {
            return Mutation.singleRelocation(
                chromosome,
                random
            );
        } else {
            return Mutation.multipleRelocation(
                chromosome,
                random
            );
        }
    }

    // Menghitung jumlah individu elit berdasarkan persentase elitism.
    private int calculateEliteCount() {

        int count = (int) Math.round(
            populationSize * elitismRate
        );

        // Memastikan jumlah elit berada pada rentang [0, populationSize].
        if (count < 0) {
            return 0;
        }

        if (count > populationSize) {
            return populationSize;
        }

        return count;
    }

    // Menyimpan riwayat cost terbaik dan rata-rata pada generasi saat ini.
    private void recordHistory(Population population) {

        bestCostHistory.add(
            bestIndividual.getCost()
        );

        averageCostHistory.add(
            population.getAverageCost()
        );
    }

    // Memformat keluaran akhir sesuai spesifikasi soal (p+1 baris).
    public String formatOutput() {

        // Memastikan GA sudah dijalankan.
        if (bestIndividual == null) {
            throw new IllegalStateException(
                "GeneticAlgorithm belum dijalankan"
            );
        }

        StringBuilder sb = new StringBuilder();

        // Baris pertama: nilai p dan rata-rata jarak terdekat f (5 angka di belakang koma).
        sb.append(stationCount)
          .append(" ")
          .append(String.format(Locale.US, "%.5f", bestIndividual.getCost()));

        // Mengambil daftar indeks sel kosong tempat FS berada.
        int[] stationIndices =
            bestIndividual.getChromosome().getStationIndices();

        List<Cell> emptyCells = matrix.getEmptyCells();

        // p baris berikutnya: koordinat (x, y) setiap FS dengan (1,1) di kiri atas.
        for (int index : stationIndices) {

            Cell cell = emptyCells.get(index);

            sb.append("\n")
              .append(cell.getX() + 1)
              .append(" ")
              .append(cell.getY() + 1);
        }

        return sb.toString();
    }

    // Mengambil daftar koordinat Cell tempat Fire Station ditempatkan pada solusi terbaik.
    public List<Cell> getBestStationCells() {

        // Memastikan GA sudah dijalankan.
        if (bestIndividual == null) {
            throw new IllegalStateException(
                "GeneticAlgorithm belum dijalankan"
            );
        }

        int[] stationIndices =
            bestIndividual.getChromosome().getStationIndices();

        List<Cell> emptyCells = matrix.getEmptyCells();
        List<Cell> result = new ArrayList<>();

        for (int index : stationIndices) {
            result.add(emptyCells.get(index));
        }

        return result;
    }

    // Mengatur ukuran turnamen untuk Tournament Selection.
    public void setTournamentSize(int tournamentSize) {

        if (tournamentSize <= 0) {
            throw new IllegalArgumentException(
                "Tournament size harus lebih dari 0"
            );
        }

        this.tournamentSize = tournamentSize;
    }

    // Mengambil ukuran turnamen.
    public int getTournamentSize() {
        return tournamentSize;
    }

    // Mengatur batas generasi stagnan (<= 0 untuk menonaktifkan).
    public void setMaxStagnantGenerations(int maxStagnantGenerations) {
        this.maxStagnantGenerations = maxStagnantGenerations;
    }

    // Mengambil batas generasi stagnan.
    public int getMaxStagnantGenerations() {
        return maxStagnantGenerations;
    }

    // Mengatur batas waktu eksekusi dalam milidetik (<= 0 untuk menonaktifkan).
    public void setMaxTimeMillis(long maxTimeMillis) {
        this.maxTimeMillis = maxTimeMillis;
    }

    // Mengambil batas waktu eksekusi dalam milidetik.
    public long getMaxTimeMillis() {
        return maxTimeMillis;
    }

    // Mengambil individu terbaik hasil eksekusi GA.
    public Individual getBestIndividual() {
        return bestIndividual;
    }

    // Mengambil nilai cost terbaik hasil eksekusi GA.
    public double getBestCost() {

        if (bestIndividual == null) {
            throw new IllegalStateException(
                "GeneticAlgorithm belum dijalankan"
            );
        }

        return bestIndividual.getCost();
    }

    // Mengambil nilai fitness terbaik hasil eksekusi GA.
    public double getBestFitness() {

        if (bestIndividual == null) {
            throw new IllegalStateException(
                "GeneticAlgorithm belum dijalankan"
            );
        }

        return bestIndividual.getFitness();
    }

    // Mengambil populasi generasi terakhir.
    public Population getCurrentPopulation() {
        return currentPopulation;
    }

    // Mengambil jumlah generasi yang telah dijalankan.
    public int getGenerationsRun() {
        return generationsRun;
    }

    // Mengambil jumlah evaluasi fitness yang telah digunakan.
    public int getEvaluationsUsed() {
        return evaluationsUsed;
    }

    // Mengambil waktu eksekusi dalam milidetik.
    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    // Mengambil alasan berhentinya algoritma.
    public String getStopReason() {
        return stopReason;
    }

    // Mengambil riwayat cost terbaik per generasi.
    public List<Double> getBestCostHistory() {
        return Collections.unmodifiableList(bestCostHistory);
    }

    // Mengambil riwayat rata-rata cost per generasi.
    public List<Double> getAverageCostHistory() {
        return Collections.unmodifiableList(averageCostHistory);
    }

    // Mengambil ukuran populasi.
    public int getPopulationSize() {
        return populationSize;
    }

    // Mengambil evaluation budget.
    public int getEvaluationBudget() {
        return evaluationBudget;
    }

    // Mengambil jumlah maksimal generasi.
    public int getMaxGenerations() {
        return maxGenerations;
    }

    // Mengambil probabilitas crossover.
    public double getCrossoverRate() {
        return crossoverRate;
    }

    // Mengambil probabilitas mutasi.
    public double getMutationRate() {
        return mutationRate;
    }

    // Mengambil persentase elitism.
    public double getElitismRate() {
        return elitismRate;
    }

    // Mengambil jenis representasi kromosom.
    public Chromosome.Representation getRepresentation() {
        return representation;
    }

    // Mengambil jenis operator seleksi induk.
    public SelectionType getSelectionType() {
        return selectionType;
    }

    // Mengambil jenis operator crossover.
    public CrossoverType getCrossoverType() {
        return crossoverType;
    }

    // Mengambil jenis operator mutasi.
    public MutationType getMutationType() {
        return mutationType;
    }
}
