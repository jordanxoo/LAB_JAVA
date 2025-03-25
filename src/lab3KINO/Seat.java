package lab3KINO;
import java.io.Serializable;

public class Seat implements Serializable {
    private static final long serialVersionUID = 1L;
    private Character row;
    private Integer number;

    public Seat(Character row, Integer number) {
        this.row = row;
        this.number = number;
    }

    public Character getRow() {
        return row;
    }

    public Integer getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return row + String.valueOf(number);
    }
}
