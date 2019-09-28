package classifier.stats;

import classifier.treehitcounter.TaxonCounterTreeVerbose;
import classifier.treehitcounter.TaxonCounterTree;
import sequence.FastxRecord;
import taxonomy.NCBITaxonomy;

import java.util.*;

/**
 * Created by joachim on 19.08.19.
 */
public class CountProcessorBest implements CountProcessor {
    private NCBITaxonomy taxonomy;
    private TaxonCounterTreeVerbose resultCounter;
    private FastxRecord[] fastxRecords;
    private int threshold = 0;

    private boolean print = false;

    //test purposes
    //private static int count = 0;
    //private TaxonCounterTreeVerbose resultCounterAll;

    public CountProcessorBest(NCBITaxonomy taxonomy) {
        this.taxonomy = taxonomy;
        this.resultCounter = new TaxonCounterTreeVerbose(taxonomy);

        //Test
        //this.resultCounterAll = new TaxonCounterTreeVerbose(treehitcounter);
    }

    public CountProcessorBest(NCBITaxonomy taxonomy, int threshold) {
        this.taxonomy = taxonomy;
        this.resultCounter = new TaxonCounterTreeVerbose(taxonomy);
        this.threshold = threshold;

        //Test
        //this.resultCounterAll = new TaxonCounterTreeVerbose(treehitcounter);
    }

    @Override
    public ClassificationResult getClassificationResult(TaxonCounterTree[][] taxonCounters, FastxRecord... records) {
        resultCounter.clear();

        //TEST
        //resultCounterAll.clear();

        this.fastxRecords = records;

        int reads = taxonCounters.length;
        //int readingFrames = taxonCounters[0].length;

        ///List<Long> counts = new ArrayList<>();
        //for (TaxonCounterTree taxonCounter : taxonCounters[0])
        //    counts.add(taxonCounter.getTotalCount());
        //Log.getInstance().incrementHit(counts);
        //Log.getInstance().incrementRead();


        /*if (print) {
            print(taxonCounters, records[0]);
            System.out.println("LENGTH");
            Collections.sort(counts);
            Collections.reverse(counts);
            System.out.println(counts);
        }*/


        if (reads == 1) {
            int maxi = getMaxCountIndex(taxonCounters[0]);
            TaxonCounterTreeVerbose tc = (TaxonCounterTreeVerbose)taxonCounters[0][maxi];
            int taxid;

            for (Integer taxon : tc.getCounter().keySet())
                resultCounter.incrementBy(taxon, tc.get(taxon));

            // if read would be classified but is lower than threshold set taxid to -2
            if (resultCounter.getTotalCount() < threshold && resultCounter.getTotalCount() > 0)
                taxid = -2;
            else
                taxid = resultCounter.getLCAOfMaxRTLPath();

            return new ClassificationResult(taxid, tc);

        } else if (reads == 2) {
            int maxi1 = getMaxCountIndex(taxonCounters[0]);
            int maxi2 = getMaxCountIndex(taxonCounters[1]);

            TaxonCounterTreeVerbose tc1 = (TaxonCounterTreeVerbose)taxonCounters[0][maxi1];
            TaxonCounterTreeVerbose tc2 = (TaxonCounterTreeVerbose)taxonCounters[1][maxi2];

            // Take max hit reading frames for both hits and merge them
            for (Integer taxon : tc1.getCounter().keySet()) {
                resultCounter.incrementBy(taxon, tc1.get(taxon));
            }
            for (Integer taxon : tc2.getCounter().keySet()) {
                resultCounter.incrementBy(taxon, tc2.get(taxon));
            }
            resultCounter.addToClassificationList(tc1.getClassificationList());
            resultCounter.addToClassificationList(tc2.getClassificationList());

            // if read would be classified but is lower than threshold set taxid to -2
            int taxid;
            if (resultCounter.getTotalCount() < threshold && resultCounter.getTotalCount() > 0)
                taxid = -2;
            else
                taxid = resultCounter.getLCAOfMaxRTLPath();

            return new ClassificationResult(taxid, resultCounter);
        }

        return null;
    }

    public void print(TaxonCounterTree[][] taxonCounters, FastxRecord record) {
        System.out.println("SE?PE. " + taxonCounters.length);
        int maxi1 = getMaxCountIndex(taxonCounters[0]);

        System.out.println(record.getHeader());
        for (int i = 0; i < taxonCounters.length; i++) {
            for (int j = 0; j < taxonCounters[0].length; j++) {
                System.out.println("taxon counter. read: " + i + ", rf: " + j + ", hits: " + taxonCounters[i][j].getTotalCount() + ", var: " + taxonCounters[i][j].getTaxIds().size());
                for (Integer key : taxonCounters[i][j].getCounter().keySet()) {
                    System.out.print(key + ": " + taxonCounters[i][j].get(key) + ", ");
                }
                System.out.println();
            }
        }
        System.out.println("maxRF: " + maxi1);
        TaxonCounterTree tc = taxonCounters[0][maxi1];
        System.out.println("taxonCounter " + maxi1);
        for (Integer integer : tc.getCounter().keySet())
            resultCounter.incrementBy(integer, tc.get(integer));

        System.out.println("result: " + resultCounter.getLCAOfMaxRTLPath());
        resultCounter.clear();


        System.out.println("\nOther approach");

        for (TaxonCounterTree[] taxonCounter : taxonCounters) {
            for (TaxonCounterTree counter : taxonCounter) {
                for (Integer integer : counter.getCounter().keySet()) {
                    resultCounter.incrementBy(integer, counter.get(integer));
                }
            }
        }
        System.out.println("taxon counter pooled.  hits: " + resultCounter.getTotalCount() + ", var: " + resultCounter.getTaxIds().size());
        for (Integer key : resultCounter.getCounter().keySet()) {
            System.out.print(key + ": " + resultCounter.get(key) + ", ");
        }
        System.out.println();
        System.out.println("result: " + resultCounter.getLCAOfMaxRTLPath());

        System.out.println();
        resultCounter.clear();

        System.out.println("wait: ...");
        Scanner sc = new Scanner(System.in);
        String result = sc.nextLine();
        if (result.equals("1"))
            print = false;
        System.out.println("continue..");
    }

    private int getMaxCountIndex(TaxonCounterTree[] taxonCounters) {
        long max = 0;
        int maxi = 0;
        for (int i = 0; i < taxonCounters.length; i++) {
            if (taxonCounters[i].getTotalCount() > max) {
                max = taxonCounters[i].getTotalCount();
                maxi = i;
            }
        }
        return maxi;
    }
}
