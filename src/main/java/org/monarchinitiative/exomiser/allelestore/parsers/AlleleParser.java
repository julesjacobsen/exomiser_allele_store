package org.monarchinitiative.exomiser.allelestore.parsers;

import org.monarchinitiative.exomiser.allelestore.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@FunctionalInterface
public interface AlleleParser {

    Logger logger = LoggerFactory.getLogger(AlleleParser.class);

    List<Allele> parseLine(String line);

    default byte parseChr(String field, String line) {
        switch (field) {
            case "X":
            case "x":
                return 23;
            case "Y":
            case "y":
                return 24;
            case "M":
            case "MT":
            case "m":
                return 25;
            case ".":
                return 0;
            default:
                try {
                    return Byte.parseByte(field);
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse chromosome: '{}'. Error occurred parsing line: {}", field, line, e);
                }
        }
        return 0;
    }
}
