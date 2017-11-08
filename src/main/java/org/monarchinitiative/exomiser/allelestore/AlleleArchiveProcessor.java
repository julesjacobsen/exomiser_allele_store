package org.monarchinitiative.exomiser.allelestore;

import org.apache.commons.vfs2.FileObject;
import org.monarchinitiative.exomiser.allelestore.archive.AlleleArchive;
import org.monarchinitiative.exomiser.allelestore.archive.ArchiveFileReader;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.parsers.AlleleParser;
import org.monarchinitiative.exomiser.allelestore.writers.AlleleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleArchiveProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AlleleArchiveProcessor.class);

    private final AlleleArchive alleleArchive;
    private final AlleleParser alleleParser;

    public AlleleArchiveProcessor(AlleleArchive alleleArchive, AlleleParser alleleParser) {
        this.alleleArchive = alleleArchive;
        this.alleleParser = alleleParser;
    }

    public void process(AlleleWriter alleleWriter) {
        ArchiveFileReader archiveFileReader = new ArchiveFileReader(alleleArchive);
        Instant startTime = Instant.now();
        AlleleLogger alleleLogger = new AlleleLogger(startTime);
        for (FileObject fileObject : archiveFileReader.getFileObjects()) {
            try (InputStream archiveFileInputStream = archiveFileReader.readFileObject(fileObject);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(archiveFileInputStream))) {
                bufferedReader.lines()
                        .flatMap(toAlleleStream())
                        .peek(alleleLogger.logCount())
                        .forEach(alleleWriter::write);
            } catch (IOException e) {
                logger.error("Error reading archive file {}", fileObject.getName(), e);
            }
        }
        long seconds = Duration.between(startTime, Instant.now()).getSeconds();
        logger.info("Finished - processed {} variants total in {} sec", alleleWriter.count(), seconds);
    }

    private Function<String, Stream<Allele>> toAlleleStream() {
        return line -> alleleParser.parseLine(line).stream();
    }

    private class AlleleLogger {

        private final AtomicInteger counter;
        private final Instant startTime;

        public AlleleLogger(Instant startTime) {
            this.counter = new AtomicInteger();
            this.startTime = startTime;
        }

        public Consumer<Allele> logCount() {
            return allele -> {
                counter.incrementAndGet();
                if (counter.get() % 1000000 == 0) {
                    long seconds = Duration.between(startTime, Instant.now()).getSeconds();
                    logger.info("Processed {} variants total in {} sec", counter.get(), seconds);
                    logger.info("{}", allele);
                }
            };
        }
    }

}
