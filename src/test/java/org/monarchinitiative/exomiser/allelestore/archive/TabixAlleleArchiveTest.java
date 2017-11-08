package org.monarchinitiative.exomiser.allelestore.archive;

import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TabixAlleleArchiveTest {

    private final TabixAlleleArchive tabixArchive = new TabixAlleleArchive(Paths.get("tabixArchive"));

    @Test
    public void archiveFileFormat() throws Exception {
        assertThat(tabixArchive.getArchiveFileFormat(), equalTo("gz"));
    }

    @Test
    public void dataFileFormat() throws Exception {
        assertThat(tabixArchive.getDataFileFormat(), equalTo("vcf"));
    }
}