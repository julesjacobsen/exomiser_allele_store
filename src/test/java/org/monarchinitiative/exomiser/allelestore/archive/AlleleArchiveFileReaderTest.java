package org.monarchinitiative.exomiser.allelestore.archive;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleArchiveFileReaderTest {

    private final TabixAlleleArchive archive = new TabixAlleleArchive(Paths.get("src/test/resources/test_empty.vcf.gz"));

    @Test
    public void getFileObjects() throws Exception {
        ArchiveFileReader instance = new ArchiveFileReader(archive);
        List<FileObject> vcfFiles = instance.getFileObjects();
        assertThat(vcfFiles.size(), equalTo(1));
    }

    @Test
    public void readFileObject() throws Exception {
        ArchiveFileReader instance = new ArchiveFileReader(archive);
        List<FileObject> vcfFiles = instance.getFileObjects();
        FileObject vcfFile = vcfFiles.get(0);
        Stream<String> stringStream = new BufferedReader(new InputStreamReader(instance.readFileObject(vcfFile))).lines();
        assertThat(stringStream.count(), equalTo(0L));
    }

    @Test
    public void test() throws Exception {
        TabixAlleleArchive archive = new TabixAlleleArchive(Paths.get("C:/Users/hhx640/Documents/exomiser-build/data/download/00-All.vcf.gz"));
        ArchiveFileReader instance = new ArchiveFileReader(archive);
        List<FileObject> vcfFiles = instance.getFileObjects();
        FileObject vcfFile = vcfFiles.get(0);
        Stream<String> stringStream = new BufferedReader(new InputStreamReader(instance.readFileObject(vcfFile))).lines();
        stringStream.filter(string -> !string.startsWith("#")).limit(100).forEach(System.out::println);
    }

}