package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;

public class StackImpl <T> implements Stack <T> {
    linkedT head = new linkedT();
    int counter=0;
    linkedT top;

    public StackImpl(){
    }

    /**
     * @param <T>
     */

    /**
     * @param element object to add to the Stack
     */
    public void push(T element){
        linkedT newElement = new linkedT(element);
        linkedT current = head;

        while (current.next != null) { //Goes to the end
            current = current.next;
        }
        current.next = newElement;  //Adds at the end
        this.top = newElement;

        counter += 1;
    }


    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop(){
        linkedT current = head;

        if (current.next == null ){ //Empty Stack
            return null;
        }
        while (current.next.next != null) { //This should bring to the second to last element.
            current= current.next;
        }
        linkedT lastElement = current.next; //Saving the last so can return later
        current.next = null;

        top= current;
        counter -= 1;
        return (T) lastElement.element;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek(){
        return (T) this.top.element;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size(){
        return counter;
    }



    private class linkedT<T> {
        linkedT next;
        T element;
        protected linkedT() {
        }
        protected linkedT (T element){
            this.element =element;
        }
    }



}
