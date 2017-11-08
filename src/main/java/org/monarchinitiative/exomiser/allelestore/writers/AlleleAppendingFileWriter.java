package org.monarchinitiative.exomiser.allelestore.writers;

import org.jetbrains.annotations.NotNull;
import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.parsers.AlleleParser;
import org.monarchinitiative.exomiser.allelestore.parsers.ExomiserAlleleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Creates a set of 25 temp_chr_N.allele files where N is the chromosome numbered from 1-25. It will append
 * any {@link Allele} added via the {@link #write(Allele)} method to the corresponding chromosome temp file.
 * <p>
 * When all the resources have been parsed the {@link #mergeToFile(String)} method should be called which will produce a
 * VCF formatted file (without the header) of sorted, non-redundant alleles.
 * <p>
 * Currently these {@link Allele} are all held in RAM. Chromosome 1 contains ~20 million alleles when the ESP, ExAC,
 * dbSNP and dbNSFP resources are combined, this requires a little under 10GB RAM.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleAppendingFileWriter implements AlleleWriter {

    private static final Logger logger = LoggerFactory.getLogger(AlleleAppendingFileWriter.class);
    //NUM_CHROMOSOMES is set to 25 (1-22 + X, Y, M) + 1 so that the zero-based for loops create a 1-based chr file.
    private static final int NUM_CHROMOSOMES = 25 + 1;

    private final Path workingDir;
    private final Map<Integer, BufferedWriter> bufferedWriterMap;
    private final Map<Integer, Path> chromosomePaths = new TreeMap<>();

    private long count;

    public AlleleAppendingFileWriter(Path workingDir) {
        this.workingDir = workingDir;
        this.bufferedWriterMap = prepareWriters();
    }

    private Map<Integer, BufferedWriter> prepareWriters() {
        Map<Integer, BufferedWriter> writers = new HashMap<>();
        for (int i = 1; i < 26; i++) {
            Path path = workingDir.resolve("temp_chr" + i + ".allele");
            chromosomePaths.put(i, path);
            try {
                if (path.toFile().createNewFile()) {
                    BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
                    writers.put(i, bufferedWriter);
                }
            } catch (IOException e) {
                logger.error("{}", e);
            }
        }
        return writers;
    }

    @Override
    public void write(Allele allele) {
        BufferedWriter chromosomeFile = bufferedWriterMap.get(allele.getChr());
        String alleleString = toLine(allele);
        try {
            chromosomeFile.write(alleleString);
            chromosomeFile.flush();
            count++;
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    private String toLine(Allele allele) {
        StringJoiner stringJoiner = new StringJoiner("\t");
        stringJoiner.add(Integer.toString(allele.getChr()));
        stringJoiner.add(Integer.toString(allele.getPos()));
        stringJoiner.add(allele.getRsId());
        stringJoiner.add(allele.getRef());
        stringJoiner.add(allele.getAlt());
        stringJoiner.add(".");
        stringJoiner.add(".");
        stringJoiner.add(makeInfoFields(allele) + "\n");
        return stringJoiner.toString();
    }

    @NotNull
    private String makeInfoFields(Allele allele) {
        String infoString = allele.generateInfoField();
        if (infoString.isEmpty()) {
            return ".";
        }
        return infoString;
    }

    public long count() {
        return count;
    }

    public Path mergeToFile(String filename) {
        closeWriters(bufferedWriterMap.values());
        return mergeAllelesByChromosome(filename, chromosomePaths);
    }

    private void closeWriters(Collection<BufferedWriter> writers) {
        for (BufferedWriter writer : writers) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("{}", e);
            }
        }
    }

    //merge alleles in each chromosome
    private Path mergeAllelesByChromosome(String outFile, Map<Integer, Path> chromosomePaths) {
        Path merged = workingDir.resolve(outFile);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(merged, StandardOpenOption.CREATE)) {
            for (int i = 1; i < NUM_CHROMOSOMES; i++) {
                logger.info("Reading chromosome {}", i);
                Path chr = chromosomePaths.get(i);
                Map<String, Allele> alleleStore = readAndMergeAlleles(chr);
                logger.info("Writing merged chromosome {} to {}", i, merged);
                alleleStore.values().stream().sorted().forEach(writeAlleleLine(bufferedWriter));
            }
        } catch (IOException ex) {
            logger.error("Could not write file {}", merged, ex);
        }
        return merged;
    }

    private Map<String, Allele> readAndMergeAlleles(Path chr) {
        Map<String, Allele> alleleStore = new ConcurrentHashMap<>();
        AlleleParser alleleParser = new ExomiserAlleleParser();
        String line = null;
        long alleleCount = 0;
        try (BufferedReader br = Files.newBufferedReader(chr)) {
            while ((line = br.readLine()) != null) {
                List<Allele> alleles = alleleParser.parseLine(line);
                for (Allele allele : alleles) {
                    alleleCount++;
                    alleleStore.merge(allele.generateKey(), allele, mergeAlleles());
                    if (alleleCount % 1000000 == 0) {
                        logger.info("Read and merged {} into {} alleles", alleleCount, alleleStore.size());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Could not parse line {}", line, ex);
        }
        logger.info("Merged {} - read and merged {} into {} alleles", chr.getFileName(), alleleCount, alleleStore.size());
        return alleleStore;
    }

    private BiFunction<? super Allele, ? super Allele, ? extends Allele> mergeAlleles() {
        //TODO: test this works in cases where latest = null, original = null and latest and original need merging
        return (latest, original) -> {
            if (original == null) {
                logger.info("Added new: {}", latest);
                return latest;
            } else {
                if (original.getRsId().equals(".")) {
                    original.setRsId(latest.getRsId());
                }
                original.getValues().putAll(latest.getValues());
            }
            return original;
        };
    }

    @NotNull
    private Consumer<Allele> writeAlleleLine(BufferedWriter bufferedWriter) {
        return allele -> {
            try {
                bufferedWriter.write(toLine(allele));
            } catch (Exception e) {
                logger.error("Could not write allele {}", allele, e);
            }
        };
    }

}
