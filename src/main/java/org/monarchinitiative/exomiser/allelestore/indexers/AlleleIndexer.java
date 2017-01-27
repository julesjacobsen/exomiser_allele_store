package org.monarchinitiative.exomiser.allelestore.indexers;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface AlleleIndexer {

    void buildIndex(Path alleleDataFile, Path indexPath);
}
