package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;
import java.util.*;

public class TrieImpl <Value> implements Trie <Value> {

    public  TrieImpl (){
    }

    private static final int alphabetSize = 256; // extended ASCII
    private final TrieImpl.Node root = new Node(); // root of trie


    private class Node<Value> {
        private  ArrayList<Value> val = new ArrayList<Value>();
        private   TrieImpl.Node[] links = new Node[TrieImpl.alphabetSize]; //Judah calls them links. links makes it easier for me to understand.

    }


    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {
        String upKey = key.toUpperCase();
        if (val == null) {
            return;
        }

        Node movingNode = this.root;//starts at root
        int length = upKey.length();
        int index; //this will track the index of the links
        int level; //this tracks both the length of the word and depth of the trie

        for (level = 0; level < length; level++) {
            index = upKey.charAt(level);
            if (movingNode.links[index] == null) {
                //    Node newNode = new Node();
                movingNode.links[index] = new Node();
            }
            movingNode = movingNode.links[index];
        }

        if (!movingNode.val.contains(val)) {
            movingNode.val.add(val);
        }
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key
     * @param comparator used to sort  values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator){
        String upKey = key.toUpperCase();
        Node finalNode = getToTheLastNode(upKey);
        ArrayList<Value> values = new ArrayList<Value>();

        if (finalNode == null){ //No entry for that keyword
            List<Value> emptyList = new ArrayList<Value>();
            return emptyList;
        }

        values = finalNode.val;


        Collections.sort(values, comparator);
        return values;
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        String upPrefix = prefix.toUpperCase();
        Node lastNode = getToTheLastNode(upPrefix);

        if (lastNode == null){ //No words with such a prefix
            List<Value> emptyList = new ArrayList<Value>();
            return emptyList;
        }

        List<Node> allNodes = new ArrayList<Node>();
        allNodes=allNodesFromThisPrefix(lastNode, allNodes);


        HashSet<Value> allValues = new HashSet<Value>();

        for (Node node : allNodes){
            for ( Object val :  node.val){
                allValues.add((Value) val);
            }
        }
        List<Value> valuesList = new ArrayList<Value>();
        for (Value val : allValues){
            valuesList.add(val);
        }

        Collections.sort(valuesList, comparator);
        return valuesList;
    }


    //My Own Method
    private Node getToTheLastNode (String key){
        Node movingNode = this.root;//starts at root
        int length = key.length();
        int index; //this will track the index of the links
        int level; //this tracks both the length of the word and depth of the trie

        for (level = 0; level < length; level++) {
            index= key.charAt(level) ;
            if (movingNode.links[index] == null) { //Returns null, .
                return null;
            }
            movingNode = movingNode.links[index];
        }
        return movingNode;
    }



    //My Own Method. I think this way is better.
    private List<Node> allNodesFromThisPrefix (Node bottom, List<Node> allNodes){
        if (bottom.val != null){
            allNodes.add(bottom);
        }

        for (int i =0; i < TrieImpl.alphabetSize; i++){
            if( bottom.links[i] != null){
                allNodesFromThisPrefix(bottom.links[i],allNodes);
            }
        }
        return allNodes;
    }


    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAllWithPrefix(String prefix){
        String upPrefix = prefix.toUpperCase();
        Node lastNode = getToTheLastNode(upPrefix);
        List<Node> allNodes = new ArrayList<Node>();
        Set<Value> valueSet = new HashSet<Value>();

        try {
            allNodes = allNodesFromThisPrefix(lastNode, allNodes);
        } catch(NullPointerException e){
            return valueSet;
        }





        for (Node node : allNodes){
            for ( Object val :  node.val){
                valueSet.add((Value) val);
            }
            node.val.clear();
            node = null;
        }

        return valueSet;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAll(String key){
        String upKey = key.toUpperCase();
        Node finalNode = getToTheLastNode(upKey);
        if (finalNode == null){
            Set<Value> emptySet = new HashSet<Value>();
            return emptySet;
        }
        Set<Value> valueSet = new HashSet<Value>();
        List<Value> allValues = finalNode.val;

        for (Value val : allValues){
            valueSet.add(val);
        }
        //finalNode.val.clear();
        finalNode = null;

        return valueSet;
    }

    /**
     * delete ONLY the given value from the given key. Leave all other values.
     * @param key
     * @param val
     * @return if there was a Value already at that key, return that previous Value. Otherwise, return null.
     */
    public Value delete(String key, Value val) {
        String upKey = key.toUpperCase();
        Node keyNode = getToTheLastNode(upKey);
        List<Value> allValues = keyNode.val;

        for (Value v : allValues ){
            if (v.equals(val)){
                keyNode.val.remove(v);
                if(allValues.size() ==0){
                    keyNode=null;
                }
                return val;
            }
        }
        return null;
    }

}


