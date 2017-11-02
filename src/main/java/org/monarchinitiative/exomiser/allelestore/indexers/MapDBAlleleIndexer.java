package org.monarchinitiative.exomiser.allelestore.indexers;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MapDBAlleleIndexer extends AbstractAlleleIndexer {

    private static final Logger logger = LoggerFactory.getLogger(MapDBAlleleIndexer.class);

    private final DB db;
    private final Map<String, String> map;

    public MapDBAlleleIndexer(Path indexPath) {
        File dbFile = indexPath.resolve("alleles.db").toAbsolutePath().toFile();
        db = DBMaker
                .fileDB(dbFile)
                .fileMmapEnable()
                .allocateStartSize(10 * 1024 * 1024 * 1024)// 10GB
                .allocateIncrement(512 * 1024 * 1024)       // 512MB
//                .closeOnJvmShutdown()
                .make();

        map = db.hashMap("alleles")
                .keySerializer(Serializer.STRING)
                .valueSerializer(new SerializerCompressionWrapper(Serializer.STRING))
                .createOrOpen();
    }

    protected void writeAllele(Allele allele) {
        //TODO: if this works well there is no need to do the merge step for the file, just use this directly
        // in the AlleleAppendingFileWriter.readAndMergeAlleles
        String key = allele.generateKey();
        String value = allele.generateInfoField();
        map.put(key, value);
    }

    protected void commit() {
        db.commit();
    }

    protected void close() {
        db.close();
    }
}
