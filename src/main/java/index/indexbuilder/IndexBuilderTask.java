package index.indexbuilder;

import accession.AccessionMap;
import index.indexdb.IndexStore;
import sequence.FastaRecord;
import sequence.FastxReader;
import taxonomy.SimpleTaxonomy;

/**
 * Created by joachim on 19.06.19.
 */
public class IndexBuilderTask implements Runnable {
    private FastxReader reader;
    private SimpleTaxonomy taxonomy;
    private AccessionMap accession;
    private IndexStore index;
    private int k;

    public IndexBuilderTask(FastxReader reader, SimpleTaxonomy taxonomy, AccessionMap accession, IndexStore index, int k) {
        this.reader = reader;
        this.taxonomy = taxonomy;
        this.accession = accession;
        this.index = index;
        this.k = k;
    }

    @Override
    public void run() {
        try {
            // Variables for sequence array and taxon id
            char[] sequence;
            Integer id;

            int count = 0;

            while (true) {
                // get next record
                FastaRecord record = (FastaRecord)reader.readRecord();

                // stop when file has ended
                if (record == null) {
                    //index.finalize();
                    break;
                }
                count++;

                // extract sequence array and get taxon id
                sequence = record.getSequence().toCharArray();
                id = accession.getTaxId(record.getNrId());

                // iterate sequence and store kmers in index database
                for (int i = 0; i < sequence.length - k + 1; i++)
                    index.put(sequence, i, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
