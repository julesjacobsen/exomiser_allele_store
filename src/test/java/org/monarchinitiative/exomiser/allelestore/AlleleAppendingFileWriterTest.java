package org.monarchinitiative.exomiser.allelestore;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;
import org.monarchinitiative.exomiser.allelestore.parsers.ExomiserAlleleParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleAppendingFileWriterTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @NotNull
    private AlleleAppendingFileWriter getInstanceWithSavedAlleles(Iterable<Allele> alleles) throws IOException {
        AlleleAppendingFileWriter instance = new AlleleAppendingFileWriter(tmpFolder.newFolder().toPath());
        alleles.forEach(instance::save);
        return instance;
    }

    private AlleleAppendingFileWriter getInstanceWithSavedAlleles(Allele... alleles) throws IOException {
        return getInstanceWithSavedAlleles(Arrays.asList(alleles));
    }

    @Test
    public void testAlleleWithUnknownChromosomeNumber() throws Exception {
        Allele allele = new Allele(Integer.MAX_VALUE, 234565, "A", "T");
        AlleleAppendingFileWriter instance = getInstanceWithSavedAlleles(allele);
        assertThat(instance.count(), equalTo(0L));
    }

    @Test
    public void testCountDifferentAlleles() throws Exception {

        Allele allele0 = new Allele(2, 234578, "T", "G");
        Allele allele1 = new Allele(2, 234577, "T", "G");
        Allele allele2 = new Allele(2, 234577, "T", "C");

        AlleleAppendingFileWriter instance = getInstanceWithSavedAlleles(allele0, allele2, allele1);

        assertThat(instance.count(), equalTo(3L));
    }

    @Test
    public void testCountAllelesCountsAllSavedAlleles() throws Exception {

        Allele allele0 = new Allele(2, 234577, "T", "G");
        Allele allele1 = new Allele(2, 234577, "T", "G");
        Allele allele2 = new Allele(2, 234577, "T", "G");

        AlleleAppendingFileWriter instance = getInstanceWithSavedAlleles(allele0, allele2, allele1);

        assertThat(instance.count(), equalTo(3L));
    }

    @Test
    public void testToLine() throws Exception {
        Allele allele = new Allele(1, 234565, "A", "T");
        AlleleAppendingFileWriter instance = getInstanceWithSavedAlleles(allele);

        Path outFile = instance.mergeToFile("results.allele");
        assertThat(Files.exists(outFile), is(true));

        List<String> lines = Files.readAllLines(outFile);
        lines.forEach(System.out::println);
        assertThat(lines.size(), equalTo(1));
        //we expect that the alleles are ordered by chromosome, position, ref, alt
        assertThat(lines.get(0), equalTo("1\t234565\t.\tA\tT\t.\t.\t."));

        ExomiserAlleleParser parser = new ExomiserAlleleParser();
        List<Allele> out = parser.parseLine(lines.get(0));
        assertThat(out.size(), equalTo(1));
        assertThat(out.get(0), equalTo(allele));
    }


    @Test
    public void testMergeToFileSortsAlleles() throws Exception {

        Allele allele2 = new Allele(2, 234578, "T", "G");
        Allele allele3 = new Allele(2, 234577, "T", "G");
        Allele allele4 = new Allele(2, 234577, "T", "C");

        List<Allele> alleles = Arrays.asList(allele2, allele3, allele4);
        //save them OUT OF ORDER - the order should not matter for input
        Collections.shuffle(alleles);
        AlleleAppendingFileWriter instance = getInstanceWithSavedAlleles(alleles);

        Path outFile = instance.mergeToFile("results.allele");
        assertThat(Files.exists(outFile), is(true));

        List<String> lines = Files.readAllLines(outFile);
        lines.forEach(System.out::println);
        assertThat(lines.size(), equalTo(3));
        //we expect that the alleles are ordered by chromosome, position, ref, alt
        assertThat(lines.get(0), equalTo("2\t234577\t.\tT\tC\t.\t.\t."));
        assertThat(lines.get(1), equalTo("2\t234577\t.\tT\tG\t.\t.\t."));
        assertThat(lines.get(2), equalTo("2\t234578\t.\tT\tG\t.\t.\t."));

    }

    @Test
    public void testMergeToFileMergesInfoFieldWhenAlleleIsEqual() throws Exception {
        Allele allele = new Allele(1, 234565, "A", "T");
        allele.addValue(AlleleProperty.KG, 0.0123f);

        Allele allele1 = new Allele(1, 234565, "A", "T");
        allele1.addValue(AlleleProperty.ESP_AA, 0.0456f);

        AlleleAppendingFileWriter instance = getInstanceWithSavedAlleles(allele1, allele);

        Path outFile = instance.mergeToFile("results.allele");
        assertThat(Files.exists(outFile), is(true));

        List<String> lines = Files.readAllLines(outFile);
        lines.forEach(System.out::println);
        assertThat(lines.size(), equalTo(1));
        //we expect that the allele and allele1 have been merged
        assertThat(lines.get(0), equalTo("1\t234565\t.\tA\tT\t.\t.\tKG=0.0123;ESP_AA=0.0456"));
    }

}