package org.monarchinitiative.exomiser.allelestore.parsers;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExomiserAlleleParserTest {

    @Test
    public void testParseLine() {
        String line = "25\t42\trs377245343\tT\tTC\t.\t.\t.\n";
        ExomiserAlleleParser instance = new ExomiserAlleleParser();
        instance.parseLine(line);
    }

}