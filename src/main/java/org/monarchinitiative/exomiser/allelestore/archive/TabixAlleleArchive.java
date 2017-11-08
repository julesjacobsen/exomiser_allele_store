package org.monarchinitiative.exomiser.allelestore.archive;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TabixAlleleArchive extends AbstractAlleleArchive {

    public TabixAlleleArchive(Path archivePath) {
        super(archivePath, "gz", "vcf");
    }
}
