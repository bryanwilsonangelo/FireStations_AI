
package algorithm;

import java.util.HashSet;
import java.util.Set;

// Class untuk menghitung rata-rata jarak rumah ke FS terdekat.
public class CostCalculator {

    // Menghitung cost dari sekumpulan posisi FS.
    public static double calculateCost(
            DistanceMatrix matrix,
            int[] stationIndices) {

        int houseCount = matrix.getHouseCount();

        // Memastikan jumlah rumah tidak nol.
        if (houseCount == 0) {
            return 0.0;
        }

        // Memastikan jumlah FS tidak nol.
        if (stationIndices == null ||
            stationIndices.length == 0) {

            throw new IllegalArgumentException(
                "Jumlah Fire Station tidak boleh nol"
            );
        }

        // Memastikan tidak ada posisi FS duplikat.
        Set<Integer> used = new HashSet<>();

        for (int index : stationIndices) {

            if (index < 0 ||
                index >= matrix.getEmptyCount()) {

                throw new IllegalArgumentException(
                    "Indeks sel kosong tidak valid"
                );
            }

            if (!used.add(index)) {

                throw new IllegalArgumentException(
                    "Posisi Fire Station duplikat"
                );
            }
        }

        double totalDistance = 0.0;

        // Menghitung jarak minimum setiap rumah.
        for (int i = 0; i < houseCount; i++) {

            int minDistance = Integer.MAX_VALUE;

            // Memeriksa semua Fire Station.
            for (int stationIndex : stationIndices) {

                int distance = matrix.getDistance(
                    i, stationIndex
                );

                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            // Menambahkan jarak terdekat.
            totalDistance += minDistance;
        }

        // Menghitung rata-rata jarak.
        return totalDistance / houseCount;
    }
}
