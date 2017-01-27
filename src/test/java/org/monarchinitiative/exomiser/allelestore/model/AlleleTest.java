package org.monarchinitiative.exomiser.allelestore.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleTest {

    @Test
    public void alleleWithNoProperties() {
        Allele instance = new Allele(1, 123435, "A", "T");
        assertThat(instance.getChr(), equalTo(1));
        assertThat(instance.getPos(), equalTo(123435));
        assertThat(instance.getRef(), equalTo("A"));
        assertThat(instance.getAlt(), equalTo("T"));
        assertThat(instance.getRsId(), equalTo("."));
        assertThat(instance.getValues().isEmpty(), is(true));
    }

    @Test
    public void allelesSortedNaturally() {
        Allele instance0 = new Allele(1, 123435, "A", "C");
        Allele instance1 = new Allele(1, 123435, "A", "G");
        Allele instance2 = new Allele(1, 123435, "A", "T");
        Allele instance3 = new Allele(1, 123436, "A", "T");
        Allele instance4 = new Allele(2, 123436, "A", "AT");
        Allele instance5 = new Allele(2, 123436, "A", "T");
        Allele instance6 = new Allele(2, 123436, "AA", "T");

        List<Allele> sorted = Arrays.asList(instance0, instance1, instance2, instance3, instance4, instance5, instance6);
        Collections.shuffle(sorted);
        Collections.sort(sorted);

        List<Allele> expected = Arrays.asList(instance0, instance1, instance2, instance3, instance4, instance5, instance6);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testId() {
        Allele instance = new Allele(1, 123456, "A", "C");
        assertThat(instance.getId(), equalTo("1-123456-A-C"));
    }

    @Test
    public void testRsId() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.setRsId(".");
        assertThat(instance.getRsId(), equalTo("."));
    }

    @Test
    public void testAddValue() {
        Allele instance = new Allele(1, 123456, "A", "C");
        instance.addValue(AlleleProperty.KG, 0.12f);
        System.out.println(instance);
        assertThat(instance.getValue(AlleleProperty.KG), equalTo( 0.12f));
        assertThat(instance.getValues().size(), equalTo(1));
    }

    @Test
    public void testEquality() {
        Allele instance0 = new Allele(1, 123456, "A", "C");
        Allele instance1 = new Allele(1, 123456, "A", "C");
        assertThat(instance0, equalTo(instance1));
    }

    @Test
    public void testToString() {
        System.out.println(new Allele(1, 123435, "A", "C"));
    }
}