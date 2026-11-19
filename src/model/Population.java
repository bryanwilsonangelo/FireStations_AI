package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import algorithm.DistanceMatrix;

// Class untuk menyimpan dan mengelola sekumpulan individu dalam populasi GA.
public class Population {

    // Menyimpan daftar seluruh individu dalam populasi.
    private final List<Individual> individuals;

    // Constructor untuk membuat populasi kosong.
    public Population() {

        // Menginisialisasi list individu.
        this.individuals = new ArrayList<>();
    }

    // Constructor untuk membuat populasi dari daftar individu yang sudah ada.
    public Population(List<Individual> individuals) {

        // Memastikan daftar individu tidak null.
        if (individuals == null) {
            throw new IllegalArgumentException(
                "Daftar individu tidak boleh null"
            );
        }

        // Menyimpan salinan daftar individu.
        this.individuals = new ArrayList<>(individuals);
    }

    // Membuat populasi awal secara acak.
    public static Population createRandom(
            int populationSize,
            int emptyCount,
            int stationCount,
            Chromosome.Representation representation,
            DistanceMatrix matrix,
            Random random) {

        // Memastikan ukuran populasi valid.
        if (populationSize <= 0) {
            throw new IllegalArgumentException(
                "Ukuran populasi harus lebih dari 0"
            );
        }

        // Memastikan parameter objek tidak null.
        if (representation == null ||
            matrix == null ||
            random == null) {

            throw new IllegalArgumentException(
                "Parameter pembuatan populasi tidak boleh null"
            );
        }

        // Membuat objek populasi baru.
        Population population = new Population();

        // Membuat individu acak sebanyak ukuran populasi.
        for (int i = 0; i < populationSize; i++) {

            // Membuat kromosom acak.
            Chromosome chromosome = Chromosome.createRandom(
                emptyCount,
                stationCount,
                representation,
                random
            );

            // Mengevaluasi kromosom menjadi individu.
            Individual individual = new Individual(
                chromosome,
                matrix
            );

            // Menambahkan individu ke dalam populasi.
            population.add(individual);
        }

        return population;
    }

    // Menambahkan satu individu ke dalam populasi.
    public void add(Individual individual) {

        // Memastikan individu tidak null.
        if (individual == null) {
            throw new IllegalArgumentException(
                "Individu tidak boleh null"
            );
        }

        // Memasukkan individu ke dalam list.
        individuals.add(individual);
    }

    // Menambahkan sekumpulan individu ke dalam populasi.
    public void addAll(List<Individual> newIndividuals) {

        // Memastikan daftar individu tidak null.
        if (newIndividuals == null) {
            throw new IllegalArgumentException(
                "Daftar individu tidak boleh null"
            );
        }

        // Memasukkan setiap individu melalui validasi add.
        for (Individual individual : newIndividuals) {
            add(individual);
        }
    }

    // Mengambil individu berdasarkan indeks.
    public Individual get(int index) {
        return individuals.get(index);
    }

    // Mengambil jumlah individu dalam populasi.
    public int size() {
        return individuals.size();
    }

    // Memeriksa apakah populasi kosong.
    public boolean isEmpty() {
        return individuals.isEmpty();
    }

    // Mengambil daftar seluruh individu (read-only).
    public List<Individual> getIndividuals() {
        return Collections.unmodifiableList(individuals);
    }

    // Mengosongkan isi populasi.
    public void clear() {
        individuals.clear();
    }

    // Mengambil individu terbaik (fitness tertinggi / cost terendah).
    public Individual getBestIndividual() {

        // Memastikan populasi tidak kosong.
        if (individuals.isEmpty()) {
            throw new IllegalStateException(
                "Populasi kosong"
            );
        }

        // Menginisialisasi individu terbaik dengan elemen pertama.
        Individual best = individuals.get(0);

        // Mencari individu dengan nilai fitness tertinggi.
        for (int i = 1; i < individuals.size(); i++) {

            Individual current = individuals.get(i);

            if (current.getFitness() > best.getFitness()) {
                best = current;
            }
        }

        return best;
    }

    // Mengambil individu terburuk (fitness terendah / cost tertinggi).
    public Individual getWorstIndividual() {

        // Memastikan populasi tidak kosong.
        if (individuals.isEmpty()) {
            throw new IllegalStateException(
                "Populasi kosong"
            );
        }

        // Menginisialisasi individu terburuk dengan elemen pertama.
        Individual worst = individuals.get(0);

        // Mencari individu dengan nilai fitness terendah.
        for (int i = 1; i < individuals.size(); i++) {

            Individual current = individuals.get(i);

            if (current.getFitness() < worst.getFitness()) {
                worst = current;
            }
        }

        return worst;
    }

    // Mengambil nilai cost terbaik dalam populasi.
    public double getBestCost() {
        return getBestIndividual().getCost();
    }

    // Mengambil nilai fitness terbaik dalam populasi.
    public double getBestFitness() {
        return getBestIndividual().getFitness();
    }

    // Menghitung rata-rata cost seluruh individu dalam populasi.
    public double getAverageCost() {

        // Memastikan populasi tidak kosong.
        if (individuals.isEmpty()) {
            throw new IllegalStateException(
                "Populasi kosong"
            );
        }

        double totalCost = 0.0;

        // Menjumlahkan cost seluruh individu.
        for (Individual individual : individuals) {
            totalCost += individual.getCost();
        }

        // Mengembalikan rata-rata cost.
        return totalCost / individuals.size();
    }

    // Menghitung rata-rata fitness seluruh individu dalam populasi.
    public double getAverageFitness() {

        // Memastikan populasi tidak kosong.
        if (individuals.isEmpty()) {
            throw new IllegalStateException(
                "Populasi kosong"
            );
        }

        double totalFitness = 0.0;

        // Menjumlahkan fitness seluruh individu.
        for (Individual individual : individuals) {
            totalFitness += individual.getFitness();
        }

        // Mengembalikan rata-rata fitness.
        return totalFitness / individuals.size();
    }

    // Mengambil daftar individu yang sudah diurutkan dari fitness tertinggi ke terendah.
    public List<Individual> getSortedByFitness() {

        // Membuat salinan list agar urutan asli tidak berubah.
        List<Individual> sorted = new ArrayList<>(individuals);

        // Mengurutkan berdasarkan fitness secara descending.
        sorted.sort(
            Comparator.comparingDouble(
                Individual::getFitness
            ).reversed()
        );

        return sorted;
    }

    // Mengambil sejumlah individu terbaik untuk proses elitism.
    public List<Individual> getTopIndividuals(int count) {

        // Memastikan jumlah elit valid.
        if (count < 0 || count > individuals.size()) {
            throw new IllegalArgumentException(
                "Jumlah individu elit tidak valid"
            );
        }

        // Jika jumlah elit 0, kembalikan list kosong.
        if (count == 0) {
            return new ArrayList<>();
        }

        // Mengurutkan populasi berdasarkan fitness terbaik.
        List<Individual> sorted = getSortedByFitness();

        // Mengambil sebanyak count individu teratas.
        return new ArrayList<>(
            sorted.subList(0, count)
        );
    }

    // Menampilkan ringkasan informasi populasi.
    @Override
    public String toString() {

        if (individuals.isEmpty()) {
            return "Population {size=0}";
        }

        return "Population {" +
            "size=" + individuals.size() +
            ", bestCost=" + String.format("%.5f", getBestCost()) +
            ", avgCost=" + String.format("%.5f", getAverageCost()) +
            ", bestFitness=" + String.format("%.5f", getBestFitness()) +
            '}';
    }
}
