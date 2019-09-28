package taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joachim on 10.07.19.
 */
public class Taxon implements TaxonInterface, Comparable {
    private int taxonId;
    private Taxon parent;
    private int parentId;
    private TAXONOMIC_RANK rank;
    private ArrayList<Taxon> children = new ArrayList<Taxon>();
    private ArrayList<TaxonName> names = new ArrayList<TaxonName>();
    private int layer;

    public Taxon(int taxonId) {
        this.taxonId = taxonId;
    }

    public Taxon(int taxonId, int parent, TAXONOMIC_RANK rank) {
        this.taxonId = taxonId;
        this.parent = null;
        this.parentId = parent;
        this.rank = rank;
    }

    public void addName(TaxonName name) {
        this.names.add(name);
    }

    public String getFirstName() {
        return names.get(0).getName();
    }

    public String getScientificName() {
        for (TaxonName taxonName : names) {
            if (taxonName.getType().equals("scientific name")) {
                return taxonName.getName();
            }
        }
        return getFirstName();
    }

    @Override
    public int compareTo(Object o) {
        Taxon node = (Taxon)o;

        if (node.getLayer() > getLayer())
            return -1;
        else if (node.getLayer() < getLayer())
            return 1;
        else return 0;
    }


    @Override
    public boolean isLeaf() {
        return children.isEmpty();
    }


    @Override
    public int getId() {
        return taxonId;
    }
    @Override
    public int getParentId() {
        return parentId;
    }

    public void setParentId(int id) {
        this.parentId = id;
    }

    public void addChild(Taxon taxon) {
        children.add(taxon);
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public List<Taxon> getChildren() {
        return children;
    }

    public ArrayList<TaxonName> getNames() {
        return names;
    }

    @Override
    public TAXONOMIC_RANK getRank() {
        return rank;
    }

    public void setRank(TAXONOMIC_RANK rank) {
        this.rank = rank;
    }

    public void setParent(Taxon parent) {
        this.parent = parent;
    }

    @Override
    public Taxon getParent() {
        return parent;
    }

    @Override
    public int isChildOf(Taxon taxon) {
        if (taxon == null)
            return -1;

        int dist = 0;

        Taxon child = this;


        while (child.getLayer() > taxon.getLayer()) {
            dist++;
            child = child.getParent();
        }

        if (child == taxon) return dist;
        else return -1;
    }

    public int getLayer() {
        return layer;
    }
}
