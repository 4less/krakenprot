package sequence.encoding;

/**
 * Created by joachim on 01.09.19.
 */
public interface IEncoding {
    String getEncodingString();
    long kmerToLong(String kmer);
    long kmerToLong(char[] sequence, int index, int k);
    String reduce(String sequence);
    char reduce(char aa);
    void reduce(char[] sequence);
    String longToKmer(long kmer, int k);
    char[] getAlphabet();
    char getFirstChar(long kmer, int k);
}
