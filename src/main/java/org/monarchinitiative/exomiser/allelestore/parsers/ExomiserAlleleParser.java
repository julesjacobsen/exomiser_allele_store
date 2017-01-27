package org.monarchinitiative.exomiser.allelestore.parsers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.monarchinitiative.exomiser.allelestore.model.AlleleProperty;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExomiserAlleleParser extends VcfAlleleParser {

    private static final Set<String> freqKeys;
    private static final Set<String> pathKeys;

    static {
        freqKeys = new HashSet<>();
        for (ExacAlleleParser.EXAC_FIELD field: ExacAlleleParser.EXAC_FIELD.values()) {
            freqKeys.add(field.name());
        }
        freqKeys.addAll(Arrays.asList("KG", "ESP_EA", "ESP_AA", "ESP_ALL"));
        pathKeys = new HashSet<>();
        pathKeys.addAll(Arrays.asList("MUT_TASTER", "POLYPHEN", "SIFT"));
    }

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<AlleleProperty, Float> values = parseInfoLine(info.trim());
        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            allele.getValues().putAll(values);
        }
        return alleles;
    }

    private Map<AlleleProperty, Float> parseInfoLine(String info) {
        Map<AlleleProperty, Float> values = new EnumMap<>(AlleleProperty.class);
        if(".".equals(info)) {
            return values;
        }
        String[] fields = info.split(";");
        for (int i = 0; i < fields.length; i++) {
            String[] fieldValues = fields[i].split("=");
            values.put(AlleleProperty.valueOf(fieldValues[0]), Float.parseFloat(fieldValues[1]));
        }
        return values;
    }


}
