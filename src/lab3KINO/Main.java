package lab3KINO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<Character> rows = new ArrayList<>();
        rows.add('A');
        rows.add('B');

        Seans seans1 = new Seans("Avengers", "2023-10-10", "18:00", "PG-13", rows, 3);
        Seans seans2 = new Seans("Inception", "2023-10-11", "20:00", "15+", rows, 3);
        List<Seans> listaSeansow = new ArrayList<>();
        listaSeansow.add(seans1);
        listaSeansow.add(seans2);


//        Seans.serializeSeans(listaSeansow, "seanse.dat");
//        List<Seans> odczytaneSeanse = Seans.deserializeSeans("seanse.dat");
//        System.out.println("==== Seanse z pliku .dat ====");
//        odczytaneSeanse.forEach(System.out::println);
//
//        Seans.seansToXML(listaSeansow, "seanse.xml");
//        List<Seans> seanseZXML = Seans.seansFromXML("seanse.xml");
//        System.out.println("\n==== Seanse z XML ====");
//        seanseZXML.forEach(System.out::println);


        List<Seat> miejsca1 = new ArrayList<>();
        miejsca1.add(new Seat('A', 1));
        miejsca1.add(new Seat('A', 2));
        miejsca1.add(new Seat('A', 3));
//
//        List<Seat> miejsca2 = new ArrayList<>();
//        miejsca2.add(new Seat('B', 3));
//
//        if (seans1.reserveSeats(miejsca1)) {
        Klient klient1 = new Klient("Kowalski", "Jan", "jan@mail.com", "123-456-789", seans1, miejsca1);
        Klient klient2 = new Klient("adamski", "Jan", "jan@mail.com", "123-456-789", seans1, miejsca1);
        Klient klient3 = new Klient("wolski", "Jan", "jan@mail.com", "123-456-789", seans1, miejsca1);
        List<Klient> listaKlientow = new ArrayList<>();
        listaKlientow.add(klient1);
        listaKlientow.add(klient2);
        listaKlientow.add(klient3);


        Klient.serializeKlient(listaKlientow, "klienci.dat");
        List<Klient> odczytaniKlienci = Klient.deserializeKlient("klienci.dat");
        System.out.println("\n==== Klienci z pliku .dat ====");
        odczytaniKlienci.forEach(System.out::println);

        Klient.klientToXML(listaKlientow, "klienci.xml");
        List<Klient> klienciZXML = Klient.klientFromXML("klienci.xml");
        System.out.println("\n==== Klienci z XML ====");
        klienciZXML.forEach(System.out::println);

//            Klient.serializeKlient(listaKlientow, "klienci.dat");
//            List<Klient> odczytaniKlienci = Klient.deserializeKlient("klienci.dat");
//            System.out.println("\n==== Klienci z pliku .dat ====");
//            odczytaniKlienci.forEach(System.out::println);

//            Klient.klientToXML(listaKlientow, "klienci.xml");
//            List<Klient> klienciZXML = Klient.klientFromXML("klienci.xml");
//            System.out.println("\n==== Klienci z XML ====");
//            klienciZXML.forEach(System.out::println);
//        } else {
//            System.out.println("Rezerwacja nieudana: miejsca już zarezerwowane.");
//        }
//
//        if (seans2.reserveSeats(miejsca2)) {
//            Klient klient2 = new Klient("Nowak", "Anna", "anna@mail.com", "987-654-321", seans2, miejsca2);
//            List<Klient> listaKlientow = new ArrayList<>();
//            listaKlientow.add(klient2);
//
//
//            Klient.serializeKlient(listaKlientow, "klienci.dat");
//            List<Klient> odczytaniKlienci = Klient.deserializeKlient("klienci.dat");
//            System.out.println("\n==== Klienci z pliku .dat ====");
//            odczytaniKlienci.forEach(System.out::println);
//
//            Klient.klientToXML(listaKlientow, "klienci.xml");
//            List<Klient> klienciZXML = Klient.klientFromXML("klienci.xml");
//            System.out.println("\n==== Klienci z XML ====");
//            klienciZXML.forEach(System.out::println);
//        } else {
//            System.out.println("Rezerwacja nieudana: miejsca już zarezerwowane.");
//        }
    }
}