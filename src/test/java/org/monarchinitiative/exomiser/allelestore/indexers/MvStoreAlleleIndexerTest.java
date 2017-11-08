package org.monarchinitiative.exomiser.allelestore.indexers;

import com.google.common.collect.Sets;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.exomiser.allelestore.model.Allele;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MvStoreAlleleIndexerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void writeAllele() throws Exception {
        Path outPath = tempFolder.newFolder().toPath();
        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(outPath);

        Allele allele = new Allele(1, 12345, "A", "T");
        instance.writeAllele(allele);
        instance.commit();
        instance.close();

        MVStore mvStore = new MVStore.Builder()
                .fileName(outPath.resolve("alleles.mv.db").toString())
                .readOnly()
                .open();

        assertThat(mvStore.getMapNames(), equalTo(Sets.newHashSet("alleles")));
        MVMap<String, String> alleleMap = mvStore.openMap("alleles");
        assertThat(alleleMap.size(), equalTo(1));
        assertThat(alleleMap.containsKey("1-12345-A-T"), is(true));
        assertThat(alleleMap.get("1-12345-A-T"), equalTo(""));
    }

}