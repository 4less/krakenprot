package classifier.treehitcounter;

import java.util.Comparator;

/**
 * Created by joachim on 27.07.19.
 */
public class TaxonCounterTreeComparator implements Comparator<TaxonCounterTree> {
    @Override
    public int compare(TaxonCounterTree taxonCounter, TaxonCounterTree t1) {
        if (taxonCounter.getTotalCount() > t1.getTotalCount()) return -1;
        if (taxonCounter.getTotalCount() < t1.getTotalCount()) return 1;
        return 0;
    }
}
