package model;

// Class untuk menyimpan koordinat sebuah sel.
public class Cell {

    private int x;
    private int y;

    // Constructor koordinat.
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Mengambil koordinat baris.
    public int getX() {
        return x;
    }

    // Mengambil koordinat kolom.
    public int getY() {
        return y;
    }

    // Membandingkan dua koordinat.
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof Cell)) return false;

        Cell other = (Cell) obj;

        return x == other.x && y == other.y;
    }

    // Membuat hash berdasarkan koordinat.
    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    // Menampilkan koordinat.
    @Override
    public String toString() {
        return "(" + (x + 1) + "," + (y + 1) + ")";
    }
}