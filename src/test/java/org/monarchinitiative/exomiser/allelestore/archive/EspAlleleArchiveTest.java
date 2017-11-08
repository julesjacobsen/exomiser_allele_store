package org.monarchinitiative.exomiser.allelestore.archive;

import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspAlleleArchiveTest {

    private final EspAlleleArchive espArchive = new EspAlleleArchive(Paths.get("file"));

    @Test
    public void archiveFileFormat() throws Exception {
        assertThat(espArchive.getArchiveFileFormat(), equalTo("tgz"));
    }

    @Test
    public void dataFileFormat() throws Exception {
        assertThat(espArchive.getDataFileFormat(), equalTo("vcf"));
    }
}