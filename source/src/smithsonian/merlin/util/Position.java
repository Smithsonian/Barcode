package smithsonian.merlin.util;

/**
 * Created by albesmn on 8/11/2016.
 */
public class Position {

    private int table;
    private int row;

    public Position(int table, int row) {
        this.table = table;
        this.row = row;
    }

    public int getTable() {
        return table;
    }

    public void setTable(int table) {
        this.table = table;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String toString() {
        return table + ", " + row;
    }
}
