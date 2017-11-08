package org.monarchinitiative.exomiser.allelestore;

import org.junit.Test;
import org.monarchinitiative.exomiser.allelestore.archive.AlleleArchive;
import org.monarchinitiative.exomiser.allelestore.archive.TabixAlleleArchive;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.parsers.DbSnpAlleleParser;
import org.monarchinitiative.exomiser.allelestore.writers.AlleleWriter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleArchiveProcessorTest {

    @Test
    public void process() throws Exception {
        AlleleArchive dbsnpArchive = new TabixAlleleArchive(Paths.get("src/test/resources/test_first_ten_dbsnp.vcf.gz"));
        AlleleArchiveProcessor instance = new AlleleArchiveProcessor(dbsnpArchive, new DbSnpAlleleParser());

        TestAlleleWriter testAlleleWriter = new TestAlleleWriter();
        instance.process(testAlleleWriter);

        assertThat(testAlleleWriter.count(), equalTo(10L));
        testAlleleWriter.getAlleles().forEach(System.out::println);
    }

    private class TestAlleleWriter implements AlleleWriter {

        private final List<Allele> alleles = new ArrayList<>();

        public List<Allele> getAlleles() {
            return alleles;
        }

        @Override
        public void write(Allele allele) {
            alleles.add(allele);
        }

        @Override
        public long count() {
            return alleles.size();
        }
    }
}