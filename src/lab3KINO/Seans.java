package lab3KINO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class Seans implements Serializable {
    private static final long serialVersionUID = 1L;
    String tytul;
    String dzien;
    String godzina;
    String ograniczeniaWiekowe;
    private HashMap<Character, HashMap<Integer, Boolean>> liczbaMiejsc;

    public Seans(String tytul, String dzien, String godzina, String ograniczeniaWiekowe, List<Character> rows, int seatsPerRow) {
        this.tytul = tytul;
        this.dzien = dzien;
        this.godzina = godzina;
        this.ograniczeniaWiekowe = ograniczeniaWiekowe;
        this.liczbaMiejsc = new HashMap<>();

        for (Character row : rows) {
            HashMap<Integer, Boolean> seats = new HashMap<>();
            for (int i = 1; i <= seatsPerRow; i++) {
                seats.put(i, true);
            }
            liczbaMiejsc.put(row, seats);
        }
    }

    public boolean reserveSeats(List<Seat> seats) {
        for (Seat seat : seats) {
            if (!liczbaMiejsc.get(seat.getRow()).get(seat.getNumber())) {
                return false; // Seat is already reserved
            }
        }
        for (Seat seat : seats) {
            liczbaMiejsc.get(seat.getRow()).put(seat.getNumber(), false); // Reserve the seat
        }
        return true;
    }

    @Override
    public String toString() {
        return "Seans{" +
                "tytul='" + tytul + '\'' +
                ", dzien='" + dzien + '\'' +
                ", godzina='" + godzina + '\'' +
                ", ograniczeniaWiekowe='" + ograniczeniaWiekowe + '\'' +
                ", liczbaMiejsc=" + liczbaMiejsc +
                '}';
    }

    public static void serializeSeans(List<Seans> seanse, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(seanse);
        }
    }

    public static List<Seans> deserializeSeans(String filename)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<Seans>) ois.readObject();
        }
    }
    public static void seansToXML(List<Seans> seanse, String filename) {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileWriter writer = new FileWriter(filename)) {
            xstream.toXML(seanse, writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Seans> seansFromXML(String filename) {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileReader reader = new FileReader(filename)) {
            return (List<Seans>) xstream.fromXML(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}



