package lab2ONP;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.*;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

class StackOverflowException extends RuntimeException {
    public StackOverflowException() {
        super("Przepełnienie stosu");
    }
}

class InsufficientOperandsException extends RuntimeException {
    public InsufficientOperandsException() {
        super("Niewystarczająca liczba operandów");
    }
}

class UnsupportedOperatorException extends RuntimeException {
    public UnsupportedOperatorException(String message) {
        super(message);
    }
}

class InvalidParenthesisException extends RuntimeException {
    public InvalidParenthesisException(String message) {
        super(message);
    }
}

public class ONP {
    private TabStack stack = new TabStack();
    private List<Equation> equations = new ArrayList<>();

    public void addEquation(Equation equation) {
        this.equations.add(equation);
    }

    boolean czyPoprawneRownanie(String rownanie) {
        return rownanie.endsWith("=");
    }

    public String obliczOnp(String rownanie) {
        if (!czyPoprawneRownanie(rownanie)) {
            Equation equation = new Equation(rownanie, "Błędne równanie: brak znaku '='");
            addEquation(equation);
            return equation.getResult();
        }

        Equation equation = new Equation(rownanie, null);
        stack.setSize(0);

        try {
            StringBuilder currentNumber = new StringBuilder();
            for (int i = 0; i < rownanie.length(); i++) {
                char c = rownanie.charAt(i);

                if (Character.isDigit(c) || c == '.') {
                    currentNumber.append(c);
                    if (i == rownanie.length() - 1 || !(Character.isDigit(rownanie.charAt(i + 1)) || rownanie.charAt(i + 1) == '.')) {
                        stack.push(currentNumber.toString());
                        currentNumber.setLength(0);
                    }
                } else if (c == '=') {
                    if (stack.getSize() != 1) {
                        throw new InsufficientOperandsException();
                    }
                    equation.setResult(stack.pop());
                    addEquation(equation);
                    return equation.getResult();
                } else if (c != ' ') {
                    if ("+-x*/%^√!".indexOf(c) != -1) {
                        handleOperator(c);
                    } else {
                        throw new UnsupportedOperatorException("Nieobsługiwany operator: " + c);
                    }
                }
            }

            if (stack.getSize() != 1) {
                throw new InsufficientOperandsException();
            }

            equation.setResult(stack.pop());
            addEquation(equation);
            return equation.getResult();
        } catch (EmptyStackException e) {
            equation.setResult("Błąd: Za mało elementów na stosie");
            addEquation(equation);
            return equation.getResult();
        } catch (StackOverflowException e) {
            equation.setResult("Błąd: Przepełnienie stosu");
            addEquation(equation);
            return equation.getResult();
        } catch (NumberFormatException e) {
            equation.setResult("Błąd: Nieprawidłowy format liczby");
            addEquation(equation);
            return equation.getResult();
        } catch (ArithmeticException | UnsupportedOperatorException | IllegalArgumentException e) {
            equation.setResult("Błąd: " + e.getMessage());
            addEquation(equation);
            return equation.getResult();
        } catch (InsufficientOperandsException e) {
            equation.setResult("Błąd: Niewystarczająca liczba operandów");
            addEquation(equation);
            return equation.getResult();
        }
    }

    private void handleOperator(char operator) {
        switch (operator) {
            case '+':
            case '-':
            case 'x':
            case '*':
            case '/':
            case '%':
            case '^':
                handleBinaryOperator(operator);
                break;
            case '√':
            case '!':
                handleUnaryOperator(operator);
                break;
            default:
                throw new UnsupportedOperatorException("Nieobsługiwany operator: " + operator);
        }
    }

    private void handleBinaryOperator(char operator) {
        if (stack.getSize() < 2) {
            throw new InsufficientOperandsException();
        }
        double b = Double.parseDouble(stack.pop());
        double a = Double.parseDouble(stack.pop());
        String result;

        switch (operator) {
            case '+':
                result = formatResult(a + b);
                break;
            case '-':
                result = formatResult(a - b);
                break;
            case 'x':
            case '*':
                result = formatResult(a * b);
                break;
            case '/':
                if (b == 0) throw new ArithmeticException("Dzielenie przez zero");
                result = formatResult(a / b);
                break;
            case '%':
                if (b == 0) throw new ArithmeticException("Reszta z dzielenia przez 0");
                result = formatResult(a % b);
                break;
            case '^':
                result = formatResult(Math.pow(a, b));
                break;
            default:
                throw new UnsupportedOperatorException("Nieobsługiwany operator: " + operator);
        }

        stack.push(result);
    }

    private void handleUnaryOperator(char operator) {
        if (stack.getSize() < 1) {
            throw new InsufficientOperandsException();
        }
        double a = Double.parseDouble(stack.pop());
        String result;

        switch (operator) {
            case '√':
                if (a < 0) throw new IllegalArgumentException("Pierwiastek z liczby ujemnej");
                result = formatResult(Math.sqrt(a));
                break;
            case '!':
                if (a < 0 || a != (int) a)
                    throw new IllegalArgumentException("Silnia wymaga liczby całkowitej nieujemnej");
                if (a > 20)
                    throw new IllegalArgumentException("Silnia z liczby większej niż 20 może przekroczyć zakres long");

                long fact = 1;
                for (int i = 2; i <= (int) a; i++) {
                    fact *= i;
                    if (fact < 0) {
                        throw new ArithmeticException("Przepełnienie przy obliczaniu silni");
                    }
                }
                result = String.valueOf(fact);
                break;
            default:
                throw new UnsupportedOperatorException("Nieobsługiwany operator: " + operator);
        }

        stack.push(result);
    }


    private String formatResult(double value) {

        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }

    public String przeksztalcNaOnp(String rownanie) {
        if (!czyPoprawneRownanie(rownanie)) {
            return "Błędne równanie: brak znaku '='";
        }

        stack.setSize(0);
        StringBuilder wynik = new StringBuilder();
        int parenthesisBalance = 0;

        try {
            for (int i = 0; i < rownanie.length(); i++) {
                char c = rownanie.charAt(i);

                if (Character.isDigit(c) || c == '.') {
                    wynik.append(c);
                    if (i == rownanie.length() - 1 || !(Character.isDigit(rownanie.charAt(i + 1)) || rownanie.charAt(i + 1) == '.')) {
                        wynik.append(" ");
                    }
                } else if (c == ' ') {

                } else {
                    switch (c) {
                        case '(':
                            stack.push("(");
                            parenthesisBalance++;
                            break;
                        case ')':
                            parenthesisBalance--;
                            if (parenthesisBalance < 0) {
                                throw new InvalidParenthesisException("Nadmiarowy nawias zamykający");
                            }

                            while (stack.getSize() > 0 && !stack.showValue(stack.getSize() - 1).equals("(")) {
                                wynik.append(stack.pop()).append(" ");
                            }

                            if (stack.getSize() == 0) {
                                throw new InvalidParenthesisException("Niepasujący nawias zamykający");
                            }

                            stack.pop();
                            break;
                        case '=':
                            while (stack.getSize() > 0) {
                                String op = stack.pop();
                                if (op.equals("(")) {
                                    throw new InvalidParenthesisException("Niezamknięty nawias");
                                }
                                wynik.append(op).append(" ");
                            }
                            wynik.append("=");
                            break;
                        default:
                            if ("+-*/%^√!x".indexOf(c) != -1) {
                                while (stack.getSize() > 0 && shouldPopOperator(c)) {
                                    wynik.append(stack.pop()).append(" ");
                                }
                                stack.push(Character.toString(c));
                            } else {
                                throw new UnsupportedOperatorException("Nieobsługiwany operator: " + c);
                            }
                            break;
                    }
                }
            }

            if (parenthesisBalance != 0) {
                throw new InvalidParenthesisException("Niezrównoważone nawiasy");
            }

            return wynik.toString().trim();
        } catch (EmptyStackException e) {
            return "Błędne równanie: niezrównoważone operatory";
        } catch (InvalidParenthesisException | UnsupportedOperatorException e) {
            return "Błędne równanie: " + e.getMessage();
        }
    }

    private boolean shouldPopOperator(char newOp) {
        if (stack.getSize() == 0) return false;

        String currentOp = stack.showValue(stack.getSize() - 1);
        if (currentOp.equals("(")) return false;

        int newPrecedence = getPrecedence(newOp);
        int currentPrecedence = getPrecedence(currentOp.charAt(0));

        return currentPrecedence >= newPrecedence;
    }

    private int getPrecedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
            case '%':
            case 'x':
                return 2;
            case '^':
            case '√':
            case '!':
                return 3;
            default:
                return 0;
        }
    }

    public void saveEquationsToXML(String filename) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileWriter writer = new FileWriter(filename)) {
            xstream.toXML(this.equations, writer);
        }
    }
    @SuppressWarnings("unchecked")
    public static List<Equation> readEquationsFromXML(String filename) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        try (FileReader reader = new FileReader(filename)) {
            return (List<Equation>) xstream.fromXML(reader);
        }
    }


    public static void main(String[] args) {
        String[] testCases = {
                "(2+3)*(6-2)^2=",
                "(7+1)*((4-2)^2)=",
                "(8+1)/((4-2)^2)=",
                "(2+3)*(2-3)+8=",
                "(22+3)/(2-34)*3+8=",
                "(2-34)*3+8",
                "(22+3)/(2-34)*3+8)=",
                "5! + √9=",
                "2^3^2=",
                "10/0=",
                "10%0=",
                "√(-4)=",
                "5.5!=",
        };

        ONP onp = new ONP();
        for (String tmp : testCases) {
            System.out.println("Równanie: " + tmp);
            String rownanieOnp = onp.przeksztalcNaOnp(tmp);
            System.out.println("lab2ONP.ONP: " + rownanieOnp);
            String wynik = onp.obliczOnp(rownanieOnp);
            System.out.println("Wynik: " + wynik);
            System.out.println();
        }

        try {
            onp.saveEquationsToXML("equations.xml");
            List<Equation> loadedEquations = ONP.readEquationsFromXML("equations.xml");
            if (loadedEquations != null) {
                System.out.println("Załadowane równania z XML:");
                for (Equation eq : loadedEquations) {
                    System.out.println(eq);
                }
            } else {
                System.out.println("Nie udało się załadować równań z XML.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}