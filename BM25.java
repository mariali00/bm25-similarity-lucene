import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;



public class BM25 {
    public static void main(String[] args) throws Exception {
        BM25 service = new BM25();
        var r = service.bm25Search("./INDEX", "negro", 10);
        for (int i = 0; i < r.size(); i++)
            System.out.println(r.get(i).y + ": " + r.get(i).x);
    }

    public List<Tuple<String, String>> bm25Search(String indexDir, String queryText, int topK) {
        try {
            return runSearch(indexDir, queryText, topK);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Tuple<String, String>> runSearch(String indexDir, String queryText, int topK)
            throws IOException, ParseException {
        Path indexPath = Paths.get(indexDir);
        Directory directory = FSDirectory.open(indexPath);
        DirectoryReader ireader = DirectoryReader.open(directory);

        IndexSearcher indexSearcher = new IndexSearcher(ireader);
        indexSearcher.setSimilarity(new BM25Similarity(1.2f, 0.75f, false));
        BooleanQuery.setMaxClauseCount(4096);
        Analyzer analyzer = new StandardAnalyzer();
        // QueryParser parser = new QueryParser("DESCRIPTION", analyzer);
        MultiFieldQueryParser parser = 
            new MultiFieldQueryParser(new String[] { "NAME"}, analyzer);

        
        String queryTextEscaped = QueryParser.escape(queryText);
        Query query = parser.parse(queryTextEscaped);

        List<Tuple<String, String>> topKPassIds = runLuceneExpr(indexSearcher, query, topK);
        return topKPassIds;
    }

    private List<Tuple<String, String>> runLuceneExpr(IndexSearcher idxSearcher, Query query, int topK)
            throws IOException {
        ScoreDoc[] hits = idxSearcher.search(query, topK).scoreDocs;
        List<Tuple<String, String>> topKPass = new ArrayList<>();
        if (hits != null) {
            for (ScoreDoc hit : hits) {
                org.apache.lucene.document.Document hitDoc = idxSearcher.doc(hit.doc);
                topKPass.add(new Tuple<String, String>(hitDoc.get("NAME"), hitDoc.get("YEAR")));
            }
        }

        return topKPass;
    }
    class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
};
}