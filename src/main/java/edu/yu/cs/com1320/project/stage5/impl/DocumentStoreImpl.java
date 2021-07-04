package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {

    public DocumentStoreImpl()  {
        this.dpm = new DocumentPersistenceManager(null);
        bTree  = new BTreeImpl(dpm);

        try {
            this.sentinel = new URI("");
        } catch (Exception e){

        }
        bTree.put(sentinel, null);
}

    public DocumentStoreImpl(File baseDir){
        this.dpm= new DocumentPersistenceManager(baseDir);
        bTree  = new BTreeImpl(dpm);
        try {
            this.sentinel = new URI("");
        } catch (Exception e){

        }
        bTree.put(sentinel, null);
    }


    URI sentinel;
    File baseDir;
    Stack stack =new StackImpl<Undoable>();
    DocumentPersistenceManager dpm;
    BTreeImpl <URI, Document> bTree;
    TrieImpl<UriWithTime> trie= new TrieImpl();
    MinHeapImpl <UriWithTime> minHeap = new MinHeapImpl();
    HashSet<URI> liveDocs = new HashSet<>();


    private  int docLimit=0;
    private  int byteLimit=0;
    private  int liveDocsCount;
    private  int liveBytesCount;


    private class UriWithTime implements Comparable{
        private URI uri;
        private Long lastUsedTime;

        private UriWithTime(URI uri){
            this.uri=uri;
         //   Document doc = (Document) bTree.get(uri);
            this.lastUsedTime= System.nanoTime();
        }

        private Long getLastUsedTime(){
            return this.lastUsedTime;
        }

        private void setLastUsedTime(Long now){
            this.lastUsedTime = now;
        }

        private URI getUri(){
            return this.uri;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof UriWithTime)) {
                return false;
            }

            UriWithTime u = (UriWithTime) obj;

            if (u.getUri() == this.getUri()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(Object o) {
            UriWithTime uri = (UriWithTime) o;
            if (this.lastUsedTime >= uri.getLastUsedTime()) return 1;
            if (this.lastUsedTime < uri.getLastUsedTime()) return -1;
            return 0;
        }
    }



    private void removeMinCompletelyFromMemory(){
        URI leastUsedURI =  this.minHeap.removeMin().getUri(); //Return a URI with time so still need to get the URI
        DocumentImpl leastUsed = (DocumentImpl) bTree.get(leastUsedURI);
        removeFromLiveDocNumbersDoc(leastUsed);
       // removeDocFromTrieDoc(leastUsed); //deletes from Trie
        //myHashTable.put(uri, null); //then it Deletes it from hashTable
        removeFromUndo(leastUsedURI);
        try {
            bTree.moveToDisk(leastUsedURI);
        } catch (Exception e) {
            e.printStackTrace();

        }
       // bTree.put(leastUsedURI, null);
        this.liveDocs.remove(leastUsedURI);

    }



    private void removeFromUndo(URI uri) {
        StackImpl tempStack = new StackImpl();
        int stackSize = stack.size();

        while (stack.size() != 0) {
            if (stack.peek() instanceof GenericCommand) {
                GenericCommand gc = (GenericCommand) stack.peek();
                if (gc.getTarget().equals(uri)) {
                    stack.pop();
                }
            }
            if (stack.peek() instanceof CommandSet) {
                CommandSet cs = (CommandSet) stack.peek();
                Iterator<GenericCommand> iterator = cs.iterator();

                while (iterator.hasNext()) {
                    GenericCommand gc = iterator.next();

                    if (gc.getTarget() == uri){
                        cs.remove(gc);
                        break;
                    }
                }
                if (cs.size() == 0) {
                    stack.pop();
                }
            }
            tempStack.push(stack.pop());
        }
        stack = refillOGStack(stack, tempStack);
    }



    private void addToLiveDocNumbers(URI uri) { //puts, undo of delete
        DocumentImpl doc = (DocumentImpl) getDocument(uri);
        String text =doc.getDocumentAsTxt();
        byte[] textBytes = text.getBytes();


        this.liveDocsCount++;
        this.liveBytesCount += doc.pdfBytes.length + textBytes.length;

    }

    private void addToLiveDocNumbersDoc(DocumentImpl doc) { //puts, undo of delete
        String text =doc.getDocumentAsTxt();
        byte[] textBytes = text.getBytes();


        this.liveDocsCount++;
        this.liveBytesCount += doc.pdfBytes.length + textBytes.length;
    }



    private void removeFromLiveDocNumbers (URI uri){
        DocumentImpl doc = (DocumentImpl) getDocument(uri);
        String text =doc.getDocumentAsTxt();
        byte[] textBytes = text.getBytes();

        liveDocsCount--;
        liveBytesCount -= (textBytes.length + doc.pdfBytes.length);

    }


    private void removeFromLiveDocNumbersDoc (DocumentImpl doc){
        String text =doc.getDocumentAsTxt();
        byte[] textBytes = text.getBytes();
      //  this.liveDocs.remove(doc.getKey());
        liveDocsCount--;
        liveBytesCount -= (textBytes.length + doc.pdfBytes.length);

    }


    private void memoryManagement(){
        while ((docLimit > 0 && (liveDocsCount > docLimit  )) || (byteLimit >0 && (liveBytesCount > byteLimit))) {
            removeMinCompletelyFromMemory();
            //     memoryManagement();
        }
    }


    private byte[] getAsByteArray(InputStream input) {
        byte[] data = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        int x;

        try {
            while ((x = input.read()) != -1) { //not 100% sure how this works??? Wouldn't it stop after 1024  - data, 0, data.length - this was in the brackets
                byteArrayOutputStream.write(x); //data, 0, was there b4 x
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("There was an IO Error");
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return byteArray;
    }



    private Function<URI, Boolean> undoPut = uri1 -> {
        removeFromLiveDocNumbers(uri1);
        removeDocFromTrie(uri1);
        removeFromHeap(uri1);
        this.bTree.put(uri1, null);
        return true;
    };

    private int putDocumentTxt(InputStream input, URI uri) {
        byte[] textByteArray = getAsByteArray(input);
        String textString = new String(textByteArray);
        int textHash = (textString.hashCode());

        DocumentImpl textDocumentPrePdf = new DocumentImpl(uri, textString, textHash);
        byte[] pdfBytes = textDocumentPrePdf.getDocumentAsPdf();
        DocumentImpl textDocument = new DocumentImpl(uri, textString, textHash, pdfBytes);  //for stage 4 need to have pdf from beginning
        textDocument.wordCount= createHashMapForWordCount(textString);
        textDocument.setLastUseTime(System.nanoTime()); //have it here so is set for both !!!

        UriWithTime timeUri = new UriWithTime(uri);


        try { //if already there under this URI- returns old- overwrites the old doc with new
            DocumentImpl oldDoc = (DocumentImpl) getDocument(uri);
            String txt = getDocumentAsTxt(uri); //oldString. Will only work if was there before
            int txtHashOld = (txt.hashCode());


            addToTrie(textDocument.wordCount, uri); //This just uses the keys as the set of words
            removeFromLiveDocNumbers(uri); //still the old document at this point

            bTree.put(uri, textDocument);//  putting new version of Doc

            minHeap.insert(timeUri);
            addToLiveDocNumbers(uri); //has to be after put
            memoryManagement();


            Function<URI, Boolean> undoOverwrite = uri1 -> {
                removeFromLiveDocNumbers(uri1);
                removeDocFromTrie(uri1);
                removeFromHeap(uri);
                this.bTree.put(uri1, null);
                this.bTree.put(uri1, oldDoc);
                addToTrie(oldDoc.wordCount, uri);
                minHeap.insert(timeUri);
                addToLiveDocNumbers(uri);
                memoryManagement();

                return true;
            };
            GenericCommand command = new GenericCommand(uri, undoOverwrite);
            stack.push(command);
            return txtHashOld;


        } catch (NullPointerException e) { //New uri

            addToTrie(textDocument.wordCount, uri);
            //myHashTable.put(uri, textDocument);//  putting for the first time
            bTree.put(uri, textDocument);
            minHeap.insert(timeUri);
            addToLiveDocNumbers(uri); //has to be after put
            memoryManagement();


            GenericCommand command = new GenericCommand(uri, this.undoPut);
            stack.push(command);

            return 0;
        }
    }

    private int putDocumentPDF(InputStream input, URI uri) {
        String textString;
        PDDocument doc;
        int textHash = 0;
        try {
            //Getting the Text
            doc = PDDocument.load(input);
            PDFTextStripper stripper = new PDFTextStripper();
            textString = stripper.getText(doc).trim();
        } catch (IOException e) {
            throw new UnsupportedOperationException("There was an IO Error");
        }


        textHash = (textString.hashCode());
        byte[] pdfByteArray;

        try {
            //do not understand why cannot use original input for the byte array- but that did not work- and this did (getting from pdf that created)
            //getting the pdf bytes
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            doc.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            pdfByteArray =getAsByteArray(in);

        } catch (IOException z) {
            throw new UnsupportedOperationException("There was an IO Exception");
        } // Getting as byte array


        DocumentImpl pdfDocument = new DocumentImpl(uri, textString, textHash, pdfByteArray);
        pdfDocument.wordCount = createHashMapForWordCount(textString);
        pdfDocument.setLastUseTime(System.nanoTime());

        UriWithTime timeUri = new UriWithTime(uri);

        try { //Seeing if there was a document there before - and then would return old text hash.
            DocumentImpl oldDoc = (DocumentImpl) bTree.get(uri);
            int oldTextHash= oldDoc.getDocumentTextHashCode();
            addToTrie(pdfDocument.wordCount, uri); //This might need a little- maybe have to get rid of old?

            removeFromLiveDocNumbers(uri); //still the old document at this point
            bTree.put(uri, pdfDocument); //replacing

            minHeap.insert(timeUri);
            addToLiveDocNumbers(uri);
            memoryManagement();

            Function<URI, Boolean> undoOverwrite = uri1 -> {
                removeDocFromTrie(uri1);
                removeFromLiveDocNumbers(uri1);
                removeFromHeap(uri1);
                this.bTree.put(uri1, null);
                this.bTree.put(uri1, oldDoc);
                addToTrie(oldDoc.wordCount, uri);
                minHeap.insert(timeUri);
                addToLiveDocNumbers(uri1);
                memoryManagement();
                return true;
            };

            GenericCommand command = new GenericCommand(uri, undoOverwrite);
            stack.push(command);

            return oldTextHash; //then return old
        } catch (NullPointerException e){  //first time...
            addToTrie(pdfDocument.wordCount, uri);
            bTree.put(uri, pdfDocument);

            minHeap.insert(timeUri);
            addToLiveDocNumbers(uri); //has to be after put

            memoryManagement();


            GenericCommand command = new GenericCommand(uri, this.undoPut);
            stack.push(command);

            return 0;
        }
    }


    private  void addToTrie (HashMap<String,Integer> hashMap, URI uri){
        UriWithTime timeUri = new UriWithTime(uri);
        for (String word : hashMap.keySet()){
            trie.put(word,timeUri);
        }

    }

    private String [] createStringArray (String txt){
        String sameCase = txt.toUpperCase();//case INSensitive
        sameCase= sameCase.replaceAll("[^A-Za-z0-9]", " ");
        String[] wordList = sameCase.split("\\s+");
        return wordList;
    }

    private HashMap<String,Integer> createHashMapForWordCount (String txt){
        String[] wordList = createStringArray(txt);
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

        for (String word : wordList){
            int previousValue;
            if (wordCount.get(word) == null) {
                previousValue =0;
            }
            else {
                previousValue= wordCount.get(word);
            }
            int newValue= previousValue +1;

            wordCount.put(word,newValue);
        }

        return wordCount;
    }



    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {
        byte[] data = new byte[1024];
        int x;


        if (uri == null || format == null){ //This is just an error
            throw new IllegalArgumentException();
        }

        if (input == null) {  //When User does this, it deletes
            try {
                int deletedHash = (getDocumentAsTxt(uri).hashCode());
                deleteDocument(uri);
                return deletedHash;
            } catch (NullPointerException e) {//No such doc- return 0. Still should add command to stack

                Function<URI, Boolean> undoNothingThere = uri1 -> {
                    return true;
                };
                GenericCommand command2 = new GenericCommand(uri, undoNothingThere);
                stack.push(command2);
                return 0;
            }
        }


        this.liveDocs.add(uri);
        if (format == DocumentStore.DocumentFormat.TXT) {
            int textHash = putDocumentTxt(input, uri);
            return textHash;
        }
        else if (format == DocumentStore.DocumentFormat.PDF) {
            int textHash = putDocumentPDF(input, uri);
            return textHash;
        } else {
            throw new UnsupportedOperationException("Type must be either PDF or Txt");
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as a PDF, or null if no document exists with that URI
     */
    public byte[] getDocumentAsPdf(URI uri) {
        DocumentImpl doc = (DocumentImpl) this.bTree.get(uri);


        if (doc == null ){ //HashTable returns null of there is value stored there.
            return null;
        }


        byte[] pdfBytes =  doc.getDocumentAsPdf();

        doc.setLastUseTime(System.nanoTime());
        UriWithTime timeUri = new UriWithTime(uri);

         reHeapify(timeUri, doc);
        memoryManagement();
        return pdfBytes;

    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
     */
    public String getDocumentAsTxt(URI uri) {

        DocumentImpl doc = (DocumentImpl) this.bTree.get(uri);

        if (doc == null) {
            return null;
        }
        doc.setLastUseTime(System.nanoTime());
        UriWithTime timeUri = new UriWithTime(uri);
        reHeapify(timeUri, doc);
        memoryManagement();
        return doc.getDocumentAsTxt();
    }


    private void reHeapify (UriWithTime timeUri, DocumentImpl doc){
        try {
            minHeap.reHeapify(timeUri);
        } catch ( NullPointerException e){
            bTree.put(timeUri.getUri(), doc);
            addToTrie(doc.wordCount, timeUri.getUri());
            minHeap.insert(timeUri);
            minHeap.reHeapify(timeUri);
            addToLiveDocNumbersDoc(doc); //has to be after put
            this.liveDocs.add(timeUri.getUri());
            memoryManagement();
        }

    }

    /** * @return the Document object stored at that URI, or null if there is no such Document */
    protected Document getDocument(URI uri){

        if (!this.liveDocs.contains(uri)){
            return null;
        }

        Document doc = (Document) this.bTree.get(uri);
        if (doc == null) {
            return null;
        }
        else {
           //  doc.setLastUseTime(System.nanoTime());
          //   UriWithTime timeUri = new UriWithTime(uri);
          //   minHeap.reHeapify(timeUri);
            return doc;
        }
    }


    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
        DocumentImpl doc = (DocumentImpl) bTree.get(uri);

        if (doc == null) { //If no such doc to begin with
            return false;
        }

        Function<URI, Boolean> undoDelete = uri1 -> {
            UriWithTime timeUri = new UriWithTime(uri);
            this.bTree.put(uri1, doc);
            addToTrie(doc.wordCount, uri);
            doc.setLastUseTime(System.nanoTime());
            minHeap.insert(timeUri);
            memoryManagement();
            addToLiveDocNumbers(uri1);
            minHeap.reHeapify(timeUri);

            return true;
        };

        GenericCommand command = new GenericCommand(uri, undoDelete);
        stack.push(command);


        try{
            removeDocFromTrie(uri); //deletes from Trie
            removeFromLiveDocNumbers(uri);
            removeFromHeap(uri);
        } catch (NullPointerException e){

        }

        bTree.put(uri, null);
        bTree.put(uri, null);

        return true;
    }

    protected void removeDocFromTrie(URI uri){
        UriWithTime timeUri = new UriWithTime(uri);
        DocumentImpl doc = (DocumentImpl) getDocument(uri);
        for (String word: doc.wordCount.keySet()){
            trie.delete(word,timeUri);
        }
    }

    protected void removeDocFromTrieDoc(DocumentImpl doc){
        URI uri = doc.getKey();
        UriWithTime timeUri = new UriWithTime(uri);
        for (String word: doc.wordCount.keySet()){
            trie.delete(word,timeUri);
        }
    }


    private void removeFromHeap(URI uri){
        DocumentImpl doc = (DocumentImpl) getDocument(uri);
        doc.setLastUseTime(Long.MIN_VALUE);
        UriWithTime timeUri = new UriWithTime(uri);
        timeUri.setLastUsedTime(Long.MIN_VALUE);
       /// minHeap.reHeapify(timeUri);
        reHeapify(timeUri, doc);
        minHeap.removeMin(); //should be this deleted doc- bc time was just set to 0 (which is as small as it gets)
    }



    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException {
        if (stack.size() == 0){
            throw new IllegalStateException();
        }

        if (stack.peek() instanceof GenericCommand) {
            GenericCommand command = (GenericCommand) stack.peek();
            command.undo();
        }
        else{
            CommandSet cs = (CommandSet) stack.peek();
            cs.undo();
        }

        stack.pop();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI uri) throws IllegalStateException {
        if (stack.size() == 0){
            throw new IllegalStateException();
        }

        StackImpl tempStack = new StackImpl();

        int stackSize = stack.size();

        for (int x =0; x < stackSize; x++ ){

            if (!(stack.peek() instanceof CommandSet) ){
                GenericCommand gc = (GenericCommand) stack.peek();
                if (gc.getTarget().equals(uri)){
                    gc.undo();
                    stack.pop();
                    stack =refillOGStack(stack, tempStack);
                    return;
                }
            }
            else if (stack.peek() instanceof CommandSet){
                CommandSet cs = (CommandSet) stack.peek();
                if (cs.containsTarget(uri)){

                    cs.undo(uri);
                    if (cs.size() == 0){
                        stack.pop();
                        stack =refillOGStack(stack, tempStack);
                        return;
                    }

                    stack=refillOGStack(stack, tempStack);
                    return;
                }

            }
            tempStack.push(stack.pop());

            if (stack.size() ==0){
                throw new IllegalStateException();
            }
        }
    }


    private Stack refillOGStack(Stack oldStack, Stack tempStack){
        while (!(tempStack.size() == 0)){
            oldStack.push(tempStack.pop());
        }
        return oldStack;
    }


    //Search Stuff Below
    private class MyComparator implements Comparator<UriWithTime> {
        private String word;
        protected void setWord(String word){
            this.word = word;
        }
        protected String getWord(){
            return this.word;
        }


        @Override
        public int compare(UriWithTime o1, UriWithTime o2) {
            Document doc1 = (Document) bTree.get(o1.getUri());
            Document doc2 = (Document) bTree.get(o2.getUri());

            if ( doc1.wordCount(this.word) > doc2.wordCount(this.word) ) return -1;
            if (doc2.wordCount(getWord()) > doc1.wordCount(getWord())) return 1;
            else return 0;
        }
    }



    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<String> search(String keyword) {
        String upKeyword = keyword.toUpperCase();
        MyComparator comp = new MyComparator();
        comp.setWord(upKeyword);

        List<UriWithTime> uriList = trie.getAllSorted(upKeyword,comp);
        List<String> docText = new ArrayList<String>();

        Long time = System.nanoTime();
        for (UriWithTime uri : uriList){

            Document doc = (Document) bTree.get(uri.getUri());

            docText.add(doc.getDocumentAsTxt());
            doc.setLastUseTime(time);
            uri.setLastUsedTime(time);
           reHeapify(uri, (DocumentImpl) doc);
        }



        return docText;
    }

    /**
     * same logic as search, but returns the docs as PDFs instead of as Strings
     */
    public List<byte[]> searchPDFs(String keyword) {
        String upKeyword = keyword.toUpperCase();
        MyComparator comp = new MyComparator();
        comp.setWord(upKeyword);
        List<UriWithTime> uriList = trie.getAllSorted(upKeyword, comp);
        List<byte[]> docPdf = new ArrayList<byte[]>();

        for (UriWithTime uri : uriList){
            Document doc = (Document) bTree.get(uri.getUri());
            doc.setLastUseTime(System.nanoTime());
            docPdf.add(doc.getDocumentAsPdf());
            uri.setLastUsedTime(System.nanoTime());
            reHeapify(uri, (DocumentImpl) doc);
        }
        return docPdf;
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<String> searchByPrefix(String keywordPrefix) {
        String upPrefix = keywordPrefix.toUpperCase();
        PrefixComparator prefixComp = new PrefixComparator();

        prefixComp.setWord(keywordPrefix);
        List<UriWithTime> uriList = trie.getAllWithPrefixSorted(upPrefix,prefixComp);
        List<String> docText = new ArrayList<String>();


        for (UriWithTime uri : uriList){

            Document doc = (Document) bTree.get(uri.getUri());
            docText.add(doc.getDocumentAsTxt());
            doc.setLastUseTime(System.nanoTime());
            uri.setLastUsedTime(System.nanoTime());
            reHeapify(uri, (DocumentImpl) doc);
        }

        return docText;
    }

    /**
     * same logic as searchByPrefix, but returns the docs as PDFs instead of as Strings
     */
    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) {
        String upPrefix = keywordPrefix.toUpperCase();
        //MyComparator comp = new MyComparator();
        PrefixComparator prefixComp = new PrefixComparator();
        prefixComp.setWord(upPrefix);
        List<UriWithTime> uriList = trie.getAllWithPrefixSorted(upPrefix,prefixComp);
        List<byte[]> docPdf = new ArrayList<byte[]>();



        for (UriWithTime uri : uriList){
            Document doc = (Document) bTree.get(uri.getUri());
            docPdf.add(doc.getDocumentAsPdf());
            doc.setLastUseTime(System.nanoTime());
            uri.setLastUsedTime(System.nanoTime());
            reHeapify(uri, (DocumentImpl) doc);
        }
        return docPdf;
    }

    private int prefixCount(String prefix, Document doc){
    //    MyComparatorHashcode comparatorHashcode = new MyComparatorHashcode();
        int count =0;
        String text = doc.getDocumentAsTxt();
        String[] textArray = createStringArray(text);

        for (int x=0; x < textArray.length; x++) {
            if (textArray[x].startsWith(prefix)) {
                count++;
            }
        }

        return count;
    }

    private class PrefixComparator implements Comparator<UriWithTime>{
        private String prefix;
        protected void setWord(String prefix){
            this.prefix = prefix;
        }

        @Override
        public int compare(UriWithTime o1, UriWithTime o2) {
            Document doc1 = (Document) bTree.get(o1.getUri());
            Document doc2 = (Document) bTree.get(o2.getUri());



            if (prefixCount(prefix,doc1) > prefixCount(prefix,doc2) ) return -1;
            if (prefixCount(prefix,doc2) > prefixCount(prefix,doc1)) return 1;
            else return 0;
        }
    }


    /*
    private class MyComparatorHashcode implements Comparator<Document>{
        @Override
        public int compare(Document o1, Document o2) {
            if (o1.getDocumentTextHashCode() > o2.getDocumentTextHashCode()) return -1;
            if (o2.getDocumentTextHashCode() > o1.getDocumentTextHashCode()) return 1;
            else return 0;
        }
    }*/


    /**
     * Completely remove any trace of any document which contains the given keyword
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword) {
        String upKey = keyword.toUpperCase();
        MyComparator comp = new MyComparator();
        comp.setWord(upKey);
        Set<UriWithTime> uriSet= trie.deleteAll(upKey);
        Set<URI> URIs = new HashSet<URI>();
        CommandSet commandSet = new CommandSet();


        Long time = System.nanoTime();

        for (UriWithTime timeUri : uriSet){
            Document doc = (Document) bTree.get(timeUri.getUri());
            URIs.add(timeUri.getUri());
            removeFromLiveDocNumbers(timeUri.getUri());
            removeFromHeap(timeUri.getUri());
            removeDocFromTrieDoc((DocumentImpl) doc);
            this.bTree.put(timeUri.getUri(),null);
            this.bTree.put(timeUri.getUri(),null);
            this.liveDocs.remove(timeUri.getUri());


            Function<URI, Boolean> undoDeleteAll = uri1 -> {
                this.bTree.put(uri1, doc);  //includes the addition to the trie
                addToTrie(((DocumentImpl) doc).wordCount, timeUri.getUri());
                doc.setLastUseTime(time);
                addToLiveDocNumbersDoc((DocumentImpl) doc);
                this.liveDocs.add(uri1);
                minHeap.insert(timeUri);
                memoryManagement();
                minHeap.reHeapify(timeUri);
                return true;
            };

            GenericCommand command = new GenericCommand(timeUri.getUri(), undoDeleteAll);
            commandSet.addCommand(command);
        }

        stack.push(commandSet);

        return URIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        String upPrefix = keywordPrefix.toUpperCase();
        PrefixComparator comp = new PrefixComparator();
        Set<UriWithTime> uriSet =  trie.deleteAllWithPrefix(upPrefix);
        Set<URI> URIs = new HashSet<URI>();
        CommandSet commandSet = new CommandSet();

        for (UriWithTime timeUri : uriSet){

           DocumentImpl doc = (DocumentImpl) bTree.get(timeUri.getUri());
            removeDocFromTrie(timeUri.getUri());
            URIs.add(timeUri.getUri());
            removeDocFromTrie(timeUri.getUri());
            removeFromHeap(timeUri.getUri());
            removeFromLiveDocNumbers(timeUri.getUri());
            this.liveDocs.remove(timeUri.getUri());
            this.bTree.put(timeUri.getUri(),null);


            Function<URI, Boolean> undoDeleteAllWithPrefix = uri1 -> {
                this.bTree.put(uri1, doc); //includes the adding in the trie
                addToTrie(doc.wordCount, timeUri.getUri());
                doc.setLastUseTime(System.nanoTime());
                addToLiveDocNumbersDoc(doc);
                this.liveDocs.add(uri1);
                minHeap.insert(timeUri);
                memoryManagement();
                minHeap.reHeapify(timeUri);

                return true;
            };

            GenericCommand command = new GenericCommand(timeUri.getUri(), undoDeleteAllWithPrefix);
            commandSet.addCommand(command);
        }


        stack.push(commandSet);
        // CommandSet cs  = (CommandSet) stack.peek();

        return URIs;
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit
     */
    public void setMaxDocumentCount(int limit) {
        this.docLimit=limit;
        memoryManagement();

    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     *
     * @param limit
     */
    public void setMaxDocumentBytes(int limit) {
        this.byteLimit =limit;
        memoryManagement();
    }
}



