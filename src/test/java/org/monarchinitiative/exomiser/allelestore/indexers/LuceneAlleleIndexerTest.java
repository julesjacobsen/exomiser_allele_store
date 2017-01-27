package org.monarchinitiative.exomiser.allelestore.indexers;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class LuceneAlleleIndexerTest {

    private Query buildAlleleQuery(Allele allele) {
        Query chrQuery = IntPoint.newExactQuery("chr", allele.getChr());
        Query posQuery = IntPoint.newExactQuery("pos", allele.getPos());
        TermQuery refQuery = new TermQuery(new Term("ref", allele.getRef()));
        TermQuery altQuery = new TermQuery(new Term("alt", allele.getAlt()));

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(chrQuery, BooleanClause.Occur.MUST);
        builder.add(posQuery, BooleanClause.Occur.MUST);
        builder.add(refQuery, BooleanClause.Occur.MUST);
        builder.add(altQuery, BooleanClause.Occur.MUST);
        return builder.build();
    }

    private List<Allele> makeAlleles() {
        List<Allele> alleles = new ArrayList<>();
        Allele one = new Allele(1, 12345, "A", "T");
        one.setRsId("rs12345");
        one.addValue(AlleleProperty.KG, 0.001f);
        alleles.add(one);

        Allele two = new Allele(1, 23456, "T", "G");
        two.setRsId(".");
        alleles.add(two);

        return alleles;
    }

    @Test
    public void testIndex() throws Exception {
        // 1. create the index
        Directory index = new RAMDirectory();
        LuceneAlleleIndexer instance = new LuceneAlleleIndexer(index);

        List<Allele> alleles = makeAlleles();

        for (Allele allele : alleles) {
            instance.writeAllele(allele);
        }

        instance.getIndexWriter().close();

        // 2. query
        Allele allele = alleles.get(0);
        Query query = buildAlleleQuery(allele);


        // 3. search
        int hitsPerPage = 1;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + docs.totalHits + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(allele.getChr() + "\t" + allele.getPos() + "\t" + allele.getRef() + "\t" + allele.getAlt() + "\t" + d
                    .get("rsId") + "\t" + d.get(AlleleProperty.KG.name()));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

}