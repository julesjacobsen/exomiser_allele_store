package org.monarchinitiative.exomiser.allelestore.indexers;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.allelestore.model.Allele;

import java.nio.file.Path;

/**
 * AlleleStore implementation using the H2 database MVStore.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MvStoreAlleleIndexer extends AbstractAlleleIndexer {

    private final MVStore mvStore;
    private final MVMap<String, String> map;

    public MvStoreAlleleIndexer(Path indexPath) {
        String fileName = indexPath.resolve("alleles.mv.db").toAbsolutePath().toString();
        // open the store (in-memory if fileName is null)
        mvStore = new MVStore.Builder()
                .fileName(fileName)
                .compress()
                .open();

        // create/get the map named "alleles"
        //todo - investigate creating either one map per chromosome or per data type e.g. path and freq
        map = mvStore.openMap("alleles");
    }

    @Override
    protected void writeAllele(Allele allele) {
        map.put(allele.generateKey(), allele.generateInfoField());
    }

    @Override
    protected void commit() {
        mvStore.commit();
    }

    @Override
    protected void close() {
        mvStore.close();
    }
}
