package index.indexdb;

import sequence.encoding.Encoding;
import sequence.encoding.IEncoding;

import java.util.List;
import java.util.Set;

/**
 * Created by joachim on 15.07.19.
 */
public interface IndexLoader {
    int getTaxId(char[] sequence, int index);
    int getTaxId(String sequence);
    int getTaxId(long kmer);
    IEncoding getEncoder();
    void load(String path);
    int getK();
    String getPath();
    Set<Long> getKeySet();
    List<Integer> getTaxIds(List<Long> ids);
}
