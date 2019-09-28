package classifier.treehitcounter;

import java.util.Comparator;

/**
 * Created by joachim on 02.08.19.
 */
public class TaxonCounterTreeVarianceComparator implements Comparator<TaxonCounterTree> {
    @Override
    public int compare(TaxonCounterTree taxonCounter, TaxonCounterTree t1) {
        if (taxonCounter.getTaxIds().size() > t1.getTaxIds().size()) return -1;
        if (taxonCounter.getTaxIds().size() < t1.getTaxIds().size()) return 1;
        return 0;
    }
}
