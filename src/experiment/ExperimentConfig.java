package experiment;

// Parameter tetap untuk eksperimen, sesuai spesifikasi bagian 4.
public class ExperimentConfig {

    // Hyperparameter tahap 1.
    public static final int POP = 100;
    public static final double PC = 0.8;
    public static final double PM = 0.05;
    public static final double ELIT = 0.2;

    // Kandidat hyperparameter tahap 2 (masing-masing 5 nilai di luar nilai tahap 1).
    public static final int[] KANDIDAT_POP     = {50, 150, 250, 500, 1000};
    public static final double[] KANDIDAT_PC   = {0.5, 0.6, 0.7, 0.9, 0.95};
    public static final double[] KANDIDAT_PM   = {0.01, 0.02, 0.1, 0.15, 0.2};
    public static final double[] KANDIDAT_ELIT = {0.05, 0.1, 0.15, 0.25, 0.3};

    // Ukuran tournament jika memakai tournament selection.
    public static final int TOURNAMENT_SIZE = 3;

    // Stagnasi dan waktu dinonaktifkan (0) karena spesifikasi tidak
    // menyebut batasnya. Ubah jika sudah diputuskan.
    public static final int STAGNASI = 0;
    public static final long WAKTU_MS = 0;

    // Jumlah percobaan per konfigurasi.
    public static final int JUMLAH_SEED = 30;
    public static final int JUMLAH_SEED_TAHAP2A = 10;

    // Evaluation budget per ukuran grid (Tabel 1).
    public static long budgetUntuk(String ukuran) {
        switch (ukuran) {
            case "tiny":   return 10000L;
            case "small":  return 50000L;
            case "normal": return 200000L;
            case "large":  return 500000L;
            default:
                throw new IllegalArgumentException("Ukuran tidak dikenal: " + ukuran);
        }
    }
}