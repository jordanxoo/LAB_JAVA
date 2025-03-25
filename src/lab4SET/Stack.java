package lab4SET;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class Stack <X> {

    LinkedList<X> LinkedL = new LinkedList<>();


    public void push(X xxx) {
        LinkedL.addFirst(xxx);
    }

    public X peek()
    {
        try
        {
            return LinkedL.getFirst();

        }catch (Exception e)
        {
            System.out.println("Stack is empty");
            return null;
        }
    }

    public boolean empty()
    {
        return LinkedL.isEmpty();
    }

    public X pop()
    {

         try
         {
             X element = LinkedL.getFirst();
             LinkedL.removeFirst();
             return  element;
         }
         catch(Exception e)
         {
             System.out.println("Stack is empty");
             return null;
         }
    }

    public String toString()
    {
        return LinkedL.toString();
    }





}
