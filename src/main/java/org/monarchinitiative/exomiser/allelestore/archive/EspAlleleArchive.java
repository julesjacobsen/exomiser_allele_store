package org.monarchinitiative.exomiser.allelestore.archive;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspAlleleArchive extends AbstractAlleleArchive {

    public EspAlleleArchive(Path archivePath) {
        super(archivePath, "tgz", "vcf");
    }
}
