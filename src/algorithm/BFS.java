
package algorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import model.Cell;
import model.MapData;

// Class untuk melakukan Breadth First Search.
public class BFS {

    // Arah pergerakan: atas, bawah, kiri, kanan.
    private static final int[] DX = {-1, 1, 0, 0};
    private static final int[] DY = {0, 0, -1, 1};

    // Menghitung jarak dari satu rumah ke seluruh grid.
    public static int[][] calculateDistances(
            MapData map, Cell start) {

        int rows = map.getBaris();
        int cols = map.getKolom();

        // Matriks jarak.
        int[][] distance = new int[rows][cols];

        // Semua jarak awalnya -1 (belum dikunjungi).
        for (int i = 0; i < rows; i++) {
            Arrays.fill(distance[i], -1);
        }

        // Queue untuk BFS.
        Queue<Cell> queue = new LinkedList<>();

        // Memasukkan rumah sebagai titik awal.
        queue.add(start);

        distance[start.getX()][start.getY()] = 0;

        // Proses BFS.
        while (!queue.isEmpty()) {

            Cell current = queue.poll();

            int x = current.getX();
            int y = current.getY();

            // Memeriksa empat arah.
            for (int i = 0; i < 4; i++) {

                int nextX = x + DX[i];
                int nextY = y + DY[i];

                // Memastikan posisi masih di dalam grid.
                if (nextX < 0 || nextX >= rows ||
                    nextY < 0 || nextY >= cols) {
                    continue;
                }

                // Hanya sel kosong yang dapat dilewati.
                if (map.getCell(nextX, nextY) != '.') {
                    continue;
                }

                // Memastikan sel belum pernah dikunjungi.
                if (distance[nextX][nextY] != -1) {
                    continue;
                }

                // Menyimpan jarak.
                distance[nextX][nextY] =
                    distance[x][y] + 1;

                // Memasukkan sel ke queue.
                queue.add(new Cell(nextX, nextY));
            }
        }

        return distance;
    }
}
