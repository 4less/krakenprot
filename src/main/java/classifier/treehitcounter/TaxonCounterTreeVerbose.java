package classifier.treehitcounter;

import taxonomy.Taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joachim on 05.08.19.
 */
public class TaxonCounterTreeVerbose extends TaxonCounterTree {
    private List<Integer[]> classificationList = new ArrayList<>();

    public TaxonCounterTreeVerbose(Taxonomy taxonomy) {
        super(taxonomy);
    }

    @Override
    public void increment(int taxonid) {
        super.increment(taxonid);

        Integer[] lastItem = null;
        if (!classificationList.isEmpty())
             lastItem = classificationList.get(classificationList.size()-1);
        if (!classificationList.isEmpty() && lastItem[0] == taxonid) {
            lastItem[1]++;
        } else {
            Integer[] item = new Integer[2];
            item[0] = taxonid;
            item[1] = 1;
            classificationList.add(item);
        }
    }

    public String getClassificationString() {
        StringBuilder sb = new StringBuilder();
        Integer[] item;
        if (!classificationList.isEmpty()) {
            item = classificationList.get(0);
            sb.append(item[0]);
            sb.append(':');
            sb.append(item[1]);
        }
        for (int i = 1; i < classificationList.size(); i++) {
            item = classificationList.get(i);
            sb.append(" ");
            sb.append(item[0]);
            sb.append(':');
            sb.append(item[1]);
        }
        return sb.toString();
    }

    public void addToClassificationList(List<Integer[]> list) {
        for (Integer[] integers : list) {
            classificationList.add(integers);
        }
    }

    public List<Integer[]> getClassificationList() {
        return classificationList;
    }

    @Override
    public void clear() {
        super.clear();
        classificationList.clear();
    }
}
