package edu.yu.cs.com1320.project.stage5.impl;

import org.junit.After;
import org.junit.Before;

import java.io.*;
import java.net.URI;



import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.impl.Utils;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.delete;
import static org.junit.Assert.*;

    public class Stage5Tests {

        File baseDir =  new File("C:/Users/zacha/Documents/New Directory");

        //variables to hold possible values for doc1
        private URI uri1;
        private String txt1;
        private byte[] pdfData1;
        private String pdfTxt1;

        //variables to hold possible values for doc2
        private URI uri2;
        private String txt2;
        private byte[] pdfData2;
        private String pdfTxt2;

        //variables to hold possible values for doc3
        private URI uri3;
        private String txt3;
        private byte[] pdfData3;
        private String pdfTxt3;

        //variables to hold possible values for doc4
        private URI uri4;
        private String txt4;
        private byte[] pdfData4;
        private String pdfTxt4;
        private byte[] pdfData42;
        private String pdfTxt42;

        private int bytes1;
        private int bytes2;
        private int bytes3;
        private int bytes4;

        @Before
        public void init() throws Exception {
            //init possible values for doc1
            this.uri1 = new URI("ftp://edu.yu.cs/com1320/project/doc1");
            this.txt1 = "BLAH This is the text of doc1, in plain text. No fancy file format - just plain old String. blast Computer. Headphones.";
            this.pdfTxt1 = "Jab blah This is some PDF text for doc1, hat tip to Adobe. lakers kray";
            this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);

            //init possible values for doc2
            this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
            this.txt2 = "Text for doc2. A plain old String. zackster blast ";
            this.pdfTxt2 = "PDF content for doc2: PDF format was opened in 2008 josh. kray";
            this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);

            //init possible values for doc3
            this.uri3 = new URI("https://edu.yu.cs/com1320/project/doc3");
            this.txt3 = "bang This is the text of doc3";
            this.pdfTxt3 = "This is some PDF text for doc3, hat tip to Adobe.";
            this.pdfData3 = Utils.textToPdfData(this.pdfTxt3);

            //init possible values for doc4
            this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
            this.txt4 = "bang This is the text of doc4";
            this.pdfTxt4 = "This is some PDF text for doc4, which is open source.";
            this.pdfTxt42 = "asdfasfdf is some PDF text for doc4, which is open dsfhjioasfdiojs.";
            this.pdfData4 = Utils.textToPdfData(this.pdfTxt4);
            this.pdfData42 = Utils.textToPdfData(this.pdfTxt42);

            this.bytes1 = this.pdfTxt1.getBytes().length + this.pdfData1.length;
            this.bytes2 = this.pdfTxt2.getBytes().length + this.pdfData2.length;
            this.bytes3 = this.pdfTxt3.getBytes().length + this.pdfData3.length;
            this.bytes4 = this.pdfTxt4.getBytes().length + this.pdfData4.length;
        }


        @After
        public void removeAllFile() throws IOException {
            File directory = new File("C:/Users/zacha/Documents/New Directory/edu.yu.cs/com1320/project");
            if (directory.exists()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    Path path = Paths.get(file.getPath());
                    delete(path);
                }
            }
        }

       /* @Test
        public void cereal() throws IOException {
            DocumentStoreImpl store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

            Document doc1 = store.getDocument(uri1);
            File file = new File("C:/Users/zacha/Documents/New Directory"); //need to account if need to build new directory
            DocumentPersistenceManager dpm =new DocumentPersistenceManager(file);
            dpm.serialize(uri1, doc1);
        }



        @Test
        public void deCereal() throws IOException {
            File file = new File("C:/Users/zacha/Documents/New Directory"); //need to account if need to build new directory
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

            Document doc1 = store.getDocument(uri1);

            DocumentPersistenceManager dpm =new DocumentPersistenceManager(file);
            dpm.serialize(uri1, doc1);
            Document doc = dpm.deserialize(uri1);

            System.out.println(doc.getDocumentAsTxt());
            System.out.println(doc.wordCount("pdf"));


        }
*/


        @Test
        public void removedFromMemoryStillOnDisk(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);

            //1 sould no longer be in memory
            assertNull(store.getDocument(uri1));

            //should be on disk and should be retrieved
            assertEquals(this.pdfTxt1, store.getDocumentAsTxt(uri1));

            //now should be back in memory
            assertNotNull(store.getDocument(uri1));

            //doc2 should have been deleted
            assertNull(store.getDocument(uri2));
        }


        @Test
        public void deletingDocOnDisk(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);

            //1 sould no longer be in memory
            assertNull(store.getDocument(uri1));

            store.deleteDocument(uri1);

            //should have been deleted from disk as well
            assertNull(store.getDocumentAsTxt(uri1));

        }



        @Test
        public void searchWhenOneOnDisk() {
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

            //1, 2 should be on disk. 3 and 4 in memory
            ArrayList<String> test = new ArrayList<>();
            test.add(pdfTxt1);
            assertNull(store.getDocument(uri1));
            assertNull(store.getDocument(uri2));
            assertEquals(test, store.search("Jab"));//1
            //1 and 4 in memory. 2 and 3 on disk
            assertEquals(2, store.search("kray").size());// 1 and 1
            //1 and 1 in memory, 3 and 4 on disk
            assertNull(store.getDocument(uri3));
            assertNull(store.getDocument(uri4));

        }


        @Test
        public void prefixSearchWhenOnDisk(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);

            assertNull(store.getDocument(uri1));
            assertNull(store.getDocument(uri2));
            //should have 3 and 4 in memory, 1 and 2 on disk
            ArrayList<String> test = new ArrayList<>();
            test.add(this.txt2);
           // store.searchByPrefix("zack");
            assertEquals(test, store.searchByPrefix("zack")); //2
            //2 and 4 should now be in memory
            //1 and 3 on disk: yes
            assertEquals(2, store.searchByPrefix("blast").size()); //this has 1 and 2.

            //so 1 and 2 should now be in memory,
            //3 and 4 on disk
        }

        @Test
        public void searchWhenBothAreOnDisk(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt3.getBytes()), this.uri3, DocumentStore.DocumentFormat.TXT);
            store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT);


            assertNull(store.getDocument(uri1));
            assertNull(store.getDocument(uri2));

            //should have 3 and 4 in memory, 1 and 2 on disk

            ArrayList<String> test = new ArrayList<>();
            test.add(this.txt2);

            store.searchByPrefix("zack"); // this is doc 2

            assertEquals(2, store.search("bang").size()); //this has 1 and 3
            //so 1 and 2 should now be in memory,
            //2 and 4 on disk
        }


        @Test
        public void deleteNotOnDisk(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT);

            store.deleteDocument(uri1);
            assertNull(store.getDocumentAsTxt(uri1));
        }


        @Test
        public void  diskGetAsPDF(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(3);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
         //4 should be on disk

            assertNull(store.getDocument(uri4));
            assertEquals(this.pdfTxt4,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri4)));
            assertNull(store.getDocument(uri1));
        }



        @Test
        public void  overwriteDisk(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(3);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            //4 should be on disk
            assertNull(store.getDocument(uri4));
            store.putDocument(new ByteArrayInputStream(this.pdfData42), this.uri4, DocumentStore.DocumentFormat.PDF);
            assertNotNull(store.getDocument(uri4));

        }


        @Test
        public void baseDirDeleteDirectories(){
          //  File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl();
            store.setMaxDocumentCount(1);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);

            String schemeSpecificPart = uri1.getSchemeSpecificPart();
            String removedFileName = schemeSpecificPart.substring(0, schemeSpecificPart.lastIndexOf('/'));
            File file = new File(System.getProperty("user.dir") + removedFileName);

            assertNotNull(file);

            store.deleteDocument(uri1);
            store.deleteDocument(uri2);
            store.deleteDocument(uri4);

            assertEquals(false, file.exists());

        }


        @Test
        public void searchForPDFs(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);

            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

            //3 and 4 in memory, 1 and 2 on disk
            ArrayList<byte[]> test = new ArrayList();
            store.searchPDFs("jab");
            // 1 and 4 in memory , 2 and 3 on disk

            assertNull(store.getDocument(uri2));
            assertNull(store.getDocument(uri3));


        }


        @Test
        public void searchForPDFsPrefix() {
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
            //3 and 4 in memory, 1 and 2 on disk
            store.searchPDFsByPrefix("lake");
            // 1 and 4 in memory , 2 and 3 on disk
            assertNull(store.getDocument(uri2));
            assertNull(store.getDocument(uri3));
        }


        @Test
        public void getDocumentMemoryManagaement(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

            //should be 1/2 on disk 3/4 memory

            store.getDocumentAsTxt(uri2);

            //should be 1/3 on disk
            //2/4 in memory

            assertNull(store.getDocument(uri1));
            assertNull(store.getDocument(uri3));
            assertNotNull(store.getDocument(uri2));
            assertNotNull(store.getDocument(uri4));


        }

        @Test
        public void getDocumentMemoryManagaementPDF(){
            File file = new File("C:/Users/zacha/Documents/New Directory");
            DocumentStoreImpl store = new DocumentStoreImpl(file);
            store.setMaxDocumentCount(2);
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

            //should be 1/2 on disk 3/4 memory

            store.getDocumentAsPdf(uri1);
            //should be 2/3 on disk
            //1/4 in memory

            assertNull(store.getDocument(uri2));
            assertNull(store.getDocument(uri3));
            assertNotNull(store.getDocument(uri1));
            assertNotNull(store.getDocument(uri4));



        }






    }
