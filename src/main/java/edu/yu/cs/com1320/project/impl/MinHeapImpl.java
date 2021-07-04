package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;


import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

public class MinHeapImpl <E extends Comparable> extends MinHeap <E>  {

    public MinHeapImpl(){
    }


        protected E[] elements = (E[]) new Comparable[10];
        protected int count=0;
        protected Map<E,Integer> elementsToArrayIndex; //used to store the index in the elements array



    private int getIndex(E element) {
        for (int x = 1; x < elements.length; x++) {
          //  System.out.println(element.toString());
         //   System.out.println(element.toString());
            if (elements[x].equals(element)) {

                int index = x;
                return index;
            }
        }
        System.out.println("heya");
        throw new NullPointerException();
    }

        @Override
        public void reHeapify(E element) {
            int index = getIndex(element);
            this.elements[index] = element;
            if (count >= index * 2) {
                if ((isGreater(index, index * 2) || ((count >= index *2 +1) && isGreater(index, 2 * index + 1)))) {

                    downHeap(index);

                }
            } else if (index >= 2 && isGreater(index / 2, index)) {
                    upHeap(index);
            }
        }


        @Override
        protected  int getArrayIndex(E element){
            for (int x =1; x < elements.length; x++){
                if (element.equals(elements[x])){
                    return x;
                }
            }
            return -1; //what should return???
        }



        @Override
        protected  void doubleArraySize(){
        E[] doubledArray =  Arrays.copyOf(this.elements, (this.elements.length *2));
        this.elements = doubledArray;
        }

        @Override
        protected  boolean isEmpty()
        {
            return this.count == 0;
        }
        /**
         * is elements[i] > elements[j]?
         */

        @Override
        protected boolean isGreater(int i, int j)
        {


            return this.elements[i].compareTo(this.elements[j]) > 0;
        }

        /**
         * swap the values stored at elements[i] and elements[j]
         */
        @Override
        protected  void swap(int i, int j)
        {
            E temp = this.elements[i];
            this.elements[i] = this.elements[j];
            this.elements[j] = temp;
        }

        /**
         *while the key at index k is less than its
         *parent's key, swap its contents with its parent’s
         */
        @Override
        protected  void upHeap(int k)
        {
            while (k > 1 && this.isGreater(k / 2, k))
            {
                this.swap(k, k / 2);
                k = k / 2;
            }
        }

        /**
         * move an element down the heap until it is less than
         * both its children or is at the bottom of the heap
         */
        @Override
        protected  void downHeap(int k)
        {
            while (2 * k <= this.count)
            {
                //identify which of the 2 children are smaller
                int j = 2 * k;
                if (j < this.count && this.isGreater(j, j + 1))
                {
                    j++;
                }
                //if the current value is < the smaller child, we're done
                if (!this.isGreater(k, j))
                {
                    break;
                }
                //if not, swap and continue testing
                this.swap(k, j);
                k = j;
            }
        }

        @Override
        public void insert(E x)
        {

            if(getArrayIndex(x)!=-1){
                this.elements[getArrayIndex(x)] =x;
            }

            // double size of array if necessary
            if (this.count >= this.elements.length - 1)
            {
                this.doubleArraySize();
            }
            //add x to the bottom of the heap
            this.elements[++this.count] = x;
            //percolate it up to maintain heap order property
            this.upHeap(this.count);
        }

        @Override
        public E removeMin()
        {
            if (isEmpty())
            {
                throw new NoSuchElementException("Heap is empty");
            }
            E min = this.elements[1];
            //swap root with last, decrement count
            this.swap(1, this.count--);
            //move new root down as needed
            this.downHeap(1);
            this.elements[this.count + 1] = null; //null it to prepare for GC
            return min;
        }

}
