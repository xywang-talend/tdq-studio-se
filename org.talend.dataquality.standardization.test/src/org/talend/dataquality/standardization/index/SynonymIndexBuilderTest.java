// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.standardization.index;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * DOC scorreia class global comment. Detailled comment
 */
public class SynonymIndexBuilderTest {

    // The abosolute path will be "path/to/svn/top/org.talend.dataquality.standardization.test/data/index
    private static String path = "data/index";

    private IndexSearcher searcher;

    private static SynonymAnalyzer synonymAnalyzer;

    private static String[][] synonyms = { { "I.B.M.", "IBM|International Business Machines|Big Blue" },
            { "ANPE", "A.N.P.E.|Agence Nationale Pour l'Emploi|Pôle Emploi" }, { "Sécurité Sociale", "Sécu|SS|CPAM" },
            { "IAIDQ", "International Association for Information & Data Quality|Int. Assoc. Info & DQ" }, };

    private static boolean useQueryParser = true;

    private static boolean useAllDocCollector = false;

    private static boolean useMemeryForIndex = false;

    private SynonymIndexBuilder builder;

    @Before
    public void setUp() throws Exception {
        initOnceForAll();
    }

    private void initOnceForAll() throws IOException {

        builder = new SynonymIndexBuilder();
        builder.initIndexInFS(path);
        // builder.initIndexInRAM();
        builder.setSynonymSeparator('|');

        // synonymAnalyzer = new SynonymAnalyzer();

    }

    @After
    public void closeIndex() throws Exception {
        builder.closeIndex();
    }

    @Test
    public void testInsertDocuments() throws Exception {
        System.out.println("---------------Test insertDocuments--------------");
        builder.setUsingCreateMode(true);
        for (String[] syns : synonyms) {
            builder.insertDocument(syns[0], syns[1]);
        }
        builder.setUsingCreateMode(false);
        assertEquals(synonyms.length, builder.getWriter().numDocs());
    }

    @Test
    public void testInsertSynonymDocument() throws IOException {
        System.out.println("\n---------------Test addDocument------------------");

        builder.insertDocument("ADD", "This|is|a|new|document");
        assertEquals(synonyms.length + 1, builder.getWriter().numDocs());
        builder.insertDocument("I.B.M.", "This|is|an|existing|document");
        assertEquals(synonyms.length + 1, builder.getWriter().numDocs());

    }

    @Test
    public void testUpdateSynonymDocument() throws IOException {
        System.out.println("\n---------------Test updateDocument---------------");
        int updateCount = 0;
        updateCount += builder.updateDocument("Sécurité Sociale", "I|have|been|updated");
        assertEquals(1, updateCount);
        updateCount += builder.updateDocument("INEXIST", "I|don't|exist");
        assertEquals(1, updateCount);
    }

    @Test
    public void testDeleteSynonymDocument() throws IOException {
        System.out.println("\n---------------Test deleteDocument---------------");
        int docCount = builder.getWriter().numDocs();

        assertEquals(1, builder.searchDocumentByWord("I.B.M.").totalHits);
        builder.deleteDocumentByWord("I.B.M.");

        assertEquals(docCount - 1, builder.getWriter().numDocs());

        builder.deleteDocumentByWord("random");
        assertEquals(docCount - 1, builder.getWriter().numDocs());

    }

    @Test
    public void testAddSynonymToWord() throws IOException {

        System.out.println("\n---------------Test addSynonymToWord-------------");
        assertEquals(0, builder.searchDocumentBySynonym("another").totalHits);

        int synonymCount = builder.getSynonymCount("anpe");
        builder.addSynonymToDocument("ANPE", "I am another synonym of ANPE");
        assertEquals(1, builder.searchDocumentBySynonym("another").totalHits);

        builder.addSynonymToDocument("ANPE", "Anpe");
        assertEquals(synonymCount + 1, builder.getSynonymCount("anpe"));

        builder.addSynonymToDocument("ANPEEEE", "A.N.P.E");
        assertEquals(0, builder.searchDocumentByWord("ANPEEEE").totalHits);

    }

    @Test
    public void testRemoveSynonymFromWord() throws IOException {
        System.out.println("\n------------Test removeSynonymFromWord-----------");

        int synonymCount = builder.getSynonymCount("ANPE");
        // the synonym to delete should be precise and case sensitive
        builder.removeSynonymFromDocument("ANPE", "a.n.p.e.");
        assertEquals(synonymCount, builder.getSynonymCount("ANPE"));

        builder.removeSynonymFromDocument("ANPE", "A.N.P.E.");
        assertEquals(--synonymCount, builder.getSynonymCount("ANPE"));

        builder.removeSynonymFromDocument("ANPE", "Pôle Emploi");
        assertEquals(--synonymCount, builder.getSynonymCount("ANPE"));

    }

    @Test
    public void testSearchDocumentBySynonym() throws IOException {
        System.out.println("\n-----------Test searchDocumentBySynonym----------");
        TopDocs docs = builder.searchDocumentBySynonym("i");
        System.out.println(docs.totalHits + " documents found.");
        for (int i = 0; i < docs.totalHits; i++) {
            System.out.print(docs.scoreDocs[i]);
            Document doc = builder.getSearcher().doc(docs.scoreDocs[i].doc);
            System.out.println(Arrays.toString(doc.getValues("syn")));
        }

        assertEquals(2, docs.totalHits);
    }

    // @Test
    public void testDeleteAll() throws IOException {
        builder.deleteAllDocuments();
        assertEquals(0, builder.getWriter().numDocs());
    }

    /**
     * DOC scorreia Comment method "search".
     * 
     * @param str
     */
    // private void search(String str) {
    // try {
    // Directory index = useMemeryForIndex ? builder.getIndexDir() : FSDirectory.open(new File(path));
    //
    // searcher = new IndexSearcher(index);
    //
    // // Query query = new QueryParser(Version.LUCENE_30, "syn", synonymAnalyzer).parse("\"" + str + "\"");
    // Query query = useQueryParser ? new QueryParser(Version.LUCENE_30, "syn", synonymAnalyzer).parse(str)
    // : new PhraseQuery();
    //
    // if (!useQueryParser) {
    // ((PhraseQuery) query).add(new Term("syn", str));
    // }
    //
    // List<ScoreDoc> scoreDocs = new ArrayList<ScoreDoc>();
    // if (useAllDocCollector) {
    // AllDocCollector collector = new AllDocCollector();
    // searcher.search(query, collector);
    // scoreDocs = collector.getHits();
    // } else {
    // TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
    // searcher.search(query, collector);
    // scoreDocs = Arrays.asList(collector.topDocs().scoreDocs);
    // }
    // if (scoreDocs.isEmpty()) {
    // fail("No doc found for " + str);
    // } else {
    // System.out.println("Got match for " + str);
    // for (ScoreDoc hits : scoreDocs) {
    // Document doc = searcher.doc(hits.doc);
    // String[] entry = doc.getValues("word");
    // for (String string : entry) {
    // System.out.println("entry=" + string);
    // }
    // String[] values = doc.getValues("syn");
    // for (String string : values) {
    // System.out.println("syn=" + string);
    // }
    // System.out.println();
    // }
    //
    // }
    // searcher.close();
    // index.close();
    // } catch (CorruptIndexException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (ParseException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

}
