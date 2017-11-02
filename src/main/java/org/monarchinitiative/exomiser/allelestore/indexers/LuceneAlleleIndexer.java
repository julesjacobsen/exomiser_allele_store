package org.monarchinitiative.exomiser.allelestore.indexers;

import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Produces a lucene index from the alleles
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class LuceneAlleleIndexer extends AbstractAlleleIndexer {

    private static final Logger logger = LoggerFactory.getLogger(LuceneAlleleIndexer.class);

    private final IndexWriter indexWriter;

    public LuceneAlleleIndexer(Path indexPath) {
        try {
            FSDirectory directory = openDirectory(indexPath);
            this.indexWriter = new IndexWriter(directory, defaultIndexWriterConfig());
        } catch (IOException e) {
            logger.error("Error opening index.", e);
            throw new RuntimeException();
        }
    }

    @NotNull
    private FSDirectory openDirectory(Path indexPath) throws IOException {
        return FSDirectory.open(indexPath);
    }

    LuceneAlleleIndexer(Directory directory) {
        try {
            this.indexWriter = new IndexWriter(directory, defaultIndexWriterConfig());
        } catch (IOException e) {
            logger.error("Error opening index.", e);
            throw new RuntimeException();
        }
    }

    private static IndexWriterConfig defaultIndexWriterConfig() {
        IndexWriterConfig config = new IndexWriterConfig(null);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        ((TieredMergePolicy) config.getMergePolicy()).setMaxMergedSegmentMB(5);
        config.setRAMBufferSizeMB(1024);
        return config;
    }

    protected void writeAllele(Allele allele) {
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

    IndexWriter getIndexWriter() {
        return indexWriter;
    }

    @Override
    protected void commit() {
        try {
            indexWriter.commit();
        } catch (IOException e) {
            logger.error("Error committing IndexWriter.", e);
        }
    }

    protected void close() {
        try {
            indexWriter.close();
        } catch (IOException ex) {
            logger.error("Error closing IndexWriter.", ex);
        }
    }

}
