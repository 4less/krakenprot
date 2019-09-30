package index.indexbuilder;

import accession.AccessionMap;
import accession.SQLiteAccessionMap;
import index.indexdb.IndexStore;
import index.indexdb.SplitLongIndex;
import sequence.BufferedFastaReader;
import sequence.FastxReader;
import sequence.encoding.Encoding;
import taxonomy.SimpleNCBITaxonomy;
import taxonomy.SimpleTaxonomy;
import utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joachim on 19.06.19.
 */
public class IndexBuilder {
    private SimpleTaxonomy taxonomy;
    private AccessionMap accession;
    private FastxReader reader;
    private IndexStore index;
    private String outputFolder;
    private int k;

    private List<Thread> threads = new ArrayList<>();

    public IndexBuilder(SimpleTaxonomy taxonomy, AccessionMap accession, FastxReader reader, IndexStore index, int k) {
        this.taxonomy = taxonomy;
        this.accession = accession;
        this.reader = reader;
        this.index = index;
        this.outputFolder = outputFolder;
        this.k = k;

    }

    public void build(int threadCount) {

        for (int i = 0; i < threadCount; i++) {
            System.out.println("Start thread " + i);
            Thread t = new Thread(new IndexBuilderTask(reader, taxonomy, accession, index, k));
            threads.add(t);
            t.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        index.shutdown();
    }
}
