package org.monarchinitiative.exomiser.allelestore.indexers;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.TupleSerialBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.*;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SleepyCatAlleleIndexer extends AbstractAlleleIndexer {

    private static final Logger logger = LoggerFactory.getLogger(SleepyCatAlleleIndexer.class);

    private final Environment sleepyCatEnvironment;
    private final Database alleleDatabase;

    private AlleleKeyBinder alleleKeyBinder;// = new AlleleKeyBinder();
//    private AlleleDataBinder alleleDataBinder = new AlleleDataBinder();

    public SleepyCatAlleleIndexer(Path indexPath) {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);

        Environment environment = new Environment(indexPath.toFile(), envConfig);
        sleepyCatEnvironment = environment;
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setExclusiveCreate(true);
        dbConfig.setAllowCreate(true);
        alleleDatabase = environment.openDatabase(null, "alleles", dbConfig);
    }

    @Override
    protected void commit() {
        sleepyCatEnvironment.sync();
    }

    @Override
    protected void close() {
        sleepyCatEnvironment.cleanLog();
        logger.info("Closing database");
        alleleDatabase.close();
        sleepyCatEnvironment.close();
    }

    @Override
    protected void writeAllele(Allele allele) {
//        AlleleKeyBinder alleleKeyBinder = new AlleleKeyBinder();

        DatabaseEntry alleleKey = generateKey(allele);
        DatabaseEntry alleleData = generateData(allele);
        alleleDatabase.put(null, alleleKey, alleleData);
    }

    private DatabaseEntry generateKey(Allele allele) {
        DatabaseEntry databaseEntry = new DatabaseEntry();
//        alleleKeyBinder.objectToEntry(allele, databaseEntry);
        return databaseEntry;
    }

    private DatabaseEntry generateData(Allele allele) {
        DatabaseEntry databaseEntry = new DatabaseEntry();
//        alleleDataBinder.objectToEntry(allele, databaseEntry);
        return databaseEntry;
    }

    public class AlleleKeyBinder extends TupleSerialBinding<DatabaseEntry, Allele> {

        public AlleleKeyBinder(SerialBinding<DatabaseEntry> dataBinding) {
            super(dataBinding);
        }

        @Override
        public Allele entryToObject(TupleInput tupleInput, DatabaseEntry databaseEntry) {
            int chr = tupleInput.readInt();
            int pos = tupleInput.readInt();
            String ref = tupleInput.readString();
            String alt = tupleInput.readString();

            //TODO get the data from DatabaseEntry

            return new Allele(chr, pos, ref, alt);
        }

        @Override
        public void objectToKey(Allele allele, TupleOutput tupleOutput) {
            tupleOutput.writeInt(allele.getChr());
            tupleOutput.writeInt(allele.getPos());
            tupleOutput.writeString(allele.getRef());
            tupleOutput.writeString(allele.getAlt());
        }

        @Override
        public DatabaseEntry objectToData(Allele allele) {
            return null;
        }

    }

}
