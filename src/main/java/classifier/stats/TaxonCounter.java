package classifier.stats;

import classifier.treehitcounter.TaxonCounterTree;

/**
 * Created by joachim on 02.09.19.
 */
public interface TaxonCounter {
    void addTaxonId(int id, int read, int readingframe);
    TaxonCounterTree[][] getTaxonCounters();
    void clear();
}
