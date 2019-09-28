package assessment;

import accession.AccessionMap;
import classifier.treehitcounter.TaxonCounterTree;
import index.indexdb.IndexLoader;
import logger.Log;
import sequence.FastaRecord;
import sequence.FastxReader;
import sequence.encoding.IEncoding;
import taxonomy.NCBITaxonomy;
import taxonomy.TAXONOMIC_RANK;
import taxonomy.Taxon;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by joachim on 11.06.19.
 */

/*

* @startuml

* car --|> wheel

* @enduml

*/

public class IndexAssessmentTask implements Runnable {
    private NCBITaxonomy taxonomy;
    private IndexLoader index;
    private AccessionMap accession;
    private FastxReader reader;
    private int k;
    private IEncoding encoder;
    private TaxonCounterTree counter;

    public static Set<Integer> relevantRanks = new HashSet<>();

    static {
        relevantRanks.add(TAXONOMIC_RANK.EMPIRE.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.KINGDOM.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.PHYLUM.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.CLASS.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.ORDER.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.FAMILY.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.GENUS.getNumVal());
        relevantRanks.add(TAXONOMIC_RANK.SPECIES.getNumVal());
    }

    public static ConcurrentHashMap<String, Long> rankMap = new ConcurrentHashMap<>();

    public IndexAssessmentTask(NCBITaxonomy taxonomy, IndexLoader index, AccessionMap accession, FastxReader reader) {
        this.taxonomy = taxonomy;
        this.reader = reader;
        this.index = index;
        this.k = index.getK();
        this.accession = accession;
        this.counter = new TaxonCounterTree(taxonomy);
        this.encoder = index.getEncoder();
    }

    public void increment(String rank) {
        if (rankMap.containsKey(rank)) {
            rankMap.put(rank, rankMap.get(rank)+1);
        } else {
            rankMap.put(rank,1l);
        }
    }


    public static TAXONOMIC_RANK getClosestRelevantRank(Taxon taxon) {
        int iter2 = 0;
        while (taxon.getRank() == TAXONOMIC_RANK.NO_RANK && taxon.getId() > 1)
            taxon = taxon.getParent();

        TAXONOMIC_RANK current = taxon.getRank();
        int numVal = current.getNumVal();
        int iter = 0;
        while (!relevantRanks.contains(numVal) || numVal < 0)
            numVal--;
        return TAXONOMIC_RANK.get(numVal);
    }


    @Override
    public void run() {
        FastaRecord record;
        char[] sequence;
        int realTid;
        int guessedTid;
        int taxid;

        try {
            Taxon t;
            TAXONOMIC_RANK rank;

            while (true) {
                record = (FastaRecord)reader.readRecord();

                if (record == null)
                    break;

                if (record.getSequence().length() < k) {
                    continue;
                }

                realTid = accession.getTaxId(record.getNrId());
                sequence = record.getSequence().toCharArray();
                encoder.reduce(sequence);

                for (int j = 0; j < sequence.length - k + 1; j++) {
                    taxid = index.getTaxId(sequence, j);

                    if (taxid == 0 || taxid == -1)
                        System.out.println("tid from index is not valid: " + taxid + " -> " + realTid + " " + sequence[j] + " " + new String(sequence, j, k) + " " + encoder.kmerToLong(sequence, j, k));

                    counter.increment(taxid);
                }

                guessedTid = counter.getLCAOfMaxRTLPath();

                t = taxonomy.getTaxon(guessedTid);
                increment(getClosestRelevantRank(t).getString());
                Log.getInstance().ranks.add(t.getRank());

                counter.clear();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Taxon genus = new Taxon(810, 1, TAXONOMIC_RANK.GENUS);
        Taxon species = new Taxon(813, 810, TAXONOMIC_RANK.SPECIES);
        species.setParent(genus);
        genus.addChild(species);
        System.out.println(species.getRank());
        System.out.println(getClosestRelevantRank(species));
    }
}