import java.io.FileReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GenerateIndex {
    public static void main(String[] args) throws Exception {
        new GenerateIndex("movies.json", "./INDEX");
    }

    public GenerateIndex(String jsonData, String indexDir) throws Exception {
        Path indexPath = Files.createDirectories(Paths.get(indexDir));
        Directory directory = FSDirectory.open(indexPath);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(new BM25Similarity(1.2f, 0.75f, false));
        IndexWriter iwriter = new IndexWriter(directory, config);

        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(jsonData);
        JSONArray array = (JSONArray) parser.parse(reader);

        for (int i = 0; i < array.size(); i++) {
            JSONObject movie = (JSONObject) array.get(i);

            String name = (String) movie.get("name");
            Long year = (Long) movie.get("year");
            String description = (String) movie.get("description");

            Document doc = new Document();
            doc.add(new Field("NAME", name, TextField.TYPE_STORED));
            doc.add(new Field("YEAR", year.toString(), TextField.TYPE_STORED));
            doc.add(new Field("DESCRIPTION", description, TextField.TYPE_STORED));
            iwriter.addDocument(doc);
        }

        iwriter.close();
        directory.close();
    }
}
