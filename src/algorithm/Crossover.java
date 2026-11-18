
package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import model.Chromosome;

// Class crossover untuk menghasilkan offspring.
public class Crossover {

    // One-Point Crossover.
    public static Chromosome onePoint(
            Chromosome parent1,
            Chromosome parent2,
            Random random) {

        checkParents(parent1, parent2);

        int emptyCount = parent1.getEmptyCount();
        int stationCount = parent1.getStationCount();

        int[] childGenes;

        // Binary: crossover pada bit-string.
        if (parent1.getRepresentation()
                == Chromosome.Representation.BINARY) {

            int[] genes1 = parent1.getGenes();
            int[] genes2 = parent2.getGenes();

            int point = random.nextInt(genes1.length + 1);

            childGenes = new int[genes1.length];

            for (int i = 0; i < genes1.length; i++) {

                if (i < point) {
                    childGenes[i] = genes1[i];
                } else {
                    childGenes[i] = genes2[i];
                }
            }

            // Repair agar jumlah FS tetap stationCount.
            childGenes = repairBinary(
                childGenes,
                stationCount,
                random
            );

            int[] stationIndices = binaryToIndices(childGenes);

            return Chromosome.fromStationIndices(
                stationIndices,
                emptyCount,
                parent1.getRepresentation()
            );
        }

        // Integer: crossover pada array posisi FS.
        else {

            int[] genes1 = parent1.getGenes();
            int[] genes2 = parent2.getGenes();

            int point = random.nextInt(genes1.length + 1);

            List<Integer> preferred = new ArrayList<>();

            // Mengambil bagian depan Parent 1.
            for (int i = 0; i < point; i++) {
                preferred.add(genes1[i]);
            }

            // Mengambil bagian belakang Parent 2.
            for (int i = point; i < genes2.length; i++) {
                preferred.add(genes2[i]);
            }

            int[] repaired = repairIndices(
                preferred,
                parent1,
                parent2,
                stationCount,
                emptyCount,
                random
            );

            return Chromosome.fromStationIndices(
                repaired,
                emptyCount,
                parent1.getRepresentation()
            );
        }
    }

    // Uniform / Set-Based Crossover.
    public static Chromosome uniform(
            Chromosome parent1,
            Chromosome parent2,
            Random random) {

        checkParents(parent1, parent2);

        int emptyCount = parent1.getEmptyCount();
        int stationCount = parent1.getStationCount();

        int[] genes1 = parent1.getGenes();
        int[] genes2 = parent2.getGenes();

        // Binary uniform crossover.
        if (parent1.getRepresentation()
                == Chromosome.Representation.BINARY) {

            int[] child = new int[genes1.length];

            for (int i = 0; i < child.length; i++) {

                // Memilih gen dari salah satu parent.
                child[i] = random.nextBoolean()
                    ? genes1[i]
                    : genes2[i];
            }

            // Repair jumlah bit 1.
            child = repairBinary(
                child,
                stationCount,
                random
            );

            return Chromosome.fromStationIndices(
                binaryToIndices(child),
                emptyCount,
                parent1.getRepresentation()
            );
        }

        // Integer uniform / set-based crossover.
        else {

            int[] indices1 = parent1.getStationIndices();
            int[] indices2 = parent2.getStationIndices();

            Set<Integer> selected = new LinkedHashSet<>();

            // Memilih posisi dari kedua parent secara acak.
            for (int index : indices1) {
                if (random.nextBoolean()) {
                    selected.add(index);
                }
            }

            for (int index : indices2) {
                if (random.nextBoolean()) {
                    selected.add(index);
                }
            }

            // Jika kandidat kurang dari p, gabungkan parent.
            List<Integer> preferred =
                new ArrayList<>(selected);

            for (int index : indices1) {
                if (!selected.contains(index)) {
                    preferred.add(index);
                }
            }

            for (int index : indices2) {
                if (!selected.contains(index)) {
                    preferred.add(index);
                }
            }

            int[] repaired = repairIndices(
                preferred,
                parent1,
                parent2,
                stationCount,
                emptyCount,
                random
            );

            return Chromosome.fromStationIndices(
                repaired,
                emptyCount,
                parent1.getRepresentation()
            );
        }
    }

    // Repair binary agar jumlah gen 1 tepat p.
    private static int[] repairBinary(
            int[] genes,
            int stationCount,
            Random random) {

        List<Integer> ones = new ArrayList<>();
        List<Integer> zeros = new ArrayList<>();

        for (int i = 0; i < genes.length; i++) {

            if (genes[i] == 1) {
                ones.add(i);
            } else {
                zeros.add(i);
            }
        }

        Collections.shuffle(ones, random);
        Collections.shuffle(zeros, random);

        // Jika jumlah 1 terlalu banyak, ubah menjadi 0.
        while (ones.size() > stationCount) {

            int index = ones.remove(ones.size() - 1);

            genes[index] = 0;

            zeros.add(index);
        }

        // Jika jumlah 1 terlalu sedikit, ubah 0 menjadi 1.
        while (ones.size() < stationCount) {

            int index = zeros.remove(zeros.size() - 1);

            genes[index] = 1;

            ones.add(index);
        }

        return genes;
    }

    // Repair integer agar indeks FS valid dan tidak duplikat.
    private static int[] repairIndices(
            List<Integer> preferred,
            Chromosome parent1,
            Chromosome parent2,
            int stationCount,
            int emptyCount,
            Random random) {

        Set<Integer> selected = new LinkedHashSet<>();

        // Mengambil kandidat yang valid dari hasil crossover.
        for (int index : preferred) {

            if (index >= 0 &&
                index < emptyCount &&
                selected.add(index) &&
                selected.size() == stationCount) {

                break;
            }
        }

        // Mengambil kandidat dari parent jika masih kurang.
        for (int index : parent1.getStationIndices()) {

            if (selected.size() >= stationCount) {
                break;
            }

            selected.add(index);
        }

        for (int index : parent2.getStationIndices()) {

            if (selected.size() >= stationCount) {
                break;
            }

            selected.add(index);
        }

        // Menambahkan posisi acak jika masih kurang.
        while (selected.size() < stationCount) {

            int index = random.nextInt(emptyCount);

            selected.add(index);
        }

        // Mengubah hasil menjadi array.
        int[] result = new int[stationCount];

        int i = 0;

        for (int index : selected) {

            if (i >= stationCount) {
                break;
            }

            result[i++] = index;
        }

        return result;
    }

    // Mengubah binary menjadi daftar indeks FS.
    private static int[] binaryToIndices(int[] genes) {

        int count = 0;

        for (int gene : genes) {
            if (gene == 1) {
                count++;
            }
        }

        int[] indices = new int[count];

        int j = 0;

        for (int i = 0; i < genes.length; i++) {

            if (genes[i] == 1) {
                indices[j++] = i;
            }
        }

        return indices;
    }

    // Memastikan kedua parent kompatibel.
    private static void checkParents(
            Chromosome parent1,
            Chromosome parent2) {

        if (parent1 == null || parent2 == null) {
            throw new IllegalArgumentException(
                "Parent tidak boleh null"
            );
        }

        if (parent1.getRepresentation()
                != parent2.getRepresentation()) {

            throw new IllegalArgumentException(
                "Representasi parent harus sama"
            );
        }

        if (parent1.getEmptyCount()
                != parent2.getEmptyCount() ||
            parent1.getStationCount()
                != parent2.getStationCount()) {

            throw new IllegalArgumentException(
                "Konfigurasi parent harus sama"
            );
        }
    }
}
