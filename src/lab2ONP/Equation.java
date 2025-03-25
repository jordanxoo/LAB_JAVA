package lab2ONP;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.*;

public class Equation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String equation;
    private String result;

    public Equation(String equation, String result) {
        this.equation = equation;
        this.result = result;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void serialization(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static Equation readSerialized(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Equation) ois.readObject();
        }
    }

    public void saveToXML(String filename) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileWriter writer = new FileWriter(filename)) {
            xstream.toXML(this, writer);
        }
    }

    public static Equation readFromXML(String filename) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileReader reader = new FileReader(filename)) {
            return (Equation) xstream.fromXML(reader);
        }
    }

    @Override
    public String toString() {
        return equation + " " + result;
    }
}