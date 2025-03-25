package lab2ONP;

import java.io.Serializable;
import java.util.EmptyStackException;

public class TabStack implements Serializable {
    private String[] stack = new String[20];
    private int size = 0;

    public String pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        size--;
        return stack[size];
    }

    public void push(String a) {
        if (size >= stack.length) {
            throw new StackOverflowException();
        }
        stack[size] = a;
        size++;
    }

    public String toString() {
        String tmp = "";
        for (int i = 0; i < size; i++)
            tmp += stack[i] + " ";
        return tmp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int i) {
        size = i;
    }

    public String showValue(int i) {
        if (i < size)
            return stack[i];
        else
            return null;
    }
}