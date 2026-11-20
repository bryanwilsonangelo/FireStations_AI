package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// Merangkum hasil tahap 1: rata-rata cost tiap model pada tiap peta.
// Tidak memilih model. Keputusan model terbaik ada di tangan kelompok.
//
// Pemakaian (dari folder proyek):
//   java -cp out experiment.PilihModel [file_csv]
// Output:
//   results/rata_cost_tahap1.csv (model, peta, jumlah_seed, rata_cost)
//   tabel ringkas di layar
public class PilihModel {

    public static void main(String[] args) throws IOException {

        String fileIn = args.length > 0 ? args[0] : "results/tahap1.csv";
        String fileOut = "results/rata_cost_tahap1.csv";

        // model -> peta -> daftar cost (satu per seed)
        Map<String, Map<String, List<Double>>> data = new TreeMap<>();
        Set<String> semuaPeta = new TreeSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileIn), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) {
                throw new IOException("File kosong: " + fileIn);
            }
            String[] h = header.split(",");
            int iPeta = kolom(h, "peta");
            int iRepr = kolom(h, "representasi");
            int iSel = kolom(h, "seleksi");
            int iCx = kolom(h, "crossover");
            int iMut = kolom(h, "mutasi");
            int iCost = kolom(h, "cost");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] k = line.split(",");
                String model = k[iRepr] + "/" + k[iSel] + "/" + k[iCx] + "/" + k[iMut];
                data.computeIfAbsent(model, x -> new TreeMap<>())
                    .computeIfAbsent(k[iPeta], x -> new ArrayList<>())
                    .add(Double.parseDouble(k[iCost]));
                semuaPeta.add(k[iPeta]);
            }
        }

        if (data.isEmpty()) {
            throw new IOException("Tidak ada data di " + fileIn);
        }

        // Tulis rata-rata per model per peta ke CSV.
        File f = new File(fileOut);
        if (f.getAbsoluteFile().getParentFile() != null) {
            f.getAbsoluteFile().getParentFile().mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(f), StandardCharsets.UTF_8))) {
            pw.println("model,peta,jumlah_seed,rata_cost");
            for (Map.Entry<String, Map<String, List<Double>>> m : data.entrySet()) {
                for (Map.Entry<String, List<Double>> p : m.getValue().entrySet()) {
                    List<Double> c = p.getValue();
                    pw.println(String.format(Locale.US, "%s,%s,%d,%.5f",
                        m.getKey(), p.getKey(), c.size(), rata(c)));
                }
            }
        }

        // Tampilkan tabel ringkas: baris = model, kolom = peta.
        System.out.println("Rata-rata cost per model per peta (lebih kecil lebih baik)");
        System.out.println("Disimpan ke: " + fileOut);
        System.out.println();
        System.out.printf(Locale.US, "%-40s", "Model");
        for (String p : semuaPeta) {
            System.out.printf(Locale.US, " %-28s", singkat(p));
        }
        System.out.println();

        for (Map.Entry<String, Map<String, List<Double>>> m : data.entrySet()) {
            System.out.printf(Locale.US, "%-40s", m.getKey());
            for (String p : semuaPeta) {
                List<Double> c = m.getValue().get(p);
                String isi = c == null ? "-" : String.format(Locale.US, "%.5f (n=%d)", rata(c), c.size());
                System.out.printf(Locale.US, " %-28s", isi);
            }
            System.out.println();
        }
    }

    // Nama peta dipendekkan agar tabel tidak terlalu lebar.
    static String singkat(String path) {
        String nama = new File(path).getName();
        return nama.endsWith(".txt") ? nama.substring(0, nama.length() - 4) : nama;
    }

    static double rata(List<Double> nilai) {
        double total = 0;
        for (double v : nilai) total += v;
        return total / nilai.size();
    }

    static int kolom(String[] header, String nama) throws IOException {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equals(nama)) return i;
        }
        throw new IOException("Kolom '" + nama + "' tidak ditemukan di CSV");
    }
}
