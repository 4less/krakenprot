package taxonomy;

/**
 * Created by joachim on 10.07.19.
 */
public interface SimpleTaxonomy {
    int getParent(int taxid);
    int getLCA(int taxid1, int taxid2);
    boolean contains(int id);
}
