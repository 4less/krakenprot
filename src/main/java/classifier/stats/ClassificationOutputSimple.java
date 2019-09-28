package classifier.stats;

import classifier.treehitcounter.TaxonCounterTree;
import sequence.FastxRecord;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by joachim on 20.08.19.
 */
public class ClassificationOutputSimple implements ClassificationOutput {
    private BufferedWriter writer;

    public ClassificationOutputSimple(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public void printHeader() {
        try {
            writer.write("classified\theader\tguessedId\tbp\tcount\tvar\ttcounts");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void writeLine(ClassificationResult result, TaxonCounter rftCounter, FastxRecord[] records) {
        int id = result.getTaxid();

        try {
            // Classified
            if (id > 0)
                writer.write("C\t");
            else
                writer.write("U\t");

            int bp = 0;

            for (int i = 0; i < records.length; i++)
                bp += records[i].getSequence().length();

            writer.write(records[0].getHeader()+ "\t"); //HEADER
            writer.write(result.getTaxid() + "\t"); //GuessedTID
            writer.write(Integer.toString(bp) + "\t"); // BP
            writer.write(Long.toString(result.getCounter().getTotalCount()) + "\t"); //Total identified
            writer.write(Long.toString(result.getCounter().getTaxIds().size()) + "\t"); //Total identified
            writer.write(getTaxonCountsString(result.getCounter())); //Mapping
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTaxonCountsString(TaxonCounterTree counter) {
        StringBuilder builder = new StringBuilder();
        for (Integer taxid : counter.getTaxIds()) {
            builder.append(taxid);
            builder.append(":");
            builder.append(counter.get(taxid));
            builder.append(",");
        }
        if (builder.length()>0)
            builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    @Override
    public synchronized void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
