package algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Chromosome;
import model.Individual;

// Algoritma genetik untuk penempatan Fire Station.
public class GeneticAlgorithm {

    // Pilihan operator.
    public enum SelectionType { ROULETTE, TOURNAMENT }
    public enum CrossoverType { ONE_POINT, UNIFORM }
    public enum MutationType { SINGLE, MULTIPLE }

    private final DistanceMatrix matrix;
    private final GAConfig cfg;
    private final Random random;
    private final int emptyCount;
    private final int stationCount;

    // Mutasi hanya bisa dilakukan jika ada sel kosong alternatif.
    private final boolean canMutate;

    // Individu terbaik yang pernah ditemukan.
    private Individual best = null;

    // Statistik jalannya algoritma.
    private long evaluations = 0;
    private int generations = 0;

    public GeneticAlgorithm(DistanceMatrix matrix, int stationCount,
                            GAConfig cfg, Random random) {
        this.matrix = matrix;
        this.stationCount = stationCount;
        this.cfg = cfg;
        this.random = random;
        this.emptyCount = matrix.getEmptyCount();
        this.canMutate = stationCount < emptyCount;
    }

    // Menjalankan GA dan mengembalikan individu terbaik.
    public Individual run() {

        if (cfg.evaluationBudget < cfg.popSize) {
            throw new IllegalArgumentException(
                "Evaluation budget harus >= ukuran populasi");
        }

        long startTime = System.currentTimeMillis();
        int lastImprovement = 0;

        // Populasi awal dibuat acak.
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < cfg.popSize; i++) {
            population.add(evaluate(randomChromosome()));
        }

        // Loop generasi sampai kriteria terminasi terpenuhi.
        while (evaluations < cfg.evaluationBudget) {

            if (cfg.timeLimitMs > 0 &&
                System.currentTimeMillis() - startTime >= cfg.timeLimitMs) {
                break;
            }

            if (cfg.stagnationLimit > 0 &&
                generations - lastImprovement >= cfg.stagnationLimit) {
                break;
            }

            generations++;
            double bestBefore = best.getFitness();
            long evalsBefore = evaluations;

            // Urutkan populasi dari fitness tertinggi untuk elitism.
            List<Individual> sorted = new ArrayList<>(population);
            sorted.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));

            List<Individual> next = new ArrayList<>();

            // Elitism: individu terbaik langsung masuk generasi berikutnya.
            int eliteCount = (int) (cfg.elitismPct * cfg.popSize);
            for (int i = 0; i < eliteCount && i < sorted.size(); i++) {
                next.add(sorted.get(i));
            }

            // Sisa populasi diisi hasil seleksi, crossover, dan mutasi.
            while (next.size() < cfg.popSize &&
                   evaluations < cfg.evaluationBudget) {

                Individual p1 = selectParent(population);
                Individual p2 = selectParent(population);

                Chromosome child = p1.getChromosome();
                boolean changed = false;

                if (random.nextDouble() < cfg.crossoverRate) {
                    child = crossover(p1.getChromosome(), p2.getChromosome());
                    changed = true;
                }

                if (canMutate && random.nextDouble() < cfg.mutationRate) {
                    child = mutate(child);
                    changed = true;
                }

                // Anak yang tidak berubah tidak dihitung ulang fitness-nya.
                if (changed) {
                    next.add(evaluate(child));
                } else {
                    next.add(p1);
                }
            }

            population = next;

            if (best.getFitness() > bestBefore) {
                lastImprovement = generations;
            }

            // Jika tidak ada evaluasi sama sekali, algoritma tidak akan
            // pernah menghabiskan budget, jadi dihentikan.
            if (evaluations == evalsBefore) {
                break;
            }
        }

        return best;
    }

    // Menghitung fitness individu baru dan mencatat evaluasi.
    private Individual evaluate(Chromosome chromosome) {
        Individual individual = new Individual(chromosome, matrix);
        evaluations++;
        if (best == null || individual.getFitness() > best.getFitness()) {
            best = individual;
        }
        return individual;
    }

    // Membuat kromosom acak sesuai representasi.
    private Chromosome randomChromosome() {
        return Chromosome.createRandom(
            emptyCount, stationCount, cfg.representation, random);
    }

    // Memilih satu parent sesuai operator yang dipilih.
    private Individual selectParent(List<Individual> population) {
        if (cfg.selectionType == SelectionType.TOURNAMENT) {
            return Selection.tournament(population, cfg.tournamentSize, random);
        }
        return Selection.rouletteWheel(population, random);
    }

    // Crossover sesuai operator yang dipilih.
    private Chromosome crossover(Chromosome a, Chromosome b) {
        if (cfg.crossoverType == CrossoverType.UNIFORM) {
            return Crossover.uniform(a, b, random);
        }
        return Crossover.onePoint(a, b, random);
    }

    // Mutasi sesuai operator yang dipilih.
    private Chromosome mutate(Chromosome c) {
        if (cfg.mutationType == MutationType.MULTIPLE) {
            return Mutation.multipleRelocation(c, random);
        }
        return Mutation.singleRelocation(c, random);
    }

    // Statistik setelah run().
    public long getEvaluations() { return evaluations; }
    public int getGenerations() { return generations; }
}
