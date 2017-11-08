package org.monarchinitiative.exomiser.allelestore.writers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface AlleleWriter {

    public void write(Allele allele);

    public long count();

}
