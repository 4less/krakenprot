package taxonomy;

import com.koloboke.collect.map.IntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by joachim on 10.07.19.
 */
public class SimpleNCBITaxonomy implements SimpleTaxonomy {
    private final IntIntMap childParentMap;

    public SimpleNCBITaxonomy(String nodesPath) {
        this.childParentMap = getTaxonomyMap(new File(nodesPath));
    }

    public SimpleNCBITaxonomy(File nodes) {
        this.childParentMap = getTaxonomyMap(nodes);
    }

    public int getParent(int taxid) {
        return childParentMap.get(taxid);
    }

    public int getLCA(int taxid1, int taxid2) {
        return lowestCommonAncester(taxid1, taxid2);
    }

    @Override
    public boolean contains(int id) {
        return childParentMap.containsKey(id);
    }

    /***
     * Load Map with nodes.dmp containing key value pairs in the form taxonid -> parent taxonid
     * @param nodes
     * @return
     */
    public static IntIntMap getTaxonomyMap(File nodes) {
        IntIntMap map = HashIntIntMaps.newMutableMap();

        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(nodes);
            br = new BufferedReader(fr);

            String curLine;
            Scanner reader = new Scanner (System.in);

            int linenr = -1;

            while ((curLine = br.readLine()) != null) {
                linenr++;
                String[] fields = curLine.split("\t\\|\t|\t\\|");
                map.put(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    public ArrayList<Integer> getPathToRoot(int taxid) {
        ArrayList<Integer> path = new ArrayList<Integer>();

        int parent = taxid;

        path.add(taxid);

        do {
            parent = getParent(parent);
            path.add(parent);
        } while (parent != 1);

        return path;
    }

    public int lowestCommonAncester (int t1, int t2) {
        if (!childParentMap.containsKey(t1) || !childParentMap.containsKey(t2))
            return -1;

        if (t1 == t2) return t1;

        ArrayList<Integer> path1 = getPathToRoot(t1);
        ArrayList<Integer> path2 = getPathToRoot(t2);

        int lca = 1;

        for (int i = 0; i < path1.size() && i < path2.size(); i++) {
            if (path1.get(path1.size() - 1 - i).equals(path2.get(path2.size() - 1 - i))) {
                lca = path1.get(path1.size() - 1 - i);
            } else {
                return lca;
            }
        }
        return lca;
    }

    public static void main(String[] args) {

        File nodes = new File(
                Thread.currentThread().getContextClassLoader().getResource("nodes.dmp").getFile()
        );

        SimpleTaxonomy taxonomy = new SimpleNCBITaxonomy(nodes);

        System.out.println(taxonomy.getParent(1515));
        System.out.println("lca 1515 1515: \t\t" + taxonomy.getLCA(1515, 1515));
        System.out.println("lca 1515 1502: \t\t" + taxonomy.getLCA(1515, 1502));
        System.out.println("lca 79209 102134: \t\t" + taxonomy.getLCA(79209, 102134));
    }
}
