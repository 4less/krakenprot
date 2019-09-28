package index.indexdb;

/**
 * Created by joachim on 15.07.19.
 */
public interface IndexStore {
    void put(String s, Integer taxid);
    void put(char[] sequence, int index, int taxid);
    void shutdown();
}
