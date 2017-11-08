package org.monarchinitiative.exomiser.allelestore.archive;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfpAlleleArchive extends AbstractAlleleArchive {

    public DbNsfpAlleleArchive(Path archivePath) {
        super(archivePath, "zip", "chr");
    }
}
