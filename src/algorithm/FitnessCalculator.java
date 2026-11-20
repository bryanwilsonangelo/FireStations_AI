package algorithm;

public class FitnessCalculator {
    // Menghitung fitness berdasarkan cost.
    public static double calculateFitness(double cost) {

        // Memastikan cost valid.
        if (Double.isNaN(cost) ||
            Double.isInfinite(cost) ||
            cost < 0) {

            throw new IllegalArgumentException(
                "Cost harus berupa angka positif atau nol"
            );
        }

        // Fitness berbanding terbalik dengan cost.
        return 1.0 / (1.0 + cost);
    }
}
