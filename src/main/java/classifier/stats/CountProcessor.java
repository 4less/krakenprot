package classifier.stats;

import classifier.treehitcounter.TaxonCounterTree;
import sequence.FastxRecord;

/**
 * Created by joachim on 19.08.19.
 */
public interface CountProcessor {
    ClassificationResult getClassificationResult(TaxonCounterTree[][] taxonCounters, FastxRecord... records);
}
