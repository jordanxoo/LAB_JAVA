package lab4SET;
import java.util.Arrays;

public class Set <T extends Comparable>
{
    private T[] set;
    private int pojemnosc;
    private int rozmiar;

    @SuppressWarnings("unchecked")
    public Set(int pojemnosc)
    {
        this.pojemnosc = pojemnosc;
        this.rozmiar =0;
        this.set = (T[]) new Comparable[pojemnosc];
    }

    @SuppressWarnings("unchecked")
    public int szukaj(T element) {
        for (int i = 0; i < rozmiar; i++) {
            if (set[i] != null && set[i].compareTo(element) == 0) {
                return i;
            }
        }
        return -1;
    }


    public void dodajElement(T element) {
        if (rozmiar >= pojemnosc) {
            throw new IllegalStateException("Set is full");
        }
        int poz = szukaj(element);
        if (poz >= 0) {
            return;
        }

        int i = 0;
        while (i < rozmiar && set[i].compareTo(element) < 0) {
            i++;
        }

        for (int j = rozmiar; j > i; j--) {
            set[j] = set[j - 1];
        }

        set[i] = element;
        rozmiar++;
    }

    public void usunElement(T element)
    {
        int poz = szukaj(element);

        if(poz < 0)
        {
            throw new IllegalStateException("Element does not exist");
        }

        for(int j=poz;j<rozmiar-1;j++)
        {
            set[j] = set[j+1];
        }
        rozmiar--;
    }

    public Set<T> dodajElementy(Set<T> setDodatkowy)
    {
        if(setDodatkowy.rozmiar + rozmiar > pojemnosc)
        {
            throw new IllegalStateException("Set is full");
        }
        Set<T> nowySet = new Set<T>(rozmiar + setDodatkowy.rozmiar);

        for(int i=0;i<rozmiar;i++)
        {
            nowySet.dodajElement(set[i]);
        }

        for(int j=0;j<setDodatkowy.rozmiar;j++)
        {
            nowySet.dodajElement(setDodatkowy.set[j]);
        }

        return nowySet;
    }

    public Set<T> odejmijElementy(Set<T> setDodatkowy)
    {
        Set<T> nowySet = new Set<T>(this.rozmiar);
        for (int i = 0; i < this.rozmiar; i++) {
            if (setDodatkowy.szukaj(this.set[i]) < 0) {
                nowySet.dodajElement(this.set[i]);
            }
        }
        return nowySet;
    }

    public Set<T> przeciecie(Set<T> setDodatkowy)
    {
        Set<T> nowySet = new Set<>(this.rozmiar);
        for(int i=0;i<this.rozmiar;i++)
        {
            if(setDodatkowy.szukaj(this.set[i]) >=0)
            {
                nowySet.dodajElement(this.set[i]);
            }
        }
        return nowySet;
    }

    public String toString()
    {
        return "Set{" +
                "set=" + Arrays.toString(set) +
                ", pojemnosc=" + pojemnosc +
                ", rozmiar=" + rozmiar +
                '}';
    }

}
