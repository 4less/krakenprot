package classifier.stats;

import classifier.treehitcounter.TaxonCounterTreeVerbose;
import classifier.treehitcounter.TaxonCounterTree;
import taxonomy.NCBITaxonomy;

/**
 * Created by joachim on 02.09.19.
 */
public class TaxonCounterNaive implements TaxonCounter {
    private TaxonCounterTreeVerbose taxonCounter;

    public TaxonCounterNaive(NCBITaxonomy taxonomy) {
        this.taxonCounter = new TaxonCounterTreeVerbose(taxonomy);
    }

    @Override
    public void addTaxonId(int taxId, int read, int readingframe) {
        taxonCounter.increment(taxId);
    }

    @Override
    public TaxonCounterTree[][] getTaxonCounters() {
        return new TaxonCounterTree[][] {{taxonCounter}};
    }

    @Override
    public void clear() {
        taxonCounter.clear();
    }
}
