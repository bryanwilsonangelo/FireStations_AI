
package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import model.Chromosome;

// Class mutation untuk mengubah posisi Fire Station.
public class Mutation {

    // Single Relocation Mutation.
    public static Chromosome singleRelocation(
            Chromosome chromosome,
            Random random) {

        validate(chromosome);

        int[] stations = chromosome.getStationIndices();

        int emptyCount = chromosome.getEmptyCount();

        // Memilih satu FS secara acak.
        int selectedStation =
            random.nextInt(stations.length);

        // Menyimpan posisi yang sudah digunakan.
        boolean[] occupied = new boolean[emptyCount];

        for (int index : stations) {
            occupied[index] = true;
        }

        // Mengosongkan posisi FS yang dipilih.
        occupied[stations[selectedStation]] = false;

        // Membuat daftar kandidat sel kosong.
        List<Integer> candidates = new ArrayList<>();

        for (int i = 0; i < emptyCount; i++) {

            if (!occupied[i]) {
                candidates.add(i);
            }
        }

        // Mengambil posisi baru secara acak.
        int newPosition = candidates.get(
            random.nextInt(candidates.size())
        );

        // Memindahkan FS.
        stations[selectedStation] = newPosition;

        // Mengembalikan kromosom valid.
        return Chromosome.fromStationIndices(
            stations,
            emptyCount,
            chromosome.getRepresentation()
        );
    }

    // Multiple Relocation Mutation.
    public static Chromosome multipleRelocation(
            Chromosome chromosome,
            Random random) {

        validate(chromosome);

        int[] stations = chromosome.getStationIndices();

        int emptyCount = chromosome.getEmptyCount();

        int stationCount = stations.length;

        // Menentukan jumlah FS yang akan dipindahkan.
        int moveCount = 1 + random.nextInt(stationCount);

        // Mengacak urutan FS.
        List<Integer> stationOrder = new ArrayList<>();

        for (int i = 0; i < stationCount; i++) {
            stationOrder.add(i);
        }

        Collections.shuffle(stationOrder, random);

        // Menandai posisi yang tetap ditempati.
        boolean[] occupied = new boolean[emptyCount];

        // FS yang tidak dipindahkan tetap menempati posisi.
        for (int i = moveCount; i < stationCount; i++) {

            int stationIndex = stationOrder.get(i);

            occupied[stations[stationIndex]] = true;
        }

        // Membuat daftar kandidat posisi baru.
        List<Integer> candidates = new ArrayList<>();

        for (int i = 0; i < emptyCount; i++) {

            if (!occupied[i]) {
                candidates.add(i);
            }
        }

        // Mengacak kandidat posisi.
        Collections.shuffle(candidates, random);

        // Memindahkan sejumlah FS.
        for (int i = 0; i < moveCount; i++) {

            int stationIndex = stationOrder.get(i);

            stations[stationIndex] = candidates.get(i);
        }

        // Mengembalikan kromosom valid.
        return Chromosome.fromStationIndices(
            stations,
            emptyCount,
            chromosome.getRepresentation()
        );
    }

    // Memastikan kromosom valid untuk mutation.
    private static void validate(Chromosome chromosome) {

        if (chromosome == null) {
            throw new IllegalArgumentException(
                "Chromosome tidak boleh null"
            );
        }

        if (chromosome.getStationCount() <= 0) {
            throw new IllegalArgumentException(
                "Jumlah FS harus lebih dari 0"
            );
        }

        if (chromosome.getStationCount()
                >= chromosome.getEmptyCount()) {

            throw new IllegalArgumentException(
                "Mutation membutuhkan sel kosong alternatif"
            );
        }
    }
}
