package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.stream.JsonReader;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.delete;


/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    protected File baseDir;

    public DocumentPersistenceManager(){
        this.baseDir= new File(System.getProperty("user.dir"));;
    }

    public DocumentPersistenceManager(File baseDir){
        if (baseDir == null){
            this.baseDir= new File(System.getProperty("user.dir"));;
        }else{
            this.baseDir= baseDir;
        }
    }


    @Override
    public void serialize(URI uri, Document val) throws IOException {
        JsonSerializer<Document> customSerializer = (document, type, jsonSerializationContext) -> {
            JsonObject jsonDerulo = new JsonObject(); //JAAYSON DERULOOOO
            //String text = document.getDocumentAsTxt().replaceAll(" ", "_");
            jsonDerulo.addProperty("text", document.getDocumentAsTxt());
            jsonDerulo.addProperty("uri", uri.toString());
            jsonDerulo.addProperty("hashcode", document.getDocumentTextHashCode());
            Gson gson = new Gson();
            String hashMap = gson.toJson(document.getWordMap());
            jsonDerulo.addProperty("wordMap", hashMap);
            return jsonDerulo;
        };

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Document.class, customSerializer).setPrettyPrinting();
        Gson customGson = gsonBuilder.create();
        String customJson = customGson.toJson(val, Document.class);

        String schemeSpecificPart = uri.getSchemeSpecificPart();
        String removedFileName = schemeSpecificPart.substring(0, schemeSpecificPart.lastIndexOf('/'));
        File directories = new File (this.baseDir.toString() + removedFileName);
        directories.mkdirs();
        File file = new File (this.baseDir.toString() + uri.getSchemeSpecificPart() + ".json");

        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printer = new PrintWriter(fileWriter);
        printer.print(customJson);
        printer.close();
        fileWriter.close();

    }


    @Override
    public Document deserialize(URI uri) throws IOException {

        JsonDeserializer customDeserializer = (jsonElement, type, jsonDeserializationContext) -> {
            JsonObject jsonDerulo = jsonElement.getAsJsonObject();
            String  uriString = jsonDerulo.get("uri").getAsString();
            try {
                URI uri1 = new URI(uriString);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            int hashCode = jsonDerulo.get("hashcode").getAsInt();
            String text = jsonDerulo.get("text").getAsString();
            Document doc = new DocumentImpl (uri, text, hashCode);
            byte[] pdfBytes = doc.getDocumentAsPdf();
            Document docWithPdf = new DocumentImpl (uri, text, hashCode, pdfBytes);
            return docWithPdf;
        };


        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Document.class, customDeserializer);
        Gson customGson =gsonBuilder.create();

        BufferedReader br = new BufferedReader(new FileReader(this.baseDir.toString() + uri.getSchemeSpecificPart() + ".json"));
        br.close();

        File file = new File (this.baseDir.toString() + uri.getSchemeSpecificPart() + ".json");
        JsonReader reader = new JsonReader(new FileReader(file));
        Document doc = customGson.fromJson(reader, Document.class);
        Path path = Paths.get(file.getPath());
       // System.out.println(doc.getDocumentAsTxt());
        reader.close();
       // delete(path);
        deleteEntirePath(path);
       // System.out.println(x);
        //also have to delete all directories


        return doc;
    }

    private void deleteEntirePath(Path path) throws IOException {
        delete(path);
        File parentDirectory = new File (String.valueOf(path.getParent()));
         if (parentDirectory.isDirectory()){
             if (parentDirectory.list().length >0){
                 return;
             }
             else{
                 deleteEntirePath(path.getParent());
             }

         }
    }
}
