package sequence;

/**
 * Created by joachim on 19.07.19.
 */
public class FastqRecord extends FastxRecord{
    private String quality;

    public FastqRecord(String header, String sequence, String quality) {
        super(header, sequence);
        this.quality = quality;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public void print() {
        System.out.println(header);
        System.out.println(sequence);
        System.out.println("+");
        System.out.println(quality);
    }
}
