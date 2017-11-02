package org.monarchinitiative.exomiser.allelestore.indexers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.parsers.AlleleParser;
import org.monarchinitiative.exomiser.allelestore.parsers.ExomiserAlleleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractAlleleIndexer implements AlleleIndexer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAlleleIndexer.class);

    @Override
    public void buildIndex(Path alleleDataFile, Path indexPath) {
        logger.info("Reading index from {}", alleleDataFile);
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
                        logger.info("Indexing chr {} - added {} allele docs", allele.getChr(), alleleCount);
                        commit();
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("Could not parse line {}", line, ex);
        }
        logger.info("Finished indexing of {} alleles", alleleCount);
        close();
    }

    protected abstract void writeAllele(Allele allele);

    protected abstract void commit();

    protected abstract void close();
}
