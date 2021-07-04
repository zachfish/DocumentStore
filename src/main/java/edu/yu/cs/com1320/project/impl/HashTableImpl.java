package edu.yu.cs.com1320.project.impl;


public class HashTableImpl <Key,Value> { //implements HashTable <Key,Value> {

    KeyAndValueObj[] myHashTable= new KeyAndValueObj[5];
    private int loadFactor=0;


    public HashTableImpl () {
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */

    public Value get(Key k){
        int hashCode = k.hashCode(); //This may be wrong. The Key might already be the HashCode necessary- see Piazza- look there to figure out.
        int index = (hashCode & 0x7fffffff) % myHashTable.length; //This is the hashFunction

        KeyAndValueObj current = myHashTable[index];

        if (myHashTable[index] == null) {
            return null;
        }

        if (current.key==k){
            return (Value) current.value;
        }

        while(current.next!=null) {
            current=current.next;
            if (current.key==k) {
                return (Value)current.value;
            }
        }

        return null;
    }


    //my own method:

    private void deleteEntry(Key k){
        int hashCode = k.hashCode();
        int index = (hashCode  & 0x7fffffff)  % this.myHashTable.length;
        Value val = get(k);

        KeyAndValueObj current = myHashTable[index];
        while (current.next.value != val) {
            current = current.next;
        }
        current.next= current.next.next;

    }


    /**
     * @param k the key at which to store the value
     * @param v the value to store
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */


    public Value put(Key k, Value v){

        KeyAndValueObj kAndV = new KeyAndValueObj(k,v);

        int hashCode = k.hashCode();
        int index = (hashCode & 0x7fffffff) % this.myHashTable.length; //This is the hashFunction


        if (v==null) { //This deletes the entry. When delete doc is called, it enters a null value to delete.
            Value val;
            try { //This will see if already nothing there and then just return null
                val = get(k);
            } catch (NullPointerException e) {
                return null;
            }
            deleteEntry(k);
            return null;
        }


        if (myHashTable[index]==null) {  //This is for is is first value for that index
            KeyAndValueObj header = new KeyAndValueObj(); //HEADER IS ALWAYS CREATED FIRST: VERY IMPORTANT TO REMEMBER.
            myHashTable[index] = header;
            header.next=kAndV;
            return null;
        }


        KeyAndValueObj current= myHashTable[index]; //Finds the first value in the index
        while(current.next!=null) {
            current=current.next;

            if (current.key==k) { //Already there, changes value if there is a change in value
                Value oldValue = (Value) current.value;
                current.value =kAndV.value;
                return oldValue; //returns the OLD VAlUE
            }
        }

        current.next=kAndV;//gets to end, does not find old. adds to end.
        loadFactor+=1;

        if (loadFactor / myHashTable.length >= 5){
            arrayDouble();
        }
        return null;
    }


    private void arrayDouble () {
        KeyAndValueObj[] oldArray = this.myHashTable;
        KeyAndValueObj[] newArray = new KeyAndValueObj[oldArray.length * 2];

        this.myHashTable= newArray;

        for (int x = 0; x <= oldArray.length; x++) {
            KeyAndValueObj current =oldArray[x];
            while (current.next != null){
                put((Key)current.next.key, (Value)current.next.value);
            }
        }


    }

    //Nested Class
    private class KeyAndValueObj<Key, Value> { //Definitely need to make sure this is OK
        Key key;
        Value value;
        KeyAndValueObj next;

        protected KeyAndValueObj() {
        }

        protected KeyAndValueObj (Key k, Value v){
            this.key=k;
            this.value=v;
        }

    }


}



