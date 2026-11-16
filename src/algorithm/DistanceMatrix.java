
package algorithm;

import java.util.List;

import model.Cell;
import model.MapData;

// Class untuk menyimpan matriks jarak rumah ke sel kosong.
public class DistanceMatrix {

    private int[][] distances;

    private List<Cell> emptyCells;

    // Constructor.
    public DistanceMatrix(
            MapData map,
            List<Cell> emptyCells) {

        this.emptyCells = emptyCells;

        int houseCount = map.getJumlahRumah();
        int emptyCount = emptyCells.size();

        distances = new int[houseCount][emptyCount];

        // Penalti jika rumah tidak dapat mencapai FS.
        int penalty = map.getBaris() * map.getKolom();

        // Menghitung BFS untuk setiap rumah.
        for (int i = 0; i < houseCount; i++) {

            Cell house = map.getRumah().get(i);

            // BFS dari rumah ke seluruh grid.
            int[][] bfsResult =
                BFS.calculateDistances(map, house);

            // Mengambil jarak ke setiap sel kosong.
            for (int j = 0; j < emptyCount; j++) {

                Cell empty = emptyCells.get(j);

                int distance =
                    bfsResult[empty.getX()][empty.getY()];

                // Jika tidak terjangkau, gunakan penalti.
                if (distance == -1) {
                    distance = penalty;
                }

                distances[i][j] = distance;
            }
        }
    }

    // Mengambil jarak rumah ke sel kosong tertentu.
    public int getDistance(int houseIndex, int emptyIndex) {
        return distances[houseIndex][emptyIndex];
    }

    // Mengambil seluruh matriks jarak.
    public int[][] getDistances() {
        return distances;
    }

    // Mengambil daftar sel kosong.
    public List<Cell> getEmptyCells() {
        return emptyCells;
    }

    // Mengambil jumlah rumah.
    public int getHouseCount() {
        return distances.length;
    }

    // Mengambil jumlah sel kosong.
    public int getEmptyCount() {
        return emptyCells.size();
    }
}
