package org.monarchinitiative.exomiser.allelestore.archive;

import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfpAlleleArchiveTest {

    private final DbNsfpAlleleArchive dbNsfpArchive = new DbNsfpAlleleArchive(Paths.get("file"));

    @Test
    public void archiveFileFormat() throws Exception {
        assertThat(dbNsfpArchive.getArchiveFileFormat(), equalTo("zip"));
    }

    @Test
    public void dataFileFormat() throws Exception {
        assertThat(dbNsfpArchive.getDataFileFormat(), equalTo("chr"));
    }
}