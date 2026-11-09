package model;

import java.util.ArrayList;
import java.util.List;

// Class untuk menyimpan seluruh informasi peta.
public class MapData {

    private int baris;
    private int kolom;
    private int p;

    private char[][] grid;

    private List<Cell> rumah;
    private List<Cell> pohon;
    private List<Cell> kosong;

    // Constructor.
    public MapData(int baris, int kolom, int p) {

        this.baris = baris;
        this.kolom = kolom;
        this.p = p;

        grid = new char[baris][kolom];

        rumah = new ArrayList<>();
        pohon = new ArrayList<>();
        kosong = new ArrayList<>();
    }

    // Mengambil jumlah baris.
    public int getBaris() {
        return baris;
    }

    // Mengambil jumlah kolom.
    public int getKolom() {
        return kolom;
    }

    // Mengambil jumlah fire station.
    public int getP() {
        return p;
    }

    // Mengambil grid.
    public char[][] getGrid() {
        return grid;
    }

    // Mengambil daftar rumah.
    public List<Cell> getRumah() {
        return rumah;
    }

    // Mengambil daftar pohon.
    public List<Cell> getPohon() {
        return pohon;
    }

    // Mengambil daftar sel kosong.
    public List<Cell> getKosong() {
        return kosong;
    }

    // Mengisi karakter pada grid dan mengelompokkan sel.
    public void setCell(int x, int y, char value) {

        grid[x][y] = value;

        Cell cell = new Cell(x, y);

        if (value == 'H') {
            rumah.add(cell);
        }
        else if (value == 'T') {
            pohon.add(cell);
        }
        else if (value == '.') {
            kosong.add(cell);
        }
    }

    // Mengambil karakter berdasarkan koordinat.
    public char getCell(int x, int y) {
        return grid[x][y];
    }

    // Menghitung jumlah sel.
    public int getJumlahSelKosong() {
        return kosong.size();
    }

    public int getJumlahRumah() {
        return rumah.size();
    }

    public int getJumlahPohon() {
        return pohon.size();
    }
}