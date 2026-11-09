import input.MapReader;
import model.MapData;

public class Main {

    public static void main(String[] args) {

        try {

            // Membaca peta.
            MapData map = MapReader.readMap(
                "maps/n20_klaster_04.txt"
            );

            // Menampilkan informasi peta.
            System.out.println("Ukuran peta: "
                + map.getBaris() + " x "
                + map.getKolom());

            System.out.println("Jumlah FS: "
                + map.getP());

            System.out.println("Jumlah rumah: "
                + map.getJumlahRumah());

            System.out.println("Jumlah pohon: "
                + map.getJumlahPohon());

            System.out.println("Jumlah kosong: "
                + map.getJumlahSelKosong());

        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}