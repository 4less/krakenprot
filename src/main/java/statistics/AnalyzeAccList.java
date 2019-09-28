package statistics;

import accession.AccessionMap;
import accession.SQLiteAccessionMap;
import taxonomy.NCBITaxonomy;
import taxonomy.TAXONOMIC_RANK;
import taxonomy.Taxon;
import taxonomy.Taxonomy;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by joachim on 22.09.19.
 */
public class AnalyzeAccList {
    private AccessionMap map;
    private NCBITaxonomy taxonomy;



    public AnalyzeAccList(AccessionMap map, NCBITaxonomy taxonomy) {
        this.map = map;
        this.taxonomy = taxonomy;
    }

    public void writeOut(String path, String out) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(out));


        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {


            String line;
            int taxid;
            Taxon t;

            while ((line = reader.readLine()) != null) {
                int lastindex = line.indexOf('.') > -1 ? line.indexOf('.') : line.length()-1;

                taxid = map.getTaxId(line.substring(0,lastindex));

                if (taxid > 1) {
                    writer.write(Integer.toString(taxid));
                    writer.newLine();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Map<Integer, Integer> getReadCount (String path, List<Integer> taxIds) {
        Map<Integer, Integer> counts = new HashMap<>();
        List<Taxon> taxa = taxIds.stream().map(integer -> taxonomy.getTaxon(taxonomy.getParentOfRank(integer, TAXONOMIC_RANK.SPECIES))).collect(Collectors.toList());
        taxa.stream().forEach(taxon -> counts.put(taxon.getId(), 0));

        for (Taxon taxon : taxa) {
            System.out.println(taxon.getId());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            String line;
            int taxid;
            int count;
            Taxon t;
            String[] s;

            while ((line = reader.readLine()) != null) {
                s = line.split("\t");
                taxid = Integer.parseInt(s[0]);
                count = Integer.parseInt(s[1]);

                t = taxonomy.getTaxon(taxid);

                for (Taxon taxon : taxa) {
                    if (t != null && t.isChildOf(taxon) >= 0)
                        counts.put(taxon.getId(), counts.get(taxon.getId()) + count);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return counts;
    }

    public static void main(String[] args) {
        NCBITaxonomy taxonomy = new NCBITaxonomy("nodes.dmp", "names.dmp");
        AccessionMap accessionMap = new SQLiteAccessionMap("accession2tid.db");

        AnalyzeAccList analyzer = new AnalyzeAccList(accessionMap, taxonomy);

        //analyzer.writeOut("prot.acc.txt", "taxids.mbarc.txt");

        List<Integer> taxids = new ArrayList<>();
        /*Sim20
        taxids.add(118967);
        taxids.add(195103);
        taxids.add(1349822);
        taxids.add(646529);
        taxids.add(585057);
        taxids.add(511145);
        taxids.add(1198114);
        taxids.add(203119);
        taxids.add(1678841);
        taxids.add(869210);
        taxids.add(1122223);
        taxids.add(633147);
        taxids.add(1122978);
        taxids.add(316);
        taxids.add(220341);
        taxids.add(573413);
        taxids.add(158190);
        taxids.add(93061);
        taxids.add(926566);
        taxids.add(717605);*/

        taxids.add(885581);
        taxids.add(55206);
        taxids.add(93466);
        taxids.add(52022);
        taxids.add(377615);
        taxids.add(2724);
        taxids.add(102134);
        taxids.add(79209);
        taxids.add(81475);
        taxids.add(29288);
        taxids.add(395922);
        taxids.add(44930);
        taxids.add(133926);
        taxids.add(392734);
        taxids.add(387341);
        taxids.add(316);
        taxids.add(286802);
        taxids.add(390884);
        taxids.add(28901);
        taxids.add(1314);
        taxids.add(1515);
        taxids.add(1502);
        taxids.add(1718);
        taxids.add(562);
        taxids.add(54736);
        taxids.add(2014);

        /*Map<Integer, Integer> counts = analyzer.getReadCount("taxcounts.txt", taxids);

        for (Integer integer : counts.keySet()) {
            System.out.println(integer + "\t" + treehitcounter.getTaxon(integer).getScientificName() + "\t" + counts.get(integer));
        }*/

        Map<Integer, Integer> counts = analyzer.getReadCount("taxcounts.txt", taxids);

        for (Integer integer : counts.keySet()) {
            System.out.println(integer + "\t" + taxonomy.getTaxon(integer).getScientificName() + "\t" + counts.get(integer));
        }
    }
}
