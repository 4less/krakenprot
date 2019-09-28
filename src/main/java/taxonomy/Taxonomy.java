package taxonomy;

import java.util.List;

/**
 * Created by joachim on 10.07.19.
 */
public interface Taxonomy extends SimpleTaxonomy {
    Taxon getParentTaxon(int taxid);
    Taxon getLCA(Taxon rt1, Taxon t2);
    Taxon getTaxon(int taxid);
    List<TaxonName> getTaxonNames(int taxid);
    Taxon getTaxon(String name);
}
