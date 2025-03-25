package lab3KINO;

import java.io.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
public class Pracownik implements Serializable {
    private static final long serialVersionUID = -7887612267521882048L;
    String imie;
    String nazwisko;
    String email;

    public Pracownik(String imie, String nazwisko, String email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Pracownik [imie=" + imie + ", nazwisko=" + nazwisko
                + ", email=" + email + "]";
    }


    public void serializationPracownik(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static Pracownik deserializationPracownik(String filename) throws IOException, ClassNotFoundException
    {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename)))
        {

            return  (Pracownik) ois.readObject();
        }
    }

    public void pracownikToXML(String filename)
    {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try(FileWriter writer =  new FileWriter(filename))
        {
            xstream.toXML(this,writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Pracownik fromXML(String filename)
    {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try(FileReader reader = new FileReader(filename))
        {
            return (Pracownik) xstream.fromXML(reader);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}