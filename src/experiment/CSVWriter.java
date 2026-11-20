package experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

// Menulis hasil eksperimen ke file CSV.
// Setiap baris langsung di-flush agar data tidak hilang jika program berhenti.
public class CSVWriter implements AutoCloseable {

    // Kolom-kolom CSV. Setiap baris memuat konfigurasi lengkap agar percobaan bisa
    // dilacak.
    public static final String[] HEADER = {
            "tahap", "ukuran", "peta", "representasi", "seleksi", "crossover", "mutasi",
            "pop", "budget", "pc", "pm", "elit", "seed_index", "seed",
            "cost", "fitness", "evaluasi", "generasi", "waktu_ms", "p"
    };

    private final BufferedWriter writer;

    // Membuka file (membuat folder jika belum ada) dan menulis header.
    public CSVWriter(String path) throws IOException {
        File file = new File(path);
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8));
        tulis(HEADER);
        if (file.exists()) {
            throw new IOException("File sudah ada: " + path +
                    ". Hapus atau ganti nama dulu.");
        }
    }

    // Menulis satu baris. Setiap kolom di-escape bila mengandung koma atau tanda
    // kutip.
    public void tulis(String... kolom) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kolom.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(escape(kolom[i]));
        }
        sb.append('\n');
        writer.write(sb.toString());
        writer.flush();
    }

    private static String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
