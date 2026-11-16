
package model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class Chromosome {

    // Jenis representasi kromosom.
    public enum Representation {
        BINARY,
        INTEGER
    }

    // Menyimpan gen kromosom.
    private final int[] genes;

    // Jenis representasi.
    private final Representation representation;

    // Jumlah seluruh sel kosong.
    private final int emptyCount;

    // Jumlah Fire Station.
    private final int stationCount;

    // Constructor private agar pembuatan kromosom
    // selalu melalui proses validasi.
    private Chromosome(
            int[] genes,
            Representation representation,
            int emptyCount,
            int stationCount) {

        this.genes = genes.clone();

        this.representation = representation;

        this.emptyCount = emptyCount;

        this.stationCount = stationCount;

        validate();
    }

    // Membuat kromosom acak.
    public static Chromosome createRandom(
            int emptyCount,
            int stationCount,
            Representation representation,
            Random random) {

        if (emptyCount <= 0 ||
            stationCount <= 0 ||
            stationCount > emptyCount) {

            throw new IllegalArgumentException(
                "Jumlah sel kosong atau FS tidak valid"
            );
        }

        // Membuat daftar seluruh indeks sel kosong.
        int[] candidates = new int[emptyCount];

        for (int i = 0; i < emptyCount; i++) {
            candidates[i] = i;
        }

        // Mengambil indeks secara acak tanpa duplikasi.
        for (int i = 0; i < stationCount; i++) {

            int randomIndex =
                i + random.nextInt(emptyCount - i);

            int temp = candidates[i];

            candidates[i] = candidates[randomIndex];

            candidates[randomIndex] = temp;
        }

        // Membuat kromosom berdasarkan representasi.
        if (representation == Representation.BINARY) {

            int[] genes = new int[emptyCount];

            // Menandai sel yang memiliki FS.
            for (int i = 0; i < stationCount; i++) {
                genes[candidates[i]] = 1;
            }

            return new Chromosome(
                genes,
                representation,
                emptyCount,
                stationCount
            );
        }

        else {

            // Integer hanya menyimpan indeks FS.
            int[] genes = new int[stationCount];

            for (int i = 0; i < stationCount; i++) {
                genes[i] = candidates[i];
            }

            return new Chromosome(
                genes,
                representation,
                emptyCount,
                stationCount
            );
        }
    }

    // Membuat kromosom dari indeks posisi FS.
    public static Chromosome fromStationIndices(
            int[] stationIndices,
            int emptyCount,
            Representation representation) {

        if (stationIndices == null) {
            throw new IllegalArgumentException(
                "Indeks FS tidak boleh null"
            );
        }

        int stationCount = stationIndices.length;

        if (representation == Representation.BINARY) {

            int[] genes = new int[emptyCount];

            for (int index : stationIndices) {

                if (index < 0 || index >= emptyCount) {
                    throw new IllegalArgumentException(
                        "Indeks FS tidak valid"
                    );
                }

                if (genes[index] == 1) {
                    throw new IllegalArgumentException(
                        "Posisi FS duplikat"
                    );
                }

                genes[index] = 1;
            }

            return new Chromosome(
                genes,
                representation,
                emptyCount,
                stationCount
            );
        }

        else {

            return new Chromosome(
                stationIndices,
                representation,
                emptyCount,
                stationCount
            );
        }
    }

    // Mengubah kromosom menjadi daftar indeks posisi FS.
    public int[] getStationIndices() {

        int[] result = new int[stationCount];

        if (representation == Representation.BINARY) {

            int index = 0;

            for (int i = 0; i < genes.length; i++) {

                if (genes[i] == 1) {
                    result[index++] = i;
                }
            }
        }

        else {

            result = genes.clone();
        }

        return result;
    }

    // Memvalidasi isi kromosom.
    private void validate() {

        if (emptyCount <= 0 ||
            stationCount <= 0 ||
            stationCount > emptyCount) {

            throw new IllegalArgumentException(
                "Konfigurasi kromosom tidak valid"
            );
        }

        if (representation == Representation.BINARY) {

            if (genes.length != emptyCount) {
                throw new IllegalArgumentException(
                    "Panjang kromosom binary tidak sesuai"
                );
            }

            int count = 0;

            for (int gene : genes) {

                if (gene != 0 && gene != 1) {
                    throw new IllegalArgumentException(
                        "Gen binary hanya boleh 0 atau 1"
                    );
                }

                if (gene == 1) {
                    count++;
                }
            }

            if (count != stationCount) {
                throw new IllegalArgumentException(
                    "Jumlah FS pada binary tidak sesuai"
                );
            }
        }

        else {

            if (genes.length != stationCount) {
                throw new IllegalArgumentException(
                    "Jumlah gen integer tidak sesuai"
                );
            }

            Set<Integer> used = new HashSet<>();

            for (int gene : genes) {

                if (gene < 0 || gene >= emptyCount) {
                    throw new IllegalArgumentException(
                        "Indeks sel kosong tidak valid"
                    );
                }

                if (!used.add(gene)) {
                    throw new IllegalArgumentException(
                        "Terdapat posisi FS duplikat"
                    );
                }
            }
        }
    }

    // Mengambil salinan gen.
    public int[] getGenes() {
        return genes.clone();
    }

    // Mengambil jenis representasi.
    public Representation getRepresentation() {
        return representation;
    }

    // Mengambil jumlah sel kosong.
    public int getEmptyCount() {
        return emptyCount;
    }

    // Mengambil jumlah FS.
    public int getStationCount() {
        return stationCount;
    }

    // Mengambil panjang kromosom.
    public int getLength() {
        return genes.length;
    }

    // Menampilkan isi kromosom.
    @Override
    public String toString() {

        StringBuilder result = new StringBuilder("[");

        for (int i = 0; i < genes.length; i++) {

            result.append(genes[i]);

            if (i < genes.length - 1) {
                result.append(", ");
            }
        }

        result.append("]");

        return result.toString();
    }
}
