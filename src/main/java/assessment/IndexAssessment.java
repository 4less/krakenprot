package assessment;


import accession.AccessionMap;
import accession.SQLiteAccessionMap;
import index.indexbuilder.SimpleUpdateReceiver;
import index.indexdb.IndexLoader;
import index.indexdb.SplitLongIndex;
import sequence.BufferedFastaReader;
import sequence.FastxReader;
import taxonomy.NCBITaxonomy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joachim on 11.06.19.
 */
public class IndexAssessment {
    private IndexLoader index;
    private NCBITaxonomy taxonomy;
    private FastxReader reader;
    private AccessionMap accession;
    private int k;
    private String output;
    private BufferedWriter writer = null;

    private List<Thread> threads = new ArrayList<>();

    public IndexAssessment(IndexLoader index, NCBITaxonomy taxonomy, FastxReader reader, AccessionMap accession, String output) {
        this.index = index;
        this.taxonomy = taxonomy;
        this.reader = reader;
        this.accession = accession;
        this.output = output;
        this.k = index.getK();

        try {
            this.writer = new BufferedWriter(new FileWriter(this.output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(int threadCount) {
        IndexAssessmentTask task = null;
        for (int i = 0; i < threadCount; i++) {
            task = new IndexAssessmentTask(taxonomy, index, accession, reader);
            Thread t = new Thread(task);
            threads.add(t);
            t.start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }
        // Try shutdown report writer
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write result
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write("rank\tcount");
            writer.newLine();
            for (String s : task.rankMap.keySet()) {
                writer.write(s + "\t" + task.rankMap.get(s));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NCBITaxonomy taxonomy = new NCBITaxonomy(new File("nodes.dmp"), new File("names.dmp"));
        AccessionMap accession = new SQLiteAccessionMap("accession2tid.db");
        FastxReader reader = null;
        IndexLoader index = new SplitLongIndex();

        ((SplitLongIndex)index).addUpdateReceiver(new SimpleUpdateReceiver());
        ((SplitLongIndex)index).setTimer(3000);

        index.load("index/k10_1");

        try {
            reader = new BufferedFastaReader(new File("ref/nr.chlam.filt.fa"));
            reader.addUpdateReceiver(new SimpleUpdateReceiver());
            reader.setTimer(3000);

        } catch (IOException e) {
            e.printStackTrace();
        }

        IndexAssessment indexAssessment =  new IndexAssessment(index, taxonomy, reader, accession, "assessment/" + "/k10_old.tsv");
        indexAssessment.run(1);
    }
}
