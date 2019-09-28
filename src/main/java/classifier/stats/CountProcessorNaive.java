package classifier.stats;

import classifier.treehitcounter.TaxonCounterTree;
import sequence.FastxRecord;

/**
 * Created by joachim on 19.08.19.
 */
public class CountProcessorNaive implements CountProcessor {
    private int threshold = 0;

    public CountProcessorNaive(int threshold) {
        this.threshold = threshold;
    }

    public CountProcessorNaive() {}

    @Override
    public ClassificationResult getClassificationResult(TaxonCounterTree[][] taxonCounters, FastxRecord... records) {
        if (taxonCounters.length > 1 && taxonCounters[0].length > 1) {
            System.out.println("Caution! Do not use CountProcessorNaive when providing more than one counter. CountProcessorNaive only takes one taxonCounter that contains counts for every reading frame.");
            System.exit(0);
        }

        TaxonCounterTree counter = taxonCounters[0][0];
        // Classify read
        int taxid = counter.getLCAOfMaxRTLPath();

        if (counter.getTotalCount() > 0 && counter.getTotalCount() < threshold) taxid = -2;

        // Return classification Result
        return new ClassificationResult(taxid, counter);
    }
}
