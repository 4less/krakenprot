package classifier.treehitcounter;

import com.koloboke.collect.map.IntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import taxonomy.Taxon;
import taxonomy.Taxonomy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by joachim on 16.07.19.
 */
public class TaxonCounterTree {
    private IntIntMap counter = HashIntIntMaps.newMutableMap();
    private Taxonomy taxonomy;
    private int highestCount = 0;

    public TaxonCounterTree(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    public void increment(int taxonid) {
        incrementBy(taxonid, 1);
    }

    public void incrementBy(int taxonid, int count) {
        synchronized (counter) {
            if (counter.containsKey(taxonid)) {
                counter.put(taxonid, counter.get(taxonid)+count);
            } else {
                counter.put(taxonid,count);
            }
        }
    }

    public int get(int taxonId) {
        return counter.get(taxonId);
    }

    public int get(Taxon taxon) {
        return get(taxon.getId());
    }

    public void clear() {
        counter.clear();
        highestCount = 0;
    }

    public List<Taxon> getNodes() {
        List<Taxon> list = new ArrayList<>();
        Taxon taxon;
        for (Integer taxonid : counter.keySet()) {
            taxon = null;
            if (taxonid > 0)
                taxon = taxonomy.getTaxon(taxonid);
            if (taxon != null)
                list.add(taxon);
            //else
            //    System.out.println("taxon is null in getNodes(): " + taxonid);
        }
        return list;
    }

    public Set<Integer> getTaxIds() {
        return counter.keySet().stream().filter(i -> i > 0).collect(Collectors.toSet());
    }

    public long getTotalCount() {
        long count = 0;
        for (Integer key : counter.keySet()) {
            if (key > 0)
                count += counter.get(key.intValue());
        }
        return count;
    }

    public int getLCAOfMaxRTLPath() {
        List<Taxon> nodes = getNodes();

        if (nodes.isEmpty())
            return -1;

        Collections.sort(nodes);

        Taxon node;
        Taxon ancestor;
        for (int i = 0; i < nodes.size(); i++) {
            node = nodes.get(i);

            // Increase node counter
            if (get(node) > highestCount)
                highestCount = get(node);

            for (int j = i-1; j >= 0; j--) {
                ancestor = nodes.get(j);

                if (isAncesterOf(ancestor, node)) {
                    incrementBy(node.getId(), get(ancestor));

                    // Increase node counter
                    if (get(node) > highestCount)
                        highestCount = get(node);

                    break;
                }
            }
        }

        List<Taxon> highCountNodes = new ArrayList<>();

        for (Taxon taxon : nodes) {
            if (get(taxon) == highestCount)
                highCountNodes.add(taxon);
        }


        int lca = lowestCommonAncesterNodes(highCountNodes);

        if (lca == -1) {
            for (Integer integer : counter.keySet()) {
                System.out.println(integer + " -> " + counter.get(integer.intValue()));
            }
        }

        return lca;
    }

    public IntIntMap getCounter() {
        return counter;
    }

    public boolean isAncesterOf(Taxon node1, Taxon node2) {
        if (node2.getLayer() <= node1.getLayer() || node1.getId() == node2.getId())
            return false;

        List<Taxon> ptr = getNodePathToRoot(node2);
        if (ptr.contains(node1))
            return true;
        return false;
    }

    public int lowestCommonAncesterNodes(List<Taxon> nodes) {
        if (nodes.isEmpty()) {
            System.out.println("nodelist is empty");
            return -1;
        }

        Taxon node1;
        Taxon node2;
        Taxon lcaNode;
        int lca;
        while (nodes.size() > 1) {

            node1 = nodes.remove(0);
            node2 = nodes.remove(0);
            lca = taxonomy.getLCA(node1.getId(), node2.getId());
            lcaNode = taxonomy.getTaxon(lca);

            nodes.add(lcaNode);
        }
        return nodes.get(0).getId();
    }

    public void printCounts() {
        System.out.println("print counts: ");
        for (Integer integer : counter.keySet()) {
            System.out.println(integer + "(" + counter.get(integer.intValue()) + "), ");
        }
        System.out.println();
    }

    public List<Taxon> getNodePathToRoot(Taxon node) {
        List<Taxon> path = new ArrayList<>();

        Taxon n = node;
        while (n.getId() != 1) {
            path.add(n);
            n = taxonomy.getTaxon(n.getParentId());
        }
        return path;
    }

    public void printCounter() {
        for (Integer integer : counter.keySet()) {
            System.out.println("key " + integer + ": " + counter.get(integer));
        }
    }


    public static void main(String[] args) {
        System.out.println("Test set behaviour");
        Set<Integer> set = new HashSet<>();
        set.add(11);
        set.add(4);
        set.add(0);
        set.add(-1);
        set.add(24);

        set = set.stream().filter(i -> i > 0).collect(Collectors.toSet());

        for (Integer integer : set) {
            System.out.println(integer);
        }

    }
}
