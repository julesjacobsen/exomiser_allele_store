package org.monarchinitiative.exomiser.allelestore.config;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePoolFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
//@Configuration
public class OrientDbConfig {

    @Autowired
    public Environment environment;

    @Bean(destroyMethod = "close")
    OPartitionedDatabasePool variantDbPool() {
        return new OPartitionedDatabasePoolFactory().get("plocal:C:/Data/variants","admin", "admin");
//        return new OPartitionedDatabasePoolFactory().get("remote:localhost/variants","admin", "admin");
    }

    public OrientDbConfig() throws Exception {
        String orientdbHome = new File("").getAbsolutePath(); //Set OrientDB home to current directory
        System.setProperty("ORIENTDB_HOME", orientdbHome);
    }

    @Bean(destroyMethod = "shutdown")
    public OServer orientServer() throws Exception {
        OServer server = OServerMain.create();
        server.startup(config());
        server.activate();
        //TODO:
//        server.shutdown();
        return server;
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
//                "<entry name=\"server.cache.staticResources\" value=\"false\"/>\n" +
                "<entry name=\"log.console.level\" value=\"info\"/>\n" +
                "<entry name=\"log.file.level\" value=\"fine\"/>\n" +
                "<entry name=\"plugin.dynamic\" value=\"false\"/>\n" +
                "</properties>\n" +
                "</orient-server>";
    }
}
