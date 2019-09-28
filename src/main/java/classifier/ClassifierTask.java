package classifier;

import classifier.stats.*;
import index.indexdb.IndexLoader;
import sequence.FastxReader;
import sequence.FastxRecord;
import sequence.Translation;

import java.io.IOException;

/**
 * Created by joachim on 19.08.19.
 */
public class ClassifierTask implements Runnable {
    private IndexLoader index;
    private FastxReader reader;
    private FastxReader reader2;

    private static Object peLock = new Object();

    private TaxonCounter taxonCounter;
    private CountProcessor processor;
    private ClassificationOutput writer;

    private int k;

    public ClassifierTask (IndexLoader index, FastxReader reader, FastxReader reader2, ClassificationOutput writer, CountProcessor processor, TaxonCounter taxonCounter) {
        this.reader = reader;
        this.reader2 = reader2;
        this.index = index;
        this.taxonCounter = taxonCounter;
        this.writer = writer;
        this.processor = processor;
        this.k = this.index.getK();
    }

    public void processRF(char[] rf, int readingFrame, int readNum) {
        char[] sequence = rf;
        int stopCodonCounter = 0;

        for (int j = 0; j < k && j < sequence.length; j++) {
            if (sequence[j] == '#') {
                stopCodonCounter = j + 2;
            }
        }

        for (int j = 0; j < sequence.length - k + 1; j++) {
            if (sequence[j+k-1] == '#') {
                stopCodonCounter = k;
            } else {
                stopCodonCounter--;
            }
            // If current k-mer contains stop-codon add -1 to taxon counter, else lookup in index.
            taxonCounter.addTaxonId(stopCodonCounter <= 0 ? index.getTaxId(sequence,j) : -1, readNum, readingFrame);
        }
    }


    /**
     * Get next record. If paired end, return two reads, if single end array will only contain one read.
     * @return
     */
    public FastxRecord[] readRecords() {
        FastxRecord[] records = isPairedEnd() ? new FastxRecord[2] : new FastxRecord[1];
        synchronized (peLock) {
            try {
                records[0] = reader.readRecord();
                if (isPairedEnd())
                    records[1] = reader2.readRecord();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return records;
    }

    private boolean recordsValid(FastxRecord[] records) {
        for (FastxRecord record : records)
            if (record == null) return false;
        return true;
    }

    private boolean isPairedEnd() {
        return reader2 != null;
    }

    @Override
    public void run() {
        System.out.println("Run ClassifierTask");

        char[][] rfs;

        FastxRecord[] records = readRecords();

        while (recordsValid(records)) {

            // reset all tree counters
            taxonCounter.clear();

            for (int read = 0; read < records.length; read++) {
                rfs = Translation.extract6ReadingFrames(records[read].getSequence().toCharArray());
                for (int i = 0; i < rfs.length; i++) {
                    char[] sequence = rfs[i];
                    processRF(sequence, i, read);
                }

            }

            ClassificationResult result = processor.getClassificationResult(taxonCounter.getTaxonCounters(), records);
            writer.writeLine(result, taxonCounter, records);

            // Read next pair of reads
            records = readRecords();
        }
    }
}
