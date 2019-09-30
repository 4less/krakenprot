package classifier;


import classifier.stats.*;
import index.indexbuilder.SimpleUpdateReceiver;
import index.indexdb.IndexLoader;
import index.indexdb.SplitLongIndex;
import sequence.BufferedFastaReader;
import sequence.FastxReader;
import taxonomy.NCBITaxonomy;
import utils.Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by joachim on 02.06.19.
 */
public class Classifier {
    private NCBITaxonomy taxonomy;
    private IndexLoader index;
    private FastxReader reader;
    private FastxReader reader2 = null;
    private int threshold;

    private ClassificationOutput writer;

    private final List<Thread> threads = new ArrayList<>();

    private boolean best = false;


    public Classifier(NCBITaxonomy taxonomy, IndexLoader index, FastxReader reader, FastxReader reader2, ClassificationOutput writer) {
        this.taxonomy = taxonomy;
        this.index = index;
        this.reader = reader;
        this.reader2 = reader2;
        this.writer = writer;
    }

    public Classifier(NCBITaxonomy taxonomy, IndexLoader index, FastxReader reader, FastxReader reader2, ClassificationOutput writer, boolean best, int threshold) {
        this.taxonomy = taxonomy;
        this.index = index;
        this.reader = reader;
        this.reader2 = reader2;
        this.writer = writer;
        this.best = best;
        this.threshold = threshold;
    }

    public TaxonCounter generateTaxonCounters() {
        if (best) {
            // Has 6 different counters for the reading frames for each read
            return new TaxonCounterBest(reader2 == null ? 1 : 2, 6, taxonomy);
        }
        // Only has one counter that registers all hits regardless of read or reading frame
        else return new TaxonCounterNaive(taxonomy);
    }

    public CountProcessor generateCountProcessor() {
        // only takes reading frame(s) with the most registered hits for classification (1 for single end, 2 for paired end)
        if (best) return new CountProcessorBest(taxonomy, threshold);
        // takes every reading frame for classification
        else return new CountProcessorNaive(threshold);
    }


    public void run(int threadCount) {
        ClassifierTask task;
        CountProcessor processor;
        Thread thread;

        if (best)
            System.out.println("CountProcessorBest");

        for (int i = 0; i < threadCount; i++) {
            processor = generateCountProcessor();

            task = new ClassifierTask(index, reader, reader2, writer, processor, generateTaxonCounters());
            thread = new Thread(task);

            threads.add(thread);
            thread.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }
        writer.close();


    }
}
