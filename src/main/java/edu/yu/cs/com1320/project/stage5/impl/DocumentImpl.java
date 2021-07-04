package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DocumentImpl implements Document {

    HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    URI uri;
    String txt;
    int txtHash;
    byte[] pdfBytes;
    Long lastUsedTime;

    public DocumentImpl(URI uri, String txt, int txtHash){
        this.uri=uri;
        this.txt=txt;
        this.txtHash=txtHash;
        HashMap wordCount = createWordMap(txt);
        setWordMap(wordCount);
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
        this.uri=uri;
        this.txt=txt;
        this.txtHash=txtHash;
        this.pdfBytes=pdfBytes;
        HashMap wordCount = createWordMap(txt);
        setWordMap(wordCount);
    }


    private HashMap<String,Integer> createWordMap(String text){
        String[] wordList = createStringArray(text);
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


    private String [] createStringArray (String txt){
        String sameCase = txt.toUpperCase();//case INSensitive
        sameCase= sameCase.replaceAll("[^A-Za-z0-9]", " ");
        String[] wordList = sameCase.split("\\s+");
        return wordList;
    }

    /**
     * @return the document as a PDF
     */
    @Override
    public byte[] getDocumentAsPdf(){
        PDDocument pdf;
        if (this.pdfBytes != null) {
            return this.pdfBytes;
        }
        else {
            pdf = createPDFFromText(this.txt);
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdf.save(out);
            pdf.close();
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            pdfBytes =getAsByteArray(in);
        } catch (IOException z) {
            throw new UnsupportedOperationException("There was an IO Exception");
        }
        this.pdfBytes =pdfBytes;
        return this.pdfBytes;
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

    private PDDocument createPDFFromText(String text) {
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage();
        pdf.addPage(page);
        PDFont font = PDType1Font.HELVETICA_BOLD;

        try {
            PDPageContentStream contents = new PDPageContentStream(pdf, page);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(text);
            contents.endText();
            contents.close();
            pdf.save("myPDF");
            // pdf.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("There was an IO Exception");
        }
        return pdf;
    }

    /**
     * @return the document as a Plain String
     */
    @Override
    public String getDocumentAsTxt(){
        return txt;
    }


    /**
     * @return hash code of the plain text version of the document
     */
    @Override
    public int getDocumentTextHashCode() {
        return txtHash;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return this.uri;
    }

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document
     */
    @Override
    public int wordCount(String word) {
        String upWord = word.toUpperCase();
        int number;
        try {
             number = this.wordCount.get(upWord);
        } catch (NullPointerException e){
            number =0;
        }

        return  number;
    }
    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    public long getLastUseTime(){
        return this.lastUsedTime;

    }

    public void setLastUseTime(long timeInNanoseconds){
        this.lastUsedTime = timeInNanoseconds;
    }


    /**
     * @return a copy of the word to count map so it can be serialized
     */
   public Map<String,Integer> getWordMap(){
       return this.wordCount;
   }

    /**
     * This must set the word to count map during deserialization
     * @param wordMap
     */
    public void setWordMap(Map<String,Integer> wordMap){
        this.wordCount = (HashMap<String, Integer>) wordMap;
    }

    @Override
    public int compareTo(Document o) {
        if (this.lastUsedTime >= o.getLastUseTime()) return 1;
        if (this.lastUsedTime < o.getLastUseTime()) return -1;
        return 0;
    }



}
