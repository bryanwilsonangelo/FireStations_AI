package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// Memilih model terbaik tahap 1 berdasarkan kriteria yang dipilih, lalu menyimpannya ke CSV.
//
// Kriteria (ditentukan kelompok, dipilih lewat argumen):
//   rasio : rata-rata rasio cost terhadap model terbaik di tiap peta (default)
//   rata  : rata-rata biasa dari rata_cost di semua peta
//
// Pemakaian (dari folder proyek):
//   java -cp out experiment.KeputusanTahap1 [file_input] [kriteria] [jumlah_pilihan]
// Contoh:
//   java -cp out experiment.KeputusanTahap1 results/rata_cost_tahap1.csv rasio 3
//
// Output: results/keputusan_tahap1.csv
public class KeputusanTahap1 {

    static class Hasil {
        String model;
        double skor;
        double rataCost;
        int petaTerbaik;
        int jumlahPeta;
    }

    public static void main(String[] args) throws IOException {

        String fileIn = args.length > 0 ? args[0] : "results/rata_cost_tahap1.csv";
        String kriteria = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "rasio";
        int jumlahPilihan = args.length > 2 ? Integer.parseInt(args[2]) : 3;
        String fileOut = "results/keputusan_tahap1.csv";

        if (!kriteria.equals("rasio") && !kriteria.equals("rata")) {
            throw new IllegalArgumentException("kriteria harus 'rasio' atau 'rata'");
        }

        // Membaca: model -> peta -> rata_cost
        Map<String, Map<String, Double>> cost = new TreeMap<>();
        Set<String> semuaPeta = new TreeSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileIn), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) throw new IOException("File kosong: " + fileIn);
            String[] h = header.split(",");
            int iModel = kolom(h, "model");
            int iPeta = kolom(h, "peta");
            int iCost = kolom(h, "rata_cost");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] k = line.trim().split(",");
                cost.computeIfAbsent(k[iModel], x -> new TreeMap<>())
                    .put(k[iPeta], Double.parseDouble(k[iCost]));
                semuaPeta.add(k[iPeta]);
            }
        }

        if (cost.isEmpty()) throw new IOException("Tidak ada data di " + fileIn);

        // Cost terbaik di tiap peta.
        Map<String, Double> terbaik = new HashMap<>();
        for (String peta : semuaPeta) {
            double min = Double.MAX_VALUE;
            for (Map<String, Double> per : cost.values()) {
                Double v = per.get(peta);
                if (v != null && v < min) min = v;
            }
            terbaik.put(peta, min);
        }

        // Hitung skor tiap model.
        List<Hasil> daftar = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> e : cost.entrySet()) {
            Hasil hs = new Hasil();
            hs.model = e.getKey();
            hs.jumlahPeta = e.getValue().size();

            double totalRasio = 0, totalCost = 0;
            int terbaikDi = 0;
            for (Map.Entry<String, Double> p : e.getValue().entrySet()) {
                double c = p.getValue();
                double best = terbaik.get(p.getKey());
                totalCost += c;
                totalRasio += best > 0 ? c / best : 1.0;
                if (Math.abs(c - best) < 1e-9) terbaikDi++;
            }
            hs.rataCost = totalCost / hs.jumlahPeta;
            hs.skor = kriteria.equals("rasio") ? totalRasio / hs.jumlahPeta : hs.rataCost;
            hs.petaTerbaik = terbaikDi;
            daftar.add(hs);
        }

        // Urutkan: skor kecil lebih dulu.
        daftar.sort(Comparator.comparingDouble(h -> h.skor));

        // Tulis CSV.
        File f = new File(fileOut);
        if (f.getAbsoluteFile().getParentFile() != null) {
            f.getAbsoluteFile().getParentFile().mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(f), StandardCharsets.UTF_8))) {

            pw.println("peringkat,model,kriteria,skor,rata_cost,peta_terbaik,jumlah_peta,dipilih");
            for (int i = 0; i < daftar.size(); i++) {
                Hasil hs = daftar.get(i);
                boolean dipilih = i < jumlahPilihan;
                pw.println(String.format(Locale.US, "%d,%s,%s,%.6f,%.5f,%d,%d,%s",
                    i + 1, hs.model, kriteria, hs.skor, hs.rataCost,
                    hs.petaTerbaik, hs.jumlahPeta, dipilih ? "ya" : "tidak"));
            }
        }

        // Ringkasan di layar.
        System.out.println("Kriteria: " + kriteria + " (skor kecil = baik)");
        System.out.println("Jumlah pilihan: " + jumlahPilihan);
        System.out.println();
        for (int i = 0; i < Math.min(jumlahPilihan, daftar.size()); i++) {
            Hasil hs = daftar.get(i);
            System.out.printf(Locale.US, "%d. %s  skor=%.6f  rata_cost=%.5f%n",
                i + 1, hs.model, hs.skor, hs.rataCost);
        }
        System.out.println();
        System.out.println("Disimpan ke: " + fileOut);
    }

    static int kolom(String[] header, String nama) throws IOException {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equals(nama)) return i;
        }
        throw new IOException("Kolom '" + nama + "' tidak ditemukan");
    }
}
