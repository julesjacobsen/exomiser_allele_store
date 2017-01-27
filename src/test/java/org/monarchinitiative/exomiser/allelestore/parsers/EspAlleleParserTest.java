package org.monarchinitiative.exomiser.allelestore.parsers;

import org.junit.Test;
import org.monarchinitiative.exomiser.allelestore.model.Allele;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspAlleleParserTest {

    @Test
    public void testSingleAlleleSnp() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "17\t26942314\trs371278753\tC\tG\t.\tPASS\tDBSNP=dbSNP_138;EA_AC=1,8599;AA_AC=0,4406;TAC=1,13005;MAF=0.0116,0.0,0.0077;GTS=GG,GC,CC;EA_GTC=0,1,4299;AA_GTC=0,0,2203;GTC=0,1,6502;DP=24;GL=KIAA0100;CP=0.0;CG=0.2;AA=C;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_014680.3:intron;HGVS_CDNA_VAR=NM_014680.3:c.6526-50G>C;HGVS_PROTEIN_VAR=.;CDS_SIZES=NM_014680.3:6708;GS=.;PH=.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=17:28615296";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(17));
        assertThat(allele.getPos(), equalTo(26942314));
        assertThat(allele.getRsId(), equalTo("rs371278753"));
        assertThat(allele.getRef(), equalTo("C"));
        assertThat(allele.getAlt(), equalTo("G"));

        Map<String, Float> expectedFreqs = new HashMap<>();
//        expectedFreqs.put("ESP_EA", 0.0116f);
//        expectedFreqs.put("ESP_ALL", 0.0077f);
//
//        assertThat(allele.getFrequencies().entrySet(), containsInAnyOrder(expectedFreqs.entrySet()));

    }

    @Test
    public void testSingleAlleleDeletion() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "17\t73725391\t.\tGA\tG\t.\tPASS\tDBSNP=.;EA_AC=0,8254;AA_AC=2,4262;TAC=2,12516;MAF=0.0,0.0469,0.016;GTS=A1A1,A1R,RR;EA_GTC=0,0,4127;AA_GTC=0,2,2130;GTC=0,2,6257;DP=92;GL=ITGB4;CP=1.0;CG=3.6;AA=.;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_001005731.1:frameshift,NM_001005619.1:frameshift,NM_000213.3:frameshift;HGVS_CDNA_VAR=NM_001005731.1:c.613del1,NM_001005619.1:c.613del1,NM_000213.3:c.613del1;HGVS_PROTEIN_VAR=NM_001005731.1:p.(N205Tfs*5),NM_001005619.1:p.(N205Tfs*5),NM_000213.3:p.(N205Tfs*5);CDS_SIZES=NM_001005731.1:5259,NM_001005619.1:5418,NM_000213.3:5469;GS=.,.,.;PH=.,.,.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=17:75729310";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(17));
        assertThat(allele.getPos(), equalTo(73725391));
        assertThat(allele.getRsId(), equalTo("."));
        assertThat(allele.getRef(), equalTo("GA"));
        assertThat(allele.getAlt(), equalTo("G"));
//        assertThat(allele.getFrequencies().isEmpty(), is(true));

    }

    @Test
    public void testMultiAllele() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "7\t107167661\t.\tTAA\tTAAA,TTAA,TAAAA,T,TA\t.\tPASS\tDBSNP=.;EA_AC=2461,15,376,36,596,4262;AA_AC=1215,23,182,39,450,2089;TAC=3676,38,558,75,1046,6351;MAF=44.9781,47.7489,45.9213;GTS=A1A1,A1A2,A1A3,A1A4,A1A5,A1R,A2A2,A2A3,A2A4,A2A5,A2R,A3A3,A3A4,A3A5,A3R,A4A4,A4A5,A4R,A5A5,A5R,RR;EA_GTC=276,3,128,5,97,1676,1,0,0,0,10,28,0,8,184,1,1,28,45,400,982;AA_GTC=134,9,59,8,82,789,4,0,0,0,6,16,1,8,82,1,4,24,30,296,446;GTC=410,12,187,13,179,2465,5,0,0,0,16,44,1,16,266,2,5,52,75,696,1428;DP=17;GL=COG5;CP=0.0;CG=.;AA=.;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_181733.2:intron,NM_181733.2:intron,NM_181733.2:intron,NM_181733.2:intron,NM_181733.2:intron,NM_006348.3:intron,NM_006348.3:intron,NM_006348.3:intron,NM_006348.3:intron,NM_006348.3:intron,NM_001161520.1:intron,NM_001161520.1:intron,NM_001161520.1:intron,NM_001161520.1:intron,NM_001161520.1:intron;HGVS_CDNA_VAR=NM_181733.2:c.631+20del1,NM_181733.2:c.631+19_631+20del2,NM_181733.2:c.631+20_631+21insTT,NM_181733.2:c.631+20_631+21insTTAA,NM_181733.2:c.631+20_631+21insT,NM_006348.3:c.631+20del1,NM_006348.3:c.631+19_631+20del2,NM_006348.3:c.631+20_631+21insTT,NM_006348.3:c.631+20_631+21insTTAA,NM_006348.3:c.631+20_631+21insT,NM_001161520.1:c.631+20del1,NM_001161520.1:c.631+19_631+20del2,NM_001161520.1:c.631+20_631+21insTT,NM_001161520.1:c.631+20_631+21insTTAA,NM_001161520.1:c.631+20_631+21insT;HGVS_PROTEIN_VAR=.,.,.,.,.,.,.,.,.,.,.,.,.,.,.;CDS_SIZES=NM_181733.2:2520,NM_181733.2:2520,NM_181733.2:2520,NM_181733.2:2520,NM_181733.2:2520,NM_006348.3:2583,NM_006348.3:2583,NM_006348.3:2583,NM_006348.3:2583,NM_006348.3:2583,NM_001161520.1:2472,NM_001161520.1:2472,NM_001161520.1:2472,NM_001161520.1:2472,NM_001161520.1:2472;GS=.,.,.,.,.,.,.,.,.,.,.,.,.,.,.;PH=.,.,.,.,.,.,.,.,.,.,.,.,.,.,.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=7:107527216";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(5));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(7));
        assertThat(allele.getPos(), equalTo(107167661));
        assertThat(allele.getRsId(), equalTo("."));
        assertThat(allele.getRef(), equalTo("TAA"));
        assertThat(allele.getAlt(), equalTo("TAAA"));
//        assertThat(allele.getFrequencies().isEmpty(), is(true));

    }
}