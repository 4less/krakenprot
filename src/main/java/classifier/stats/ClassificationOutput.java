package classifier.stats;

import sequence.FastxRecord;

/**
 * Created by joachim on 20.08.19.
 */
public interface ClassificationOutput {
    void printHeader();
    void writeLine(ClassificationResult result, TaxonCounter rftCounter, FastxRecord[] records);
    void close();
}
