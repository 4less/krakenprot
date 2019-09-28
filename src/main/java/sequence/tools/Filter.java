package sequence.tools;

import accession.AccessionMap;
import accession.SQLiteAccessionMap;
import index.indexbuilder.SimpleUpdateReceiver;
import sequence.BufferedFastaReader;
import sequence.BufferedFastaWriter;
import sequence.FastaRecord;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by joachim on 26.08.19.
 */
public class Filter {
    private BufferedFastaReader reader;
    private BufferedFastaWriter writer;
    private long counter = 1;
    private Set<Character> alphabet;

    public Filter(BufferedFastaReader reader, String output,Set<Character> alphabet) {
        this.reader = reader;
        this.writer = new BufferedFastaWriter(output);
        this.alphabet = alphabet;
    }

    public void convert() throws IOException {
        FastaRecord record;
        int taxid;
        FastaRecord newRecord;
        boolean take;

        while ((record = reader.readRecord()) != null) {
            take = true;

            for (char c : record.getSequence().toCharArray()) {
                if (!alphabet.contains(c)) {
                    take = false;
                    break;
                }
            }
            if (take) {
                newRecord = new FastaRecord(record.getHeader(), record.getSequence());
                counter++;
                writer.write(newRecord);
            }
        }

        writer.close();
    }

    public static void main(String[] args) {

        Set<Character> alphabet = new HashSet();
        for (char c : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
            alphabet.add(c);
        }
        try {
            BufferedFastaReader reader = new BufferedFastaReader(new File("ref/nr.chlam.fa"));

            reader.addUpdateReceiver(new SimpleUpdateReceiver());
            reader.setTimer(3000l);

            String output = "ref/nr.chlam.filt.fa";
            SQLiteAccessionMap accessionMap = new SQLiteAccessionMap("accession2tid.db");
            Filter kaijuconv = new Filter(reader, output, alphabet);
            kaijuconv.convert();

            reader.cancelTimer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
