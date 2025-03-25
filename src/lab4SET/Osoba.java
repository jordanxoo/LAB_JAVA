package lab4SET;
class Osoba implements Comparable<Osoba> {
    private String imie;
    private String nazwisko;
    private int wiek;

    public Osoba(String imie, String nazwisko, int wiek) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.wiek = wiek;
    }

    @Override
    public int compareTo(Osoba inna) {
        int porownanieNazwisk = this.nazwisko.compareTo(inna.nazwisko);
        if (porownanieNazwisk != 0) {
            return porownanieNazwisk;
        }
        int porownanieImion = this.imie.compareTo(inna.imie);
        if (porownanieImion != 0) {
            return porownanieImion;
        }
        return Integer.compare(this.wiek, inna.wiek);
    }

    @Override
    public String toString() {
        return imie + " " + nazwisko + " (" + wiek + " lat)";
    }
}
