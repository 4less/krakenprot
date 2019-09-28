package sequence;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by joachim on 01.08.19.
 */
public abstract class FastxRecord {
    protected String header;
    protected String sequence;

    public FastxRecord(String header, String sequence) {
        this.header = header;
        this.sequence = sequence;
    }

    public String getHeader() {
        return header;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public void print() {
        System.out.println(header);
        System.out.println(sequence);
    }
}
