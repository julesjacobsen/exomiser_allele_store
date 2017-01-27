package org.monarchinitiative.exomiser.allelestore.parsers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @link https://www.ncbi.nlm.nih.gov/variation/docs/human_variation_vcf/
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbSnpAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(DbSnpAlleleParser.class);

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        List<String> minorAlleleFrequencies = parseMinorAlleleFrequencies(info);
        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            if (!minorAlleleFrequencies.isEmpty()) {
                String maf = minorAlleleFrequencies.get(i);
                if (! maf.equals(".")) {
                    float freq = 100f *  Float.parseFloat(maf);
                    allele.addValue(AlleleProperty.KG, freq);
                }
            }
        }
        return alleles;
    }

    private List<String> parseMinorAlleleFrequencies(String info) {
//##INFO=<ID=CAF,Number=.,Type=String,Description="An ordered, comma delimited list of allele frequencies based on 1000Genomes, starting with the reference allele followed by alternate alleles as ordered in the ALT column. Where a 1000Genomes alternate allele is not in the dbSNPs alternate allele set, the allele is added to the ALT column.  The minor allele is the second largest value in the list, and was previuosly reported in VCF as the GMAF.  This is the GMAF reported on the RefSNP and EntrezSNP pages and VariationReporter">
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            if (infoField.startsWith("CAF=")) {
                return parseCafField(infoField);
            }
        }
        return Collections.emptyList();
    }

    private List<String> parseCafField(String infoField) {
        //allele freq data format is ;CAF=0.9812,.,0.01882; where major allele is 1st followed by minor alleles in order of alt line
        List<String> minorFreqs = new ArrayList<>();
        String[] freqs = infoField.substring(4).split(",");
        //note we're taking the minor freqs, so the loop starts at int i = 1
        for (int i = 1; i < freqs.length; i++) {
            minorFreqs.add(freqs[i]);
        }
        return minorFreqs;
    }
}
