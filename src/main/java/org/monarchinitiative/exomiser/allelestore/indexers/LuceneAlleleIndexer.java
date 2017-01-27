package org.monarchinitiative.exomiser.allelestore.indexers;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;
import org.monarchinitiative.exomiser.allelestore.parsers.AlleleParser;
import org.monarchinitiative.exomiser.allelestore.parsers.ExomiserAlleleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Produces a lucene index from the alleles
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class LuceneAlleleIndexer implements AlleleIndexer {

    private static final Logger logger = LoggerFactory.getLogger(LuceneAlleleIndexer.class);

    private final Directory index;
    private final IndexWriter indexWriter;

    public LuceneAlleleIndexer(Path indexPath) {
        try {
            this.index = FSDirectory.open(indexPath);
            IndexWriterConfig indexWriterConfig = getDefaultConfig();
            this.indexWriter = createIndexWriter(index, indexWriterConfig);
        } catch (IOException e) {
            logger.error("Error opening index.", e);
            throw new RuntimeException();
        }
    }

    public LuceneAlleleIndexer(Directory index) {
        this.index = index;
        IndexWriterConfig indexWriterConfig = getDefaultConfig();
        this.indexWriter = createIndexWriter(index, indexWriterConfig);
    }

    IndexWriter getIndexWriter() {
        return indexWriter;
    }

    private IndexWriter createIndexWriter(Directory index, IndexWriterConfig indexWriterConfig) {
        try (IndexWriter indexWriter = new IndexWriter(index, indexWriterConfig)) {
            return indexWriter;
        } catch (IOException e) {
            logger.error("Error opening IndexWriter.", e);
        }
        return null;
    }

    private void closeIndexWriter() {
        try {
            indexWriter.close();
        } catch (IOException ex) {
            logger.error("Error closing IndexWriter.", ex);
        }
    }

    private IndexWriterConfig getDefaultConfig() {
        IndexWriterConfig config = new IndexWriterConfig(null);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        ((TieredMergePolicy) config.getMergePolicy()).setMaxMergedSegmentMB(5);
        config.setRAMBufferSizeMB(1024);
        return config;
    }

    public void buildIndex(Path alleleDataFile, Path indexPath) {
        AlleleParser alleleParser = new ExomiserAlleleParser();
        String line = null;
        long alleleCount = 0;
        try (BufferedReader br = Files.newBufferedReader(alleleDataFile)) {
            while ((line = br.readLine()) != null) {
                List<Allele> alleles = alleleParser.parseLine(line);
                for (Allele allele : alleles) {
                    alleleCount++;
                    writeAllele(allele);
                    if (alleleCount % 1000000 == 0) {
                        logger.info("Now on chr {} - added {} allele docs", allele.getChr(), alleleCount);
                        indexWriter.commit();
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("Could not parse line {}", line, ex);
        }
        logger.info("Finished Lucene indexing of {} alleles", alleleCount);
        closeIndexWriter();
    }

    void writeAllele(Allele allele) {
        Document alleleDoc = toAlleleDoc(allele);
        try {
            indexWriter.addDocument(alleleDoc);
        } catch (IOException ex) {
            logger.error("Unable to write allele {} to index", allele, ex);
        }
    }

    private Document toAlleleDoc(Allele allele) {
        Document doc = new Document();
        //these aren't stored in the index as we don't really need them.
        doc.add(new IntPoint("chr", allele.getChr()));
        doc.add(new IntPoint("pos", allele.getPos()));

        doc.add(new StringField("rsId", allele.getRsId(), Field.Store.YES));
        doc.add(new StringField("ref", allele.getRef(), Field.Store.NO));
        doc.add(new StringField("alt", allele.getAlt(), Field.Store.NO));

        for (Map.Entry<AlleleProperty, Float> entry : allele.getValues().entrySet()) {
            doc.add(new StoredField(entry.getKey().name(), entry.getValue()));
        }
        return doc;
    }

}
