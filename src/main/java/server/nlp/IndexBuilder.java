package server.nlp;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import server.log.LogUtil;

import java.io.File;

public class IndexBuilder {

    public enum TYPE {
        ios, android
    }

    public static final String INDEX_DIRECTORY_IOS = "data/lucene/ios_index";

    public static final String INDEX_DIRECTORY_ANDROID = "data/lucene/android_index";

    public static void addIndex(String description, String name, TYPE type) {

        File indexDir = null;

        switch (type) {
            case ios:
                indexDir = new File(INDEX_DIRECTORY_IOS);
                break;
            case android:
                indexDir = new File(INDEX_DIRECTORY_ANDROID);
                break;
        }

        // check if this id already exists
        Analyzer analyzer = new StandardAnalyzer();
        try {
            Directory fsDir = FSDirectory.open(indexDir);
            DirectoryReader reader = DirectoryReader.open(fsDir);
            IndexSearcher searcher = new IndexSearcher(reader);

            Directory directory = new SimpleFSDirectory(indexDir);
            IndexWriterConfig iwConf = new IndexWriterConfig(Version.LATEST,
                    analyzer);
            iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter iwriter = new IndexWriter(directory, iwConf);

            TermQuery nameFilter = new TermQuery(new Term("name", name));

            TopDocs hits = searcher.search(nameFilter, 2);
            ScoreDoc[] scoreDocs = hits.scoreDocs;

            if (scoreDocs.length == 1) {

                LogUtil.log(searcher.doc(scoreDocs[0].doc).get("name")
                        + "  found index..deleting it");
                iwriter.deleteDocuments(nameFilter);

            }

            LogUtil.log("Updating index...." + name);

            // Store the index in file

            Document d = new Document();
            d.add(new StringField("name", name, Field.Store.YES));
            d.add(new TextField("text", description, Field.Store.YES));
            iwriter.addDocument(d);

            iwriter.commit();
            iwriter.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public static double getIndexScore(String query, String name, TYPE type)
            throws Exception {

        double score = 0;

        File indexDir = null;

        switch (type) {
            case ios:
                indexDir = new File(INDEX_DIRECTORY_IOS);

                break;
            case android:
                indexDir = new File(INDEX_DIRECTORY_ANDROID);

                break;
        }

        int maxHits = 5;

        Directory fsDir = FSDirectory.open(indexDir);
        DirectoryReader reader = DirectoryReader.open(fsDir);
        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer stdAn = new StandardAnalyzer();

        //TermQuery nameFilter = new TermQuery(new Term("name", name));

        QueryParser parser = new QueryParser("text", stdAn);
        Query q = parser.parse(query);

        BooleanQuery booleanQuery = new BooleanQuery();
        //booleanQuery.add(nameFilter, BooleanClause.Occur.MUST);
        booleanQuery.add(q, BooleanClause.Occur.MUST);

        TopDocs hits = searcher.search(booleanQuery, maxHits);
        ScoreDoc[] scoreDocs = hits.scoreDocs;
        // LogUtil.log("hits=" + scoreDocs.length);
        // LogUtil.log("Hits (rank,score,docId)");

        if (scoreDocs.length >= 1) {
            for (int i = 0; i < scoreDocs.length; i++) {
                ScoreDoc sd = scoreDocs[i];
                score += sd.score;
            }
            score /= (double) scoreDocs.length;
        } else {

            LogUtil.log(name + "  __  " + query + " __ length : "
                    + scoreDocs.length);
            throw new Exception("query not found impossible!");
        }

        reader.close();

        return score;

    }

}
