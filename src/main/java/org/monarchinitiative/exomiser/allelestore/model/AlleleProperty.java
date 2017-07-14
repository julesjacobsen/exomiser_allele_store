package org.monarchinitiative.exomiser.allelestore.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum AlleleProperty {
    KG,

    ESP_EA,
    ESP_AA,
    ESP_ALL,

    EXAC_AFR,
    EXAC_AMR,
    EXAC_EAS,
    EXAC_FIN,
    EXAC_NFE,
    EXAC_OTH,
    EXAC_SAS,

    SIFT,
    POLYPHEN,
    MUT_TASTER;

    public static final Set<AlleleProperty> FREQUENCY_PROPERTIES = EnumSet.range(KG, EXAC_SAS);
    public static final Set<AlleleProperty> PATHOGENIC_PROPERTIES = EnumSet.range(SIFT, MUT_TASTER);

}
