package org.monarchinitiative.exomiser.allelestore;

import org.jetbrains.annotations.NotNull;
import org.monarchinitiative.exomiser.allelestore.archive.AlleleArchive;
import org.monarchinitiative.exomiser.allelestore.archive.DbNsfpAlleleArchive;
import org.monarchinitiative.exomiser.allelestore.archive.EspAlleleArchive;
import org.monarchinitiative.exomiser.allelestore.archive.TabixAlleleArchive;
import org.monarchinitiative.exomiser.allelestore.indexers.*;
import org.monarchinitiative.exomiser.allelestore.parsers.*;
import org.monarchinitiative.exomiser.allelestore.writers.AlleleAppendingFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class AlleleStoreApplicationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AlleleArchiveProcessor.class);

    private final Path workingDir;
    private final AlleleAppendingFileWriter alleleWriter;

    public AlleleStoreApplicationRunner(Path workingDir) {
        this.workingDir = workingDir;
        this.alleleWriter = new AlleleAppendingFileWriter(workingDir);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        //--working-directory=
        //--loadDbSnp=C:/Users/hhx640/Downloads/00-All.vcf.gz --loadExac=C:/Users/hhx640/Downloads/ExAC.r0.3.1.sites.vep.vcf.gz --loadEsp=C:/Users/hhx640/Downloads/ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz --out=exomiser_frequencies.allele

        if (applicationArguments.containsOption("loadExac")) {
            //--loadExac=C:/Users/hhx640/Downloads/ExAC.r0.3.1.sites.vep.vcf.gz
            processExac(applicationArguments.getOptionValues("loadExac"));
        }

        if (applicationArguments.containsOption("loadDbSnp")) {
            //--loadDbSnp=C:/Users/hhx640/Downloads/00-All.vcf.gz
            processDbSnp(applicationArguments.getOptionValues("loadDbSnp"));
        }

        if (applicationArguments.containsOption("loadEsp")) {
            //--loadEsp=C:/Users/hhx640/Downloads/ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz
            processEsp(applicationArguments.getOptionValues("loadEsp"));
        }

        if (applicationArguments.containsOption("loadDbNsfp")) {
            //--loadDbNsfp=C:/Users/hhx640/Downloads/dbNSFPv3.4a.zip
            processDbNsfp(applicationArguments.getOptionValues("loadDbNsfp"));
        }
        if (applicationArguments.containsOption("out")) {
            //--out=exomiser_merged.allele
            // use VM options: -XX:+UseG1GC -Xmx10G
            mergeToOutfile(applicationArguments.getOptionValues("out"));
        }

        if (applicationArguments.containsOption("indexer")) {
            createIndex(applicationArguments.getOptionValues("indexer"));
        }

        logger.info("Done");
    }

    private void processExac(List<String> fileName) {
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Please specify the full system path to ExAC ExAC.r0.3.1.sites.vep.vcf.gz file");
        }
        logger.info("Loading ExAC");
        processArchive(new TabixAlleleArchive(Paths.get(fileName.get(0))), new ExacAlleleParser());
    }

    private void processDbSnp(List<String> fileName) {
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Please specify the full system path to dbSNP 00-All file");
        }
        logger.info("Loading dbSNP");
        processArchive(new TabixAlleleArchive(Paths.get(fileName.get(0))), new DbSnpAlleleParser());
    }

    private void processEsp(List<String> fileName) {
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Please specify the full system path to ESP ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz file");
        }
        logger.info("Loading ESP");
        processArchive(new EspAlleleArchive(Paths.get(fileName.get(0))), new EspAlleleParser());
    }

    private void processDbNsfp(List<String> fileName) {
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Please specify the full system path to dbNSFP zip file");
        }
        logger.info("Loading dbNSFP");
        processArchive(new DbNsfpAlleleArchive(Paths.get(fileName.get(0))), new DbNsfpAlleleParser());
    }

    private void processArchive(AlleleArchive alleleArchive, AlleleParser alleleParser) {
        AlleleArchiveProcessor alleleArchiveProcessor = new AlleleArchiveProcessor(alleleArchive, alleleParser);
        alleleArchiveProcessor.process(alleleWriter);
    }

    private void mergeToOutfile(List<String> outOptions) {
        if (outOptions.isEmpty()) {
            throw new IllegalArgumentException("Please specify the output file name");
        }
        String mergedFileName = outOptions.get(0);
        logger.info("Merging alleles to file {}", mergedFileName);
        alleleWriter.mergeToFile(mergedFileName);
    }

    private void createIndex(List<String> indexOption) throws IOException {
        if (indexOption.isEmpty()) {
            throw new IllegalArgumentException("Please specify the indexer required");
        }
        String indexer = indexOption.get(0);
        logger.info("Running {} indexer", indexer);
        Path indexerDir = workingDir.resolve(indexer);
        if (!indexerDir.toFile().exists()) {
            Files.createDirectory(indexerDir);
        }
        AlleleIndexer alleleIndexer = getAlleleIndexer(indexer, indexerDir);
        //Added 227,000,000 allele docs in 26 mins 5.35GB index
        //todo make this configurable
        alleleIndexer.buildIndex(workingDir.resolve("exomiser-all.vcf"), indexerDir);
    }

    @NotNull
    private AlleleIndexer getAlleleIndexer(String indexer, Path indexPath) {
        switch (indexer) {
            case "mvStore":
                return new MvStoreAlleleIndexer(indexPath);
            case "mapDB":
                return new MapDBAlleleIndexer(indexPath);
            case "berkeley":
                return new SleepyCatAlleleIndexer(indexPath);
            case "lucene":
            default:
                return new LuceneAlleleIndexer(indexPath);
        }
    }

}
