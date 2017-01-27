package org.monarchinitiative.exomiser.allelestore.indexers;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import org.jetbrains.annotations.NotNull;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.parsers.AlleleParser;
import org.monarchinitiative.exomiser.allelestore.parsers.ExomiserAlleleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
//@Component
public class OrientAlleleIndexer implements AlleleIndexer {

    private static final Logger logger = LoggerFactory.getLogger(OrientAlleleIndexer.class);

    public OrientAlleleIndexer() {
        String orientdbHome = new File("").getAbsolutePath(); //Set OrientDB home to current directory
        System.setProperty("ORIENTDB_HOME", orientdbHome);
    }

    //-Dstorage.useWAL=false
// -XX:MaxDirectMemorySize=7200m
// -Xmx800m -Dstorage.diskCache.bufferSize=7200
    //-XX:+UseG1GC -XX:MaxDirectMemorySize=7200m -Dstorage.useWAL=false -Xmx300m -Dstorage.diskCache.bufferSize=7200
    //-XX:+UseG1GC -XX:MaxDirectMemorySize=16021m -Dstorage.useWAL=false -Xmx300m -Dstorage.diskCache.bufferSize=16021

    //-Dstorage.useWAL=false -Xms512m -Xmx512m -Djna.nosys=true -XX:+HeapDumpOnOutOfMemoryError -XX:MaxDirectMemorySize=512g -Djava.awt.headless=true -Dfile.encoding=UTF8 -Drhino.opt.level=9
    @Override
    public void buildIndex(Path alleleDataFile, Path indexPath) {
        OServer oServer = startOrientServer();
        logger.info("Creating variants database");
        //This is only required once to create the database. Calling it more than once will throw an error
        createAlleleDatabase();
        OPartitionedDatabasePool databasePool = newVariantDbPool(indexPath);
        ODatabaseDocumentTx database = databasePool.acquire();
        database.declareIntent(new OIntentMassiveInsert());

        OSchema schema = database.getMetadata().getSchema();
        OClass alleleClass = createAlleleDocSchema(schema);
        logger.info("Creating alleleIndex");
        alleleClass.createIndex("alleleIndex", OClass.INDEX_TYPE.UNIQUE_HASH_INDEX, "chr", "pos", "ref", "alt");

        logger.info("Starting indexing...");
        AlleleParser alleleParser = new ExomiserAlleleParser();
        String line = null;
        long alleleCount = 0;
        try (BufferedReader br = Files.newBufferedReader(alleleDataFile)) {
            while ((line = br.readLine()) != null) {
                List<Allele> alleles = alleleParser.parseLine(line);
                for (Allele allele : alleles) {
                    alleleCount++;
                    ODocument alleleDoc = new ODocument();
                    addAlleleDoc(alleleDoc, allele);
                    if (alleleCount % 1000000 == 0) {
                        logger.info("Now on chr {} - added {} allele docs", allele.getChr(), alleleCount);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Could not parse line {}", line, ex);
        }

        logger.info("Finished OrientDB indexing of {} alleles", database.countClass("Allele"));
        database.close();
        databasePool.close();
        oServer.shutdown();
    }

    @NotNull
    private OClass createAlleleDocSchema(OSchema schema) {
        OClass alleleClass = schema.getOrCreateClass("Allele");
        alleleClass.createProperty("chr", OType.INTEGER);
        alleleClass.createProperty("pos", OType.INTEGER);
        alleleClass.createProperty("rsId", OType.STRING);
        alleleClass.createProperty("ref", OType.STRING);
        alleleClass.createProperty("alt", OType.STRING);
        alleleClass.createProperty("properties", OType.EMBEDDEDMAP);
        return alleleClass;
    }

    private void addAlleleDoc(ODocument alleleDoc, Allele allele) {
        alleleDoc.reset();
        alleleDoc.setClassName("Allele");
        alleleDoc.field("chr", allele.getChr());
        alleleDoc.field("pos", allele.getPos());
        alleleDoc.field("rsId", allele.getRsId());
        alleleDoc.field("ref", allele.getRef());
        alleleDoc.field("alt", allele.getAlt());
//        ODocument freqDoc = new ODocument("frequencies");
//        alleleDoc.field("freq", freqDoc);
//        ODocument pathDoc = new ODocument("pathogenicities");
//        alleleDoc.field("path", pathDoc);
        alleleDoc.field("properties", allele.getValues());
//        for (Map.Entry<AlleleProperty, Float> entry : allele.getValues().entrySet()) {
//            AlleleProperty key = entry.getKey();
//            Float value = entry.getValue();
//            alleleDoc.field(key.name(), value);
////            if (AlleleProperty.PATHOGENIC_PROPERTIES.contains(key)) {
////                pathDoc.field(key.name(), value);
////            } else {
////                freqDoc.field(key.name(), value);
////            }
//        }
        alleleDoc.save();
    }

    private void createAlleleDatabase() {
        try {
            getRootServerAdmin()
                    .createDatabase("variants", "document", "plocal")
                    .close();
        } catch (IOException e) {
            logger.error("{}", e);
        }
    }


    OPartitionedDatabasePool newVariantDbPool(Path indexPath) {
        return new OPartitionedDatabasePoolFactory().get("plocal:" + indexPath, "admin", "admin");
//        return new OPartitionedDatabasePoolFactory().get("remote:localhost/variants","admin", "admin");
    }

    public OServer startOrientServer() {
        try {
            OServer server = OServerMain.create();
            server.startup(config());
            server.activate();
            return server;
        } catch (Exception e) {
            logger.error("{}", e);
        }
        return null;
    }

    private OServerAdmin getRootServerAdmin() throws IOException {
        return new OServerAdmin("localhost/variants")
                .connect("root", "3(hiDpjFk6nk[)Q7?qD");
    }

    private String config() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<orient-server>\n" +
                "<network>\n" +
                "<protocols>\n" +
                "<protocol name=\"binary\" implementation=\"com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary\"/>\n" +
                "<protocol name=\"http\" implementation=\"com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb\"/>\n" +
                "</protocols>\n" +
                "<listeners>\n" +
                "<listener ip-address=\"0.0.0.0\" port-range=\"2424-2430\" protocol=\"binary\"/>\n" +
                "<listener protocol=\"http\" port-range=\"2480-2490\" ip-address=\"0.0.0.0\">\n" +
                "<commands>\n" +
                "<command implementation=\"com.orientechnologies.orient.server.network.protocol.http.command.get.OServerCommandGetStaticContent\" pattern=\"GET|www GET|studio/ GET| GET|*.htm GET|*.html GET|*.xml GET|*.jpeg GET|*.jpg GET|*.png GET|*.gif GET|*.js GET|*.css GET|*.swf GET|*.ico GET|*.txt GET|*.otf GET|*.pjs GET|*.svg\">\n" +
                "<parameters>\n" +
                "<entry value=\"Cache-Control: no-cache, no-store, max-age=0, must-revalidate\r\nPragma: no-cache\" name=\"http.cache:*.htm *.html\"/>\n" +
                "<entry value=\"Cache-Control: max-age=120\" name=\"http.cache:default\"/>\n" +
                "</parameters>\n" +
                "</command>\n" +
                "</commands>\n" +
                "</listener>\n" +
                "</listeners>\n" +
                "</network>\n" +
                "<users>\n" +
                "<user name=\"root\" password=\"3(hiDpjFk6nk[)Q7?qD\" resources=\"*\"/>\n" +
                "<user name=\"exomiser\" password=\"exomiser\" resources=\"connect,server.listDatabases,server.dblist\"/>\n" +
                "<user name=\"guest\" password=\"]$2)LZJn49Bpq[WRbYo\" resources=\"connect,server.listDatabases,server.dblist\"/>\n" +
                "</users>\n" +
                "<properties>\n" +
                "<entry name=\"server.database.path\" value=\"C:/Data/variants\"/>\n" +
                "<entry name=\"orientdb.config.file\" value=\"C:/Data/variants/config/orientdb-server-config.xml\"/>" +
                "<entry name=\"server.cache.staticResources\" value=\"false\"/>\n" +
                "<entry name=\"log.console.level\" value=\"info\"/>\n" +
                "<entry name=\"log.file.level\" value=\"fine\"/>\n" +
                "<entry name=\"plugin.dynamic\" value=\"false\"/>\n" +
                "</properties>\n" +
                "</orient-server>";
    }
}
