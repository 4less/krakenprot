package sequence;

import accession.AccessionMap;
import accession.SQLiteAccessionMap;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by joachim on 16.03.19.
 */
public class FastaRecord extends FastxRecord {
    private String nrId;

    public FastaRecord(String header, String sequence) {
        super(header, sequence);
        extractNrId();
    }

    public void extractNrId() {
        if (header != null && header.length() >= 15) {
            int index = header.indexOf(".");
            if (index < 0) {
                nrId = null;
            } else {
                nrId = header.substring(1, header.indexOf("."));
            }
        }
    }

    public String getSubstring(int index, int k) {
        if (index+k <= sequence.length()) {
            return new String(sequence.toCharArray(), index, index+k);
        }
        return "";
    }

    public String getNrId() {
        return nrId;
    }

    public boolean isEmpty() {
        return (header.length() == 0) && (sequence.length()== 0);
    }

    @Override
    public void print() {
        System.out.println(header);
        System.out.println("nrId: " + nrId);
        System.out.println(new String(sequence));
    }

    public static void main(String[] args) {
        AccessionMap map = new SQLiteAccessionMap("accession2tid.db");
        try {
            BufferedFastaReader reader = new BufferedFastaReader(new File("ref/nr.chlam.filt.fa"));

            FastaRecord record;

            while((record=reader.readRecord()) != null) {
                System.out.println(record.getHeader());
                System.out.println(record.getNrId());
                System.out.println(map.getTaxId(record.getNrId()));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}