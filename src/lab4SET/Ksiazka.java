package lab4SET;
class Ksiazka implements Comparable<Ksiazka> {
    private String tytul;
    private String autor;
    private int rokWydania;

    public Ksiazka(String tytul, String autor, int rokWydania) {
        this.tytul = tytul;
        this.autor = autor;
        this.rokWydania = rokWydania;
    }

    @Override
    public int compareTo(Ksiazka inna) {
        int porownanieAutorow = this.autor.compareTo(inna.autor);
        if (porownanieAutorow != 0) {
            return porownanieAutorow;
        }
        int porownanieRokow = Integer.compare(this.rokWydania, inna.rokWydania);
        if (porownanieRokow != 0) {
            return porownanieRokow;
        }

        return this.tytul.compareTo(inna.tytul);
    }

    @Override
    public String toString() {
        return "\"" + tytul + "\" - " + autor + " (" + rokWydania + ")";
    }
}