package classifier.stats;

import classifier.treehitcounter.TaxonCounterTree;

/**
 * Created by joachim on 23.07.19.
 */
public class ClassificationResult {
    private int taxid;
    private TaxonCounterTree counter;

    public ClassificationResult(int taxid, TaxonCounterTree counter) {
        this.taxid = taxid;
        this.counter = counter;
    }

    public int getTaxid() {
        return taxid;
    }

    public TaxonCounterTree getCounter() {
        return counter;
    }
}
