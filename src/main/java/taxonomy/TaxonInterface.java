package taxonomy;

import java.util.List;

/**
 * Created by joachim on 11.07.19.
 */
public interface TaxonInterface {
    boolean isLeaf();

    int getId();

    int getParentId();

    List<Taxon> getChildren();

    TAXONOMIC_RANK getRank();

    Taxon getParent();

    int isChildOf(Taxon taxon);
}
