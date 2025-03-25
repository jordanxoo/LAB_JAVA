package lab3KINO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.*;
import java.util.List;

public class Klient implements Serializable {
    private static final long serialVersionUID = 3L;
    private String nazwisko;
    private String imie;
    private String mail;
    private String telefon;
    private Seans seans;
    private List<Seat> miejsca;

    public Klient(String nazwisko, String imie, String mail, String telefon, Seans seans, List<Seat> miejsca) {
        this.nazwisko = nazwisko;
        this.imie = imie;
        this.mail = mail;
        this.telefon = telefon;
        this.seans = seans;
        this.miejsca = miejsca;
    }

    @Override
    public String toString() {
        return "Klient{" +
                "nazwisko='" + nazwisko + '\'' +
                ", imie='" + imie + '\'' +
                ", mail='" + mail + '\'' +
                ", telefon='" + telefon + '\'' +
                ", seans=" + seans +
                ", miejsca=" + miejsca +
                '}';
    }

    public static void serializeKlient(List<Klient> klienci, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(klienci);
        }
    }

    public static List<Klient> deserializeKlient(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<Klient>) ois.readObject();
        }
    }

    public static void klientToXML(List<Klient> klienci, String filename) {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileWriter writer = new FileWriter(filename)) {
            xstream.toXML(klienci, writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Klient> klientFromXML(String filename) {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileReader reader = new FileReader(filename)) {
            return (List<Klient>) xstream.fromXML(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}