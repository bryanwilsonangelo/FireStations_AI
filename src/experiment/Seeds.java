package experiment;

import java.util.Random;

// Membuat 30 seed tetap sesuai spesifikasi, agar eksperimen dapat diulang.
public class Seeds {

    // Seed utama dari spesifikasi (NPM ketua).
    public static final int NPM_KETUA = 25099;

    // Jumlah seed (= jumlah percobaan per konfigurasi).
    public static final int JUMLAH = 30;

    // Mengembalikan array 30 seed yang selalu sama.
    public static int[] generate() {
        Random rand = new Random(NPM_KETUA);
        int[] seed = new int[JUMLAH];
        int batas = 1 << 16;
        for (int i = 0; i < JUMLAH; i++) {
            seed[i] = rand.nextInt(batas + 1);
        }
        return seed;
    }
}
