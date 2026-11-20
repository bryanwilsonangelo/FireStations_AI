package algorithm;

import model.Chromosome;

// Menyimpan seluruh hyperparameter GA dalam satu objek.
public class GAConfig {

    // Representasi kromosom yang dipakai (BINARY atau INTEGER).
    public Chromosome.Representation representation = Chromosome.Representation.INTEGER;

    // Operator parent selection.
    public GeneticAlgorithm.SelectionType selectionType = GeneticAlgorithm.SelectionType.ROULETTE;

    // Operator crossover.
    public GeneticAlgorithm.CrossoverType crossoverType = GeneticAlgorithm.CrossoverType.ONE_POINT;

    // Operator mutasi.
    public GeneticAlgorithm.MutationType mutationType = GeneticAlgorithm.MutationType.SINGLE;

    // Jumlah individu dalam populasi.
    public int popSize = 100;

    // Batas jumlah perhitungan fitness (evaluation budget).
    public long evaluationBudget = 10000;

    // Probabilitas crossover.
    public double crossoverRate = 0.8;

    // Probabilitas mutasi.
    public double mutationRate = 0.05;

    // Persentase elitism (0-1).
    public double elitismPct = 0.2;

    // Ukuran tournament (jika memakai tournament selection).
    public int tournamentSize = 3;

    // Berhenti jika best tidak membaik selama sekian generasi (0 = nonaktif).
    public int stagnationLimit = 0;

    // Batas waktu dalam milidetik (0 = nonaktif).
    public long timeLimitMs = 0;
}
