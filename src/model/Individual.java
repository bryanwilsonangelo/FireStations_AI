
package model;

import algorithm.DistanceMatrix;
import algorithm.CostCalculator;
import algorithm.FitnessCalculator;

// Class untuk menyimpan satu individu dalam populasi GA.
public class Individual {

    // Kromosom individu.
    private final Chromosome chromosome;

    // Nilai cost.
    private final double cost;

    // Nilai fitness.
    private final double fitness;

    // Constructor untuk membuat dan mengevaluasi individu.
    public Individual(
            Chromosome chromosome,
            DistanceMatrix matrix) {

        if (chromosome == null || matrix == null) {
            throw new IllegalArgumentException(
                "Chromosome dan matrix tidak boleh null"
            );
        }

        // Memastikan ukuran kromosom sesuai dengan peta.
        if (chromosome.getEmptyCount() != matrix.getEmptyCount()) {
            throw new IllegalArgumentException(
                "Ukuran kromosom tidak sesuai dengan matriks jarak"
            );
        }

        if (chromosome.getStationCount() == 0 ||
            chromosome.getStationCount() > matrix.getEmptyCount()) {
            throw new IllegalArgumentException(
                "Jumlah FS tidak valid"
            );
        }

        this.chromosome = chromosome;

        // Menghitung cost.
        this.cost = CostCalculator.calculateCost(
            matrix,
            chromosome.getStationIndices()
        );

        // Menghitung fitness.
        this.fitness = FitnessCalculator.calculateFitness(
            cost
        );
    }

    // Mengambil kromosom.
    public Chromosome getChromosome() {
        return chromosome;
    }

    // Mengambil cost.
    public double getCost() {
        return cost;
    }

    // Mengambil fitness.
    public double getFitness() {
        return fitness;
    }

    // Menampilkan informasi individu.
    @Override
    public String toString() {

        return "Individual {" +
            "chromosome=" + chromosome +
            ", cost=" + String.format("%.5f", cost) +
            ", fitness=" + String.format("%.5f", fitness) +
            '}';
    }
}
