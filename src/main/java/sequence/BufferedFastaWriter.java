package sequence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by joachim on 17.07.19.
 */
public class BufferedFastaWriter {
    private BufferedWriter writer;
    private boolean wroteFirst = false;

    public BufferedFastaWriter(String file) {
        try {
            this.writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(FastaRecord record) {
        try {
            if (wroteFirst) writer.newLine();
            else wroteFirst = true;

            writer.write(record.getHeader());
            writer.newLine();
            writer.write(record.getSequence());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            BufferedFastaReader reader = new BufferedFastaReader(new File("index_chlam/nr.chlam.10000.fa"));
            BufferedFastaWriter writer = new BufferedFastaWriter("index_chlam/nr.chlam.10000.dup.fa");

            FastaRecord record;

            while ((record = (FastaRecord)reader.readRecord())!= null) {
                writer.write(record);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
