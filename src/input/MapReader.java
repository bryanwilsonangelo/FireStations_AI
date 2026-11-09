package input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import model.MapData;

// Class untuk membaca file peta TXT.
public class MapReader {

    public static MapData readMap(String filename)
            throws IOException {

        // Membuka file.
        try (BufferedReader br =
                new BufferedReader(new FileReader(filename))) {

            // Membaca header.
            String header = br.readLine();

            if (header == null) {
                throw new IOException("File peta kosong");
            }

            // Memisahkan m, n, dan p.
            String[] data = header.trim().split("\\s+");

            if (data.length != 3) {
                throw new IOException(
                    "Format header harus: m n p"
                );
            }

            int baris;
            int kolom;
            int p;

            try {
                baris = Integer.parseInt(data[0]);
                kolom = Integer.parseInt(data[1]);
                p = Integer.parseInt(data[2]);
            }
            catch (NumberFormatException e) {
                throw new IOException(
                    "Header harus berisi tiga bilangan integer"
                );
            }

            // Validasi ukuran peta.
            if (baris <= 0 || kolom <= 0 || p <= 0) {
                throw new IOException(
                    "Ukuran peta dan jumlah FS harus positif"
                );
            }

            // Membuat objek peta.
            MapData map = new MapData(baris, kolom, p);

            // Membaca setiap baris peta.
            for (int i = 0; i < baris; i++) {

                String line = br.readLine();

                if (line == null) {
                    throw new IOException(
                        "Jumlah baris peta kurang dari " + baris
                    );
                }

                if (line.length() != kolom) {
                    throw new IOException(
                        "Panjang baris " + (i + 1) +
                        " tidak sesuai dengan kolom " + kolom
                    );
                }

                // Membaca setiap karakter.
                for (int j = 0; j < kolom; j++) {

                    char value = line.charAt(j);

                    // Validasi karakter.
                    if (value != 'H' &&
                        value != 'T' &&
                        value != '.') {

                        throw new IOException(
                            "Karakter tidak valid pada (" +
                            (i + 1) + "," + (j + 1) + ")"
                        );
                    }

                    // Menyimpan karakter.
                    map.setCell(i, j, value);
                }
            }

            // Memastikan tidak ada baris tambahan.
            if (br.readLine() != null) {
                throw new IOException(
                    "Terdapat baris tambahan setelah peta"
                );
            }

            // Memastikan jumlah FS tidak melebihi sel kosong.
            if (p > map.getJumlahSelKosong()) {
                throw new IOException(
                    "Jumlah FS melebihi jumlah sel kosong"
                );
            }

            return map;
        }
    }
}