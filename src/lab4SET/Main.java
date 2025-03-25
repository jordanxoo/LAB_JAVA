package lab4SET;

public class Main {
    public static void main(String[] args) {



        Stack<Gracz> stack = new Stack<>();

        Gracz gracz1 = new Gracz(20, "Kuba", 100);
        Gracz gracz2 = new Gracz(21, "Kuba2", 200);
        Gracz gracz3 = new Gracz(22, "Kuba3", 300);

        stack.push(gracz1);
        stack.push(gracz2);
        stack.push(gracz3);
       // stack.pop();

        System.out.println(stack);
        System.out.println(stack.peek());
        System.out.println(stack.empty());

    }
}