package lab4SET;
public class TestSet {
    public static void main(String[] args) {
        try {

            testOsoba();

            System.out.println("\n--------------------------------------\n");

            testKsiazka();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testOsoba() {
        System.out.println("Test klasy Set z obiektami typu Osoba:");

        // Utworzenie zbioru
        Set<Osoba> zbiorOsob = new Set<>(10);

        // Dodawanie elementów
        zbiorOsob.dodajElement(new Osoba("Jan", "Kowalski", 30));
        zbiorOsob.dodajElement(new Osoba("Anna", "Nowak", 25));
        zbiorOsob.dodajElement(new Osoba("Piotr", "Wiśniewski", 40));
        zbiorOsob.dodajElement(new Osoba("Maria", "Kowalska", 35));
        zbiorOsob.dodajElement(new Osoba("Jan", "Kowalski", 30)); // Duplikat - nie powinien zostać dodany

        // Wyświetlenie informacji o zbiorze
        System.out.println("Zbiór osób po dodaniu elementów:");
        System.out.println(zbiorOsob);

        // Wyszukiwanie elementu
        Osoba szukanaOsoba = new Osoba("Anna", "Nowak", 25);
        int pozycja = zbiorOsob.szukaj(szukanaOsoba);
        System.out.println("Pozycja osoby " + szukanaOsoba + ": " + pozycja);

        // Usuwanie elementu
        zbiorOsob.usunElement(new Osoba("Piotr", "Wiśniewski", 40));
        System.out.println("Zbiór osób po usunięciu Piotra Wiśniewskiego:");
        System.out.println(zbiorOsob);

        // Utworzenie drugiego zbioru
        Set<Osoba> zbiorOsob2 = new Set<>(5);
        zbiorOsob2.dodajElement(new Osoba("Anna", "Nowak", 25)); // Powtórzony element
        zbiorOsob2.dodajElement(new Osoba("Tomasz", "Lewandowski", 28));
        zbiorOsob2.dodajElement(new Osoba("Karolina", "Zielińska", 22));

        System.out.println("Drugi zbiór osób:");
        System.out.println(zbiorOsob2);

        // Operacje na zbiorach
        Set<Osoba> sumaZbiorow = zbiorOsob.dodajElementy(zbiorOsob2);
        System.out.println("Suma zbiorów:");
        System.out.println(sumaZbiorow);

        Set<Osoba> roznicaZbiorow = zbiorOsob.odejmijElementy(zbiorOsob2);
        System.out.println("Różnica zbiorów (zbiór1 - zbiór2):");
        System.out.println(roznicaZbiorow);

        Set<Osoba> przeciecieZbiorow = zbiorOsob.przeciecie(zbiorOsob2);
        System.out.println("Przecięcie zbiorów:");
        System.out.println(przeciecieZbiorow);
    }

    private static void testKsiazka() {
        System.out.println("Test klasy Set z obiektami typu Książka:");

        // Utworzenie zbioru
        Set<Ksiazka> zbiorKsiazek = new Set<>(10);

        // Dodawanie elementów
        zbiorKsiazek.dodajElement(new Ksiazka("Lalka", "Bolesław Prus", 1890));
        zbiorKsiazek.dodajElement(new Ksiazka("Pan Tadeusz", "Adam Mickiewicz", 1834));
        zbiorKsiazek.dodajElement(new Ksiazka("Quo Vadis", "Henryk Sienkiewicz", 1896));
        zbiorKsiazek.dodajElement(new Ksiazka("Dziady", "Adam Mickiewicz", 1822));
        zbiorKsiazek.dodajElement(new Ksiazka("Lalka", "Bolesław Prus", 1890)); // Duplikat - nie powinien zostać dodany

        // Wyświetlenie informacji o zbiorze
        System.out.println("Zbiór książek po dodaniu elementów:");
        System.out.println(zbiorKsiazek);

        // Wyszukiwanie elementu
        Ksiazka szukanaKsiazka = new Ksiazka("Pan Tadeusz", "Adam Mickiewicz", 1834);
        int pozycja = zbiorKsiazek.szukaj(szukanaKsiazka);
        System.out.println("Pozycja książki " + szukanaKsiazka + ": " + pozycja);
        zbiorKsiazek.usunElement(new Ksiazka("Quo Vadis", "Henryk Sienkiewicz", 1896));
        System.out.println("Zbiór książek po usunięciu Quo Vadis:");
        System.out.println(zbiorKsiazek);
        Set<Ksiazka> zbiorKsiazek2 = new Set<>(5);
        zbiorKsiazek2.dodajElement(new Ksiazka("Pan Tadeusz", "Adam Mickiewicz", 1834)); // Powtórzony element
        zbiorKsiazek2.dodajElement(new Ksiazka("Krzyżacy", "Henryk Sienkiewicz", 1900));
        zbiorKsiazek2.dodajElement(new Ksiazka("Chłopi", "Władysław Reymont", 1904));

        System.out.println("Drugi zbiór książek:");
        System.out.println(zbiorKsiazek2);


        Set<Ksiazka> sumaZbiorow = zbiorKsiazek.dodajElementy(zbiorKsiazek2);
        System.out.println("Suma zbiorów:");
        System.out.println(sumaZbiorow);

        Set<Ksiazka> roznicaZbiorow = zbiorKsiazek.odejmijElementy(zbiorKsiazek2);
        System.out.println("Różnica zbiorów (zbiór1 - zbiór2):");
        System.out.println(roznicaZbiorow);

        Set<Ksiazka> przeciecieZbiorow = zbiorKsiazek.przeciecie(zbiorKsiazek2);
        System.out.println("Przecięcie zbiorów:");
        System.out.println(przeciecieZbiorow);


        try {
            Set<Ksiazka> malyZbior = new Set<>(2);
            malyZbior.dodajElement(new Ksiazka("Lalka", "Bolesław Prus", 1890));
            malyZbior.dodajElement(new Ksiazka("Pan Tadeusz", "Adam Mickiewicz", 1834));
            System.out.println("Mały zbiór po dodaniu 2 elementów:");
            System.out.println(malyZbior);

            System.out.println("Próba dodania elementu do pełnego zbioru:");
            malyZbior.dodajElement(new Ksiazka("Quo Vadis", "Henryk Sienkiewicz", 1896));
        } catch (IllegalStateException e) {
            System.out.println("Złapano wyjątek: " + e.getMessage());
        }
    }
}