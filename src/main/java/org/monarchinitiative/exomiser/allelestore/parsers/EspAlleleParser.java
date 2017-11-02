package org.monarchinitiative.exomiser.allelestore.parsers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspAlleleParser extends VcfAlleleParser {

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<AlleleProperty, Float> minorAlleleFrequencies = parseMinorAlleleFrequencies(info);

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            allele.getValues().putAll(minorAlleleFrequencies);
        }
        return alleles;
    }

    private Map<AlleleProperty, Float> parseMinorAlleleFrequencies(String info) {
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            if (infoField.startsWith("MAF=")) {
                return parseMafField(infoField);
            }
        }
        return Collections.emptyMap();
    }

    private Map<AlleleProperty, Float> parseMafField(String infoField) {
        Map<AlleleProperty, Float> frequencies = new EnumMap<>(AlleleProperty.class);
        //MAF=44.9781,47.7489,45.9213
        String[] minorAlleleFreqs = infoField.substring(4).split(",");
        for (MAF_FIELD field : MAF_FIELD.values()) {
            String freq = minorAlleleFreqs[field.ordinal()];
            if (!"0.0".equals(freq)) {
                frequencies.put(AlleleProperty.valueOf(field.name()), Float.parseFloat(freq));
            }
        }
        return frequencies;
    }

    private enum MAF_FIELD {
        //##INFO=<ID=MAF,Number=.,Type=String,Description="Minor Allele Frequency in percent in the order of EA,AA,All">
        ESP_EA, ESP_AA, ESP_ALL;
    }
}
