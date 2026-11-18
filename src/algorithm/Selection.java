
package algorithm;

import java.util.List;
import java.util.Random;

import model.Individual;


public class Selection {

    // Roulette Wheel Selection.
    public static Individual rouletteWheel(
            List<Individual> population,
            Random random) {

        if (population == null || population.isEmpty()) {
            throw new IllegalArgumentException(
                "Populasi tidak boleh kosong"
            );
        }

        // Menghitung total fitness.
        double totalFitness = 0.0;

        for (Individual individual : population) {
            totalFitness += individual.getFitness();
        }

        // Jika total fitness invalid, fallback ke random.
        if (totalFitness <= 0 ||
            Double.isNaN(totalFitness) ||
            Double.isInfinite(totalFitness)) {

            return population.get(
                random.nextInt(population.size())
            );
        }

        // Mengambil nilai acak pada rentang total fitness.
        double randomValue = random.nextDouble() * totalFitness;

        double cumulativeFitness = 0.0;

        // Memilih individu berdasarkan fitness kumulatif.
        for (Individual individual : population) {

            cumulativeFitness += individual.getFitness();

            if (randomValue <= cumulativeFitness) {
                return individual;
            }
        }

        // Fallback untuk masalah floating-point.
        return population.get(population.size() - 1);
    }

    // Tournament Selection.
    public static Individual tournament(
            List<Individual> population,
            int tournamentSize,
            Random random) {

        if (population == null || population.isEmpty()) {
            throw new IllegalArgumentException(
                "Populasi tidak boleh kosong"
            );
        }

        if (tournamentSize <= 0) {
            throw new IllegalArgumentException(
                "Tournament size harus lebih dari 0"
            );
        }

        // Ukuran tournament tidak boleh melebihi populasi.
        int actualSize = Math.min(
            tournamentSize,
            population.size()
        );

        Individual best = null;

        // Mengambil kandidat secara acak.
        for (int i = 0; i < actualSize; i++) {

            Individual candidate = population.get(
                random.nextInt(population.size())
            );

            // Mengambil kandidat dengan fitness terbaik.
            if (best == null ||
                candidate.getFitness() > best.getFitness()) {

                best = candidate;
            }
        }

        return best;
    }
}
