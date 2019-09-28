package classifier.stats;

import classifier.treehitcounter.TaxonCounterTreeVerbose;
import classifier.treehitcounter.TaxonCounterTree;
import taxonomy.NCBITaxonomy;

/**
 * Created by joachim on 19.08.19.
 */
public class TaxonCounterBest implements TaxonCounter {
    private int readingFrames;
    private int reads;

    private NCBITaxonomy taxonomy;


    private TaxonCounterTree[][] taxonCounters;

    public TaxonCounterBest(int reads, int readingFrames, NCBITaxonomy taxonomy) {
        this.readingFrames = readingFrames;
        this.reads = reads;
        taxonCounters = new TaxonCounterTree[reads][readingFrames];

        initCounters();
    }

    private void initCounters() {
        for (int i = 0; i < reads; i++) {
            for (int j = 0; j < readingFrames; j++) {
                taxonCounters[i][j] = new TaxonCounterTreeVerbose(taxonomy);
            }
        }
    }

    public void clear() {
        for (int i = 0; i < reads; i++) {
            for (int j = 0; j < readingFrames; j++) {
                taxonCounters[i][j].clear();
            }
        }
    }

    public TaxonCounterTree[][] getTaxonCounters() {
        return taxonCounters;
    }

    @Override
    public void addTaxonId (int taxonId, int read, int readingFrame) {
        taxonCounters[read][readingFrame].increment(taxonId);
    }
}
