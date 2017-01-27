package org.monarchinitiative.exomiser.allelestore.parsers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class VcfAlleleParser implements AlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(VcfAlleleParser.class);

    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            return Collections.emptyList();
        }
        String[] fields = line.split("\t");
        List<Allele> alleles = parseAlleles(fields, line);

        if (hasNoInfoField(fields)) {
            return alleles;
        }
        String info = fields[7];
        return parseInfoField(alleles, info);
    }

    private boolean hasNoInfoField(String[] fields) {
        return fields.length <= 7;
    }

    abstract List<Allele> parseInfoField(List<Allele> alleles, String info);

    private List<Allele> parseAlleles(String[] fields, String line) {

        byte chr = parseChr(fields[0], line);
        int pos = Integer.parseInt(fields[1]);
        //A dbSNP rsID such as rs101432848. In rare cases may be multiple e.g., rs200118651;rs202059104
        String[] rsId = fields[2].split(";");
        //Uppercasing shouldn't be necessary acccording to the VCF standard,
        //but occasionally one sees VCF files with lower case for part of the
        //sequences, e.g., to show indels.
        String ref = fields[3].toUpperCase();

        //dbSNP has introduced the concept of multiple minor alleles on the
        //same VCF line with their frequencies reported in same order in the
        //INFO field in the CAF section Because of this had to introduce a loop
        //and move the dbSNP freq parsing to here. Not ideal as ESP processing
        //also goes through this method but does not use the CAF field so
        //should be skipped
        String[] alts = fields[4].toUpperCase().split(",");

        // VCF files and Annovar-style annotations use different nomenclature for
        // indel variants. We use Annovar.
        //TODO - now that we use the new Jannovar which uses a 0-based co-ordinate system investigate is this is necessary
//       transformVCF2AnnovarCoordinates();
        List<Allele> alleles = new ArrayList<>();
        for (int i = 0; i < alts.length; i++) {
            Allele allele = new Allele(chr, pos, ref, alts[i]);
            allele.setRsId(getCurrentRsId(rsId));
            alleles.add(allele);
        }
        return alleles;
    }

    /**
     * rsIds can be merged - these are reported in the format rs200118651;rs202059104 where the first rsId is the current one,
     * the second is the rsId which was merged into the first.
     *
     * @param rsIds an array of rsId. Can be empty.
     * @return The first rsId present in an array or "." if empty.
     */
    private String getCurrentRsId(String[] rsIds) {
        if (rsIds.length >= 1) {
            return rsIds[0];
        }
        return ".";
    }
}
