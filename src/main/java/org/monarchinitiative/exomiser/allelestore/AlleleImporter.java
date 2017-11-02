package org.monarchinitiative.exomiser.allelestore;

import org.apache.commons.vfs2.*;
import org.jetbrains.annotations.NotNull;
import org.monarchinitiative.exomiser.allelestore.indexers.*;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.parsers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class AlleleImporter implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AlleleImporter.class);

    private final Path workingDir;
    private final AlleleAppendingFileWriter alleleWriter;

    public AlleleImporter(Path workingDir) {
        this.workingDir = workingDir;
        this.alleleWriter = new AlleleAppendingFileWriter(workingDir);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        //--working-directory=
        //--loadDbSnp=C:/Users/hhx640/Downloads/00-All.vcf.gz --loadExac=C:/Users/hhx640/Downloads/ExAC.r0.3.1.sites.vep.vcf.gz --loadEsp=C:/Users/hhx640/Downloads/ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz --out=exomiser_frequencies.allele

        if (applicationArguments.containsOption("loadExac")) {
            //--loadExac=C:/Users/hhx640/Downloads/ExAC.r0.3.1.sites.vep.vcf.gz
            List<String> fileName = applicationArguments.getOptionValues("loadExac");
            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("Please specify the full system path to ExAC ExAC.r0.3.1.sites.vep.vcf.gz file");
            }
            logger.info("Loading ExAC");
            loadFile("gz", fileName.get(0), "vcf", new ExacAlleleParser());
        }

        if (applicationArguments.containsOption("loadDbSnp")) {
            //--loadDbSnp=C:/Users/hhx640/Downloads/00-All.vcf.gz
            List<String> fileName = applicationArguments.getOptionValues("loadDbSnp");
            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("Please specify the full system path to dbSNP 00-All file");
            }
            logger.info("Loading dbSNP");
            loadFile("gz", fileName.get(0), "vcf", new DbSnpAlleleParser());
        }

        if (applicationArguments.containsOption("loadEsp")) {
            //--loadEsp=C:/Users/hhx640/Downloads/ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz
            List<String> fileName = applicationArguments.getOptionValues("loadEsp");
            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("Please specify the full system path to ESP ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz file");
            }
            logger.info("Loading ESP");
            loadFile("tgz", fileName.get(0), "vcf", new EspAlleleParser());
        }

        if (applicationArguments.containsOption("loadDbNsfp")) {
            //--loadDbNsfp=C:/Users/hhx640/Downloads/dbNSFPv3.4a.zip
            List<String> fileName = applicationArguments.getOptionValues("loadDbNsfp");
            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("Please specify the full system path to ESP ESP6500SI-V2-SSA137.GRCh38-liftover.snps_indels.vcf.tar.gz file");
            }
            logger.info("Loading dbNSFP");
            loadFile("zip", fileName.get(0), "chr", new DbNsfpAlleleParser());
        }
        if (applicationArguments.containsOption("out")) {
            //--out=exomiser_merged.allele
            // use VM options: -XX:+UseG1GC -Xmx10G
            List<String> fileName = applicationArguments.getOptionValues("out");
            if (fileName.isEmpty()) {
                throw new IllegalArgumentException("Please specify the output file name");
            }
            String mergedFileName = fileName.get(0);
            logger.info("Merging alleles to file {}", mergedFileName);
            alleleWriter.mergeToFile(mergedFileName);
        }

        if (applicationArguments.containsOption("indexer")) {
            List<String> indexOption = applicationArguments.getOptionValues("indexer");
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

        logger.info("Done");
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

    /**
     * @param archiveFormat   format of the original compressed archive file - tgz, gz or zip
     * @param archiveFileName file name of the original compressed archive file
     * @param dataFileFormat  extension of the uncompressed data file inside the archive file - usually vcf, but dbNSFP uses .chr[1-22,X,Y,M]
     * @param alleleParser    resource specific allele parser
     */
    public void loadFile(String archiveFormat, String archiveFileName, String dataFileFormat, AlleleParser alleleParser) {
        int alleleCount = 0;
        Instant startTime = Instant.now();
        try {
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject archive = fileSystemManager.resolveFile(archiveFormat + ":file://" + archiveFileName);
            for (FileObject file : archive.getChildren()) {
                if (file.getName().getExtension().startsWith(dataFileFormat)) {
                    logger.info("Reading file {}", file.getName());
                    FileContent fileContent = file.getContent();
                    InputStream is = fileContent.getInputStream();
                    String line = null;
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        while ((line = br.readLine()) != null) {

                            List<Allele> alleles = alleleParser.parseLine(line);
                            for (Allele allele : alleles) {
                                alleleCount++;
                                alleleWriter.save(allele);
                            }

                            if (alleleCount % 1000000 == 0) {
                                long seconds = Duration.between(startTime, Instant.now()).getSeconds();
                                logger.info("Processed {} variants total in {} sec", alleleCount, seconds);
                                System.out.println(line);
                                if (!alleles.isEmpty()) {
                                    System.out.println(alleles.get(0));
                                }
                            }

                        }
                    } catch (Exception ex) {
                        logger.error("Could not parse line {}", line, ex);
                    }
                }
            }
        } catch (FileSystemException e) {
            logger.error("{}", e);
        }
        long seconds = Duration.between(startTime, Instant.now()).getSeconds();
        logger.info("Finished - processed {} variants total in {} sec", alleleWriter.count(), seconds);
    }

}
