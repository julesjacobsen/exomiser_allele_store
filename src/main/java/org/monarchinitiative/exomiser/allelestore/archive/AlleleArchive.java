package org.monarchinitiative.exomiser.allelestore.archive;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface AlleleArchive {

    public Path getPath();

    public String getArchiveFileFormat();

    public String getDataFileFormat();
}
